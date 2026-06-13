<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import request from '@/utils/request'

interface Incident {
  id: number
  incidentTitle: string
  incidentType: string
  severity: string
  areaId: number | null
  reportGuardId: number | null
  relatedPrisonerId: number | null
  occurTime: string
  description: string
  handlerResult: string
  status: string
  createTime: string
  updateTime: string
}

const incidentTypeMap: Record<string, string> = {
  FIGHT: '斗殴',
  ESCAPE_ATTEMPT: '越狱企图',
  MEDICAL: '医疗',
  DISCIPLINE: '违纪',
  OTHER: '其他'
}

const severityMap: Record<string, { label: string; type: string }> = {
  LOW: { label: '低', type: 'info' },
  MEDIUM: { label: '中', type: '' },
  HIGH: { label: '高', type: 'warning' },
  CRITICAL: { label: '严重', type: 'danger' }
}

const statusMap: Record<string, { label: string; type: string }> = {
  PENDING: { label: '待处理', type: 'info' },
  PROCESSING: { label: '处理中', type: 'warning' },
  RESOLVED: { label: '已解决', type: '' },
  CLOSED: { label: '已关闭', type: 'success' }
}

const statusList = [
  { value: 'PENDING', label: '待处理' },
  { value: 'PROCESSING', label: '处理中' },
  { value: 'RESOLVED', label: '已解决' },
  { value: 'CLOSED', label: '已关闭' }
]

const tableData = ref<Incident[]>([])
const searchForm = reactive({ keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)
const tableLoading = ref(false)

const form = reactive<Incident>({
  id: 0,
  incidentTitle: '',
  incidentType: 'DISCIPLINE',
  severity: 'LOW',
  areaId: null,
  reportGuardId: null,
  relatedPrisonerId: null,
  occurTime: '',
  description: '',
  handlerResult: '',
  status: 'PENDING',
  createTime: '',
  updateTime: ''
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const pagedData = computed(() => tableData.value)

function formatDateTime(val: string) {
  if (!val) return ''
  return val.slice(0, 16).replace('T', ' ')
}

function getIncidentTypeLabel(type: string) {
  return incidentTypeMap[type] || type
}

function getSeverityInfo(sev: string) {
  return severityMap[sev] || { label: sev, type: 'info' }
}

function getStatusInfo(st: string) {
  return statusMap[st] || { label: st, type: 'info' }
}

async function loadData() {
  tableLoading.value = true
  try {
    const res: any = await request.get('/incidents', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchForm.keyword || undefined
      }
    })
    const payload = res?.data?.data
    if (payload && payload.records) {
      tableData.value = payload.records
      total.value = payload.total || 0
    } else if (Array.isArray(payload)) {
      tableData.value = payload
      total.value = payload.length
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载数据失败')
  } finally {
    tableLoading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handleAdd() {
  dialogTitle.value = '新增事件'
  Object.assign(form, {
    id: 0,
    incidentTitle: '',
    incidentType: 'DISCIPLINE',
    severity: 'LOW',
    areaId: null,
    reportGuardId: null,
    relatedPrisonerId: null,
    occurTime: '',
    description: '',
    handlerResult: '',
    status: 'PENDING'
  })
  dialogVisible.value = true
}

function handleEdit(row: Incident) {
  dialogTitle.value = '编辑事件'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

function handleDelete(row: Incident) {
  ElMessageBox.confirm('确定要删除该事件记录吗？', '确认删除', { type: 'warning' })
    .then(async () => {
      try {
        await request.delete(`/incidents/${row.id}`)
        ElMessage.success('删除成功')
        loadData()
      } catch (e: any) {
        ElMessage.error(e?.response?.data?.message || e?.message || '删除失败')
      }
    })
    .catch(() => {})
}

async function handleStartProcessing(row: Incident) {
  try {
    await request.put(`/incidents/${row.id}/start-processing`)
    ElMessage.success('已开始处理')
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
  }
}

async function handleResolve(row: Incident) {
  const { value } = await ElMessageBox.prompt('请输入处置结论', '解决事件', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请填写详细描述处置措施和结果...',
    inputValidator: (v: string) => {
      if (!v || !v.trim()) return '处置结论不能为空'
      return true
    }
  })
  try {
    await request.put(`/incidents/${row.id}/resolve`, { handlerResult: value })
    ElMessage.success('事件已解决')
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
  }
}

async function handleClose(row: Incident) {
  ElMessageBox.confirm('确定要关闭该事件吗？关闭后将不可再变更状态。', '确认关闭', {
    type: 'warning'
  })
    .then(async () => {
      try {
        await request.put(`/incidents/${row.id}/close`)
        ElMessage.success('事件已关闭')
        loadData()
      } catch (e: any) {
        ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
      }
    })
    .catch(() => {})
}

function canStartProcessing(row: Incident) {
  return row.status === 'PENDING'
}

function canResolve(row: Incident) {
  return row.status === 'PROCESSING'
}

function canClose(row: Incident) {
  return row.status === 'RESOLVED'
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    if (form.id === 0) {
      await request.post('/incidents', form)
      ElMessage.success('新增成功')
    } else {
      await request.put(`/incidents/${form.id}`, form)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '保存失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="list-page">
    <div class="page-header">
      <h2>事件管理</h2>
    </div>
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索标题 / 类型"
        clearable
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增事件</el-button>
    </div>
    <el-table :data="pagedData" border stripe v-loading="tableLoading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="incidentTitle" label="事件标题" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          {{ getIncidentTypeLabel(row.incidentType) }}
        </template>
      </el-table-column>
      <el-table-column label="严重程度" width="90">
        <template #default="{ row }">
          <el-tag :type="getSeverityInfo(row.severity).type" size="small">
            {{ getSeverityInfo(row.severity).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发生时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.occurTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="事件描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="handlerResult" label="处置结论" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusInfo(row.status).type" size="small">
            {{ getStatusInfo(row.status).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button
            v-if="canStartProcessing(row)"
            type="success"
            link
            size="small"
            @click="handleStartProcessing(row)"
          >开始处理</el-button>
          <el-button
            v-if="canResolve(row)"
            type="warning"
            link
            size="small"
            @click="handleResolve(row)"
          >解决</el-button>
          <el-button
            v-if="canClose(row)"
            type="info"
            link
            size="small"
            @click="handleClose(row)"
          >关闭</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="事件标题" prop="incidentTitle" :rules="[{ required: true, message: '请输入事件标题' }]">
              <el-input v-model="form.incidentTitle" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="事件类型" prop="incidentType" :rules="[{ required: true, message: '请选择事件类型' }]">
              <el-select v-model="form.incidentType" style="width: 100%">
                <el-option label="斗殴" value="FIGHT" />
                <el-option label="越狱企图" value="ESCAPE_ATTEMPT" />
                <el-option label="医疗" value="MEDICAL" />
                <el-option label="违纪" value="DISCIPLINE" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="严重程度" prop="severity" :rules="[{ required: true, message: '请选择严重程度' }]">
              <el-select v-model="form.severity" style="width: 100%">
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
                <el-option label="严重" value="CRITICAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发生时间" prop="occurTime" :rules="[{ required: true, message: '请选择发生时间' }]">
              <el-date-picker
                v-model="form.occurTime"
                type="datetime"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="监区ID">
              <el-input-number v-model="form.areaId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="上报警员ID">
              <el-input-number v-model="form.reportGuardId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="关联服刑人员ID">
              <el-input-number v-model="form.relatedPrisonerId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="事件描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="处置结论">
          <el-input v-model="form.handlerResult" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="状态" prop="status" :rules="[{ required: true, message: '请选择状态' }]">
          <el-select v-model="form.status" style="width: 100%">
            <el-option
              v-for="s in statusList"
              :key="s.value"
              :label="s.label"
              :value="s.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.list-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.page-header h2 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 16px;
}
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.search-input {
  width: 260px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
