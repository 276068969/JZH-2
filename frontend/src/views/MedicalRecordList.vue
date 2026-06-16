<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { get, post, put, del } from '@/utils/request'

interface MedicalRecord {
  id: number
  prisonerId: number
  recordDate: string
  diagnosis: string
  treatment: string
  hospital: string
  doctorName: string
  medicalType: string
  result: string
  medicine: string
  followUpDate: string
  followUpStatus: string
  actualFollowUpDate: string
  followUpResult: string
  followUpRemark: string
  prisonerName?: string
  prisonerNumber?: string
}

interface Prisoner {
  id: number
  name: string
  prisonerNumber: string
  healthStatus: string
}

const tableData = ref<MedicalRecord[]>([])
const prisonerMap = ref<Record<number, Prisoner>>({})
const searchForm = reactive({ keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)
const tableLoading = ref(false)

const form = reactive<MedicalRecord>({
  id: 0,
  prisonerId: 0,
  recordDate: '',
  diagnosis: '',
  treatment: '',
  hospital: '监狱医务室',
  doctorName: '',
  medicalType: 'OUTPATIENT',
  result: 'TREATING',
  medicine: '',
  followUpDate: '',
  followUpStatus: 'PENDING',
  actualFollowUpDate: '',
  followUpResult: '',
  followUpRemark: ''
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const medicalTypeOptions = [
  { label: '体检', value: 'PHYSICAL' },
  { label: '门诊', value: 'OUTPATIENT' },
  { label: '急诊', value: 'EMERGENCY' },
  { label: '住院', value: 'HOSPITALIZATION' },
  { label: '心理咨询', value: 'PSYCHOLOGICAL' }
]

const resultOptions = [
  { label: '已治愈', value: 'RECOVERED' },
  { label: '治疗中', value: 'TREATING' },
  { label: '病情稳定', value: 'STABLE' },
  { label: '已痊愈', value: 'CURED' },
  { label: '已转院', value: 'TRANSFERRED' },
  { label: '已故', value: 'DECEASED' }
]

const followUpStatusOptions = [
  { label: '待复诊', value: 'PENDING' },
  { label: '已复诊', value: 'COMPLETED' },
  { label: '未复诊', value: 'MISSED' },
  { label: '已取消', value: 'CANCELLED' }
]

const prisonerOptions = computed(() => {
  return Object.values(prisonerMap.value).map(p => ({
    label: `${p.name}（${p.prisonerNumber}）`,
    value: p.id
  }))
})

function getMedicalTypeLabel(type: string) {
  const map: Record<string, string> = {
    PHYSICAL: '体检',
    OUTPATIENT: '门诊',
    EMERGENCY: '急诊',
    HOSPITALIZATION: '住院',
    PSYCHOLOGICAL: '心理咨询'
  }
  return map[type] || type
}

function getResultLabel(result: string) {
  const map: Record<string, string> = {
    RECOVERED: '已治愈',
    TREATING: '治疗中',
    STABLE: '病情稳定',
    CURED: '已痊愈',
    TRANSFERRED: '已转院',
    DECEASED: '已故'
  }
  return map[result] || result
}

function getResultTagType(result: string) {
  const map: Record<string, string> = {
    RECOVERED: 'success',
    TREATING: 'warning',
    STABLE: '',
    CURED: 'success',
    TRANSFERRED: 'info',
    DECEASED: 'danger'
  }
  return map[result] || 'info'
}

function getPrisonerName(prisonerId: number) {
  return prisonerMap.value[prisonerId]?.name || '-'
}

function getPrisonerNumber(prisonerId: number) {
  return prisonerMap.value[prisonerId]?.prisonerNumber || '-'
}

async function fetchList() {
  tableLoading.value = true
  try {
    const res = await get('/medical-records', {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchForm.keyword || undefined
    })
    if (res.data.code === 200) {
      const data = res.data.data
      tableData.value = data.records || data.list || []
      total.value = data.total || 0
      await enrichWithPrisonerInfo(tableData.value)
    }
  } catch (e) {
    console.error('获取医疗记录列表失败', e)
  } finally {
    tableLoading.value = false
  }
}

async function fetchPrisoners() {
  try {
    const res = await get('/prisoners/all')
    if (res.data.code === 200) {
      const list = res.data.data || []
      prisonerMap.value = {}
      list.forEach((p: Prisoner) => {
        prisonerMap.value[p.id] = p
      })
    }
  } catch (e) {
    console.error('获取服刑人员列表失败', e)
  }
}

async function enrichWithPrisonerInfo(records: MedicalRecord[]) {
  const missingIds = records
    .filter(r => !prisonerMap.value[r.prisonerId])
    .map(r => r.prisonerId)
  
  if (missingIds.length > 0) {
    await fetchPrisoners()
  }
  
  records.forEach(r => {
    const p = prisonerMap.value[r.prisonerId]
    if (p) {
      r.prisonerName = p.name
      r.prisonerNumber = p.prisonerNumber
    }
  })
}

function handleSearch() {
  currentPage.value = 1
  fetchList()
}

function handleAdd() {
  dialogTitle.value = '新增医疗记录'
  Object.assign(form, {
    id: 0,
    prisonerId: 0,
    recordDate: '',
    diagnosis: '',
    treatment: '',
    hospital: '监狱医务室',
    doctorName: '',
    medicalType: 'OUTPATIENT',
    result: 'TREATING',
    medicine: '',
    followUpDate: '',
    followUpStatus: 'PENDING',
    actualFollowUpDate: '',
    followUpResult: '',
    followUpRemark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row: MedicalRecord) {
  dialogTitle.value = '编辑医疗记录'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

function handleDelete(row: MedicalRecord) {
  const prisonerName = getPrisonerName(row.prisonerId)
  ElMessageBox.confirm(`确定要删除 ${prisonerName} 的医疗记录吗？`, '确认删除', { type: 'warning' })
    .then(async () => {
      try {
        const res = await del(`/medical-records/${row.id}`)
        if (res.data.code === 200) {
          ElMessage.success('删除成功')
          fetchList()
          if (row.prisonerId) {
            refreshPrisonerHealthStatus(row.prisonerId)
          }
        }
      } catch (e) {
        console.error('删除失败', e)
      }
    })
}

async function refreshPrisonerHealthStatus(prisonerId: number) {
  try {
    const res = await get(`/prisoners/${prisonerId}`)
    if (res.data.code === 200) {
      const prisoner = res.data.data
      if (prisoner && prisonerMap.value[prisonerId]) {
        prisonerMap.value[prisonerId].healthStatus = prisoner.healthStatus
      }
    }
  } catch (e) {
    console.error('刷新服刑人员健康状态失败', e)
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!form.prisonerId) {
    ElMessage.warning('请选择服刑人员')
    return
  }
  loading.value = true
  try {
    if (form.id === 0) {
      const res = await post('/medical-records', form)
      if (res.data.code === 200) {
        ElMessage.success('新增成功')
        dialogVisible.value = false
        fetchList()
        refreshPrisonerHealthStatus(form.prisonerId)
      }
    } else {
      const res = await put(`/medical-records/${form.id}`, form)
      if (res.data.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchList()
        refreshPrisonerHealthStatus(form.prisonerId)
      }
    }
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  fetchList()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  fetchList()
}

onMounted(() => {
  fetchPrisoners()
  fetchList()
})
</script>

<template>
  <div class="list-page">
    <div class="page-header">
      <h2>医疗记录</h2>
      <el-button type="primary" @click="fetchList">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索诊断 / 医生 / 医疗类型" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增记录</el-button>
    </div>
    <el-table :data="tableData" border stripe v-loading="tableLoading">
      <el-table-column prop="recordDate" label="就诊日期" width="110" />
      <el-table-column label="服刑人员" width="100">
        <template #default="{ row }">
          {{ getPrisonerName(row.prisonerId) }}
        </template>
      </el-table-column>
      <el-table-column label="编号" width="110">
        <template #default="{ row }">
          {{ getPrisonerNumber(row.prisonerId) }}
        </template>
      </el-table-column>
      <el-table-column prop="diagnosis" label="诊断" width="120" />
      <el-table-column prop="treatment" label="治疗方案" min-width="180" show-overflow-tooltip />
      <el-table-column prop="doctorName" label="医生" width="90" />
      <el-table-column prop="hospital" label="就诊机构" width="140" />
      <el-table-column label="医疗类型" width="90">
        <template #default="{ row }">
          {{ getMedicalTypeLabel(row.medicalType) }}
        </template>
      </el-table-column>
      <el-table-column label="结果" width="90">
        <template #default="{ row }">
          <el-tag :type="getResultTagType(row.result)" size="small">{{ getResultLabel(row.result) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="followUpDate" label="复诊日期" width="110" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="服刑人员">
              <el-select v-model="form.prisonerId" placeholder="请选择服刑人员" filterable style="width: 100%">
                <el-option
                  v-for="opt in prisonerOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="就诊日期">
              <el-date-picker v-model="form.recordDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医疗类型">
              <el-select v-model="form.medicalType" style="width: 100%">
                <el-option v-for="opt in medicalTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="诊断结果">
              <el-select v-model="form.result" style="width: 100%">
                <el-option v-for="opt in resultOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主治医生">
              <el-input v-model="form.doctorName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="诊断">
          <el-input v-model="form.diagnosis" />
        </el-form-item>
        <el-form-item label="治疗方案">
          <el-input v-model="form.treatment" type="textarea" :rows="2" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="就诊机构">
              <el-input v-model="form.hospital" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用药">
              <el-input v-model="form.medicine" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="复诊日期">
              <el-date-picker v-model="form.followUpDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="复诊状态">
              <el-select v-model="form.followUpStatus" style="width: 100%">
                <el-option v-for="opt in followUpStatusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="实际复诊日">
              <el-date-picker v-model="form.actualFollowUpDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="复诊结果">
              <el-input v-model="form.followUpResult" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="复诊备注">
          <el-input v-model="form.followUpRemark" type="textarea" />
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
.list-page { background: #fff; border-radius: 8px; padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 18px; color: #303133; margin: 0; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.search-input { width: 260px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
