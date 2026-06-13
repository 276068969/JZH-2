<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { get, post, put, del } from '@/utils/request'

interface PrisonerTransfer {
  id: number
  prisonerId: number
  prisonerNumber: string
  prisonerName: string
  fromAreaId: number | null
  fromAreaName: string
  fromCellId: number | null
  fromCellNumber: string
  toAreaId: number | null
  toAreaName: string
  toCellId: number | null
  toCellNumber: string
  transferType: string
  transferTime: string
  transferReason: string
  operatorId: number | null
  operatorName: string
  remark: string
  createTime: string
}

interface PrisonArea {
  id: number
  areaName: string
  areaCode: string
}

interface Cell {
  id: number
  cellNumber: string
  areaId: number
  capacity: number
  currentOccupancy: number
  status: string
}

interface Prisoner {
  id: number
  name: string
  prisonerNumber: string
  areaId: number | null
  cellId: number | null
}

const tableData = ref<PrisonerTransfer[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const tableLoading = ref(false)

const searchForm = reactive({
  keyword: '',
  prisonerId: null as number | null,
  fromAreaId: null as number | null,
  toAreaId: null as number | null,
  transferType: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  id: 0,
  prisonerId: null as number | null,
  toAreaId: null as number | null,
  toCellId: null as number | null,
  transferType: 'BOTH',
  transferTime: '',
  transferReason: '',
  operatorName: '',
  remark: ''
})

const prisonAreaList = ref<PrisonArea[]>([])
const cellList = ref<Cell[]>([])
const prisonerList = ref<Prisoner[]>([])

const transferTypeOptions = [
  { label: '调监', value: 'AREA_TRANSFER' },
  { label: '调舍', value: 'CELL_TRANSFER' },
  { label: '调监调舍', value: 'BOTH' }
]

const transferTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    AREA_TRANSFER: '调监',
    CELL_TRANSFER: '调舍',
    BOTH: '调监调舍'
  }
  return map[type] || type
}

const transferTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    AREA_TRANSFER: 'primary',
    CELL_TRANSFER: 'success',
    BOTH: 'warning'
  }
  return map[type] || 'info'
}

const filteredCells = computed(() => {
  if (!form.toAreaId) return []
  return cellList.value.filter(c => c.areaId === form.toAreaId)
})

async function loadPrisonAreas() {
  try {
    const res: any = await get('/prison-areas/all')
    if (res.data.code === 200) {
      prisonAreaList.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载监区列表失败', e)
  }
}

async function loadCells() {
  try {
    const res: any = await get('/cells', { page: 1, size: 100 })
    if (res.data.code === 200) {
      cellList.value = res.data.data.records || []
    }
  } catch (e) {
    console.error('加载监舍列表失败', e)
  }
}

async function loadPrisoners() {
  try {
    const res: any = await get('/prisoners/all')
    if (res.data.code === 200) {
      prisonerList.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载服刑人员列表失败', e)
  }
}

async function loadData() {
  tableLoading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.prisonerId) params.prisonerId = searchForm.prisonerId
    if (searchForm.fromAreaId) params.fromAreaId = searchForm.fromAreaId
    if (searchForm.toAreaId) params.toAreaId = searchForm.toAreaId
    if (searchForm.transferType) params.transferType = searchForm.transferType

    const res: any = await get('/prisoner-transfers', params)
    if (res.data.code === 200) {
      tableData.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
  } catch (e) {
    console.error('加载调动记录失败', e)
    ElMessage.error('加载数据失败')
  } finally {
    tableLoading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.prisonerId = null
  searchForm.fromAreaId = null
  searchForm.toAreaId = null
  searchForm.transferType = ''
  currentPage.value = 1
  loadData()
}

function handleAdd() {
  dialogTitle.value = '新增调监调舍'
  Object.assign(form, {
    id: 0,
    prisonerId: null,
    toAreaId: null,
    toCellId: null,
    transferType: 'BOTH',
    transferTime: new Date().toISOString().slice(0, 16),
    transferReason: '',
    operatorName: '',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row: PrisonerTransfer) {
  dialogTitle.value = '编辑调动记录'
  Object.assign(form, {
    id: row.id,
    prisonerId: row.prisonerId,
    toAreaId: row.toAreaId,
    toCellId: row.toCellId,
    transferType: row.transferType,
    transferTime: row.transferTime?.slice(0, 16) || '',
    transferReason: row.transferReason,
    operatorName: row.operatorName,
    remark: row.remark
  })
  dialogVisible.value = true
}

function handleDelete(row: PrisonerTransfer) {
  ElMessageBox.confirm('确定要删除该调动记录吗？删除最近一次调动将回滚服刑人员位置。', '确认删除', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      const res: any = await del(`/prisoner-transfers/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      }
    } catch (e) {
      console.error('删除失败', e)
    }
  })
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!form.prisonerId) {
    ElMessage.warning('请选择服刑人员')
    return
  }
  if (!form.toAreaId && !form.toCellId) {
    ElMessage.warning('新监区和新监舍不能同时为空')
    return
  }

  loading.value = true
  try {
    if (form.id === 0) {
      const res: any = await post('/prisoner-transfers', form)
      if (res.data.code === 200) {
        ElMessage.success('调动登记成功')
        dialogVisible.value = false
        loadData()
      }
    } else {
      const res: any = await put(`/prisoner-transfers/${form.id}`, form)
      if (res.data.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadData()
      }
    }
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    loading.value = false
  }
}

const formRules: FormRules = {
  prisonerId: [{ required: true, message: '请选择服刑人员', trigger: 'change' }],
  transferReason: [{ required: true, message: '请输入调动原因', trigger: 'blur' }],
  transferTime: [{ required: true, message: '请选择调动时间', trigger: 'change' }]
}

onMounted(() => {
  loadPrisonAreas()
  loadCells()
  loadPrisoners()
  loadData()
})
</script>

<template>
  <div class="list-page">
    <div class="page-header"><h2>调监调舍管理</h2></div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索姓名/编号/监区/监舍" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-select v-model="searchForm.fromAreaId" placeholder="原监区" clearable class="search-select">
        <el-option v-for="area in prisonAreaList" :key="area.id" :label="area.areaName" :value="area.id" />
      </el-select>
      <el-select v-model="searchForm.toAreaId" placeholder="新监区" clearable class="search-select">
        <el-option v-for="area in prisonAreaList" :key="area.id" :label="area.areaName" :value="area.id" />
      </el-select>
      <el-select v-model="searchForm.transferType" placeholder="调动类型" clearable class="search-select">
        <el-option v-for="opt in transferTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="primary" @click="handleAdd">登记调动</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="tableLoading">
      <el-table-column prop="prisonerNumber" label="服刑人员编号" width="130" />
      <el-table-column prop="prisonerName" label="姓名" width="90" />
      <el-table-column label="原位置" min-width="160">
        <template #default="{ row }">
          <div class="location-cell">
            <span class="location-label">监区：</span>{{ row.fromAreaName || '无' }}
            <br />
            <span class="location-label">监舍：</span>{{ row.fromCellNumber || '无' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="新位置" min-width="160">
        <template #default="{ row }">
          <div class="location-cell">
            <span class="location-label">监区：</span>{{ row.toAreaName || '无' }}
            <br />
            <span class="location-label">监舍：</span>{{ row.toCellNumber || '无' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="transferType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="transferTypeTagType(row.transferType)" size="small">
            {{ transferTypeLabel(row.transferType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="transferTime" label="调动时间" width="160">
        <template #default="{ row }">{{ row.transferTime?.replace('T', ' ') }}</template>
      </el-table-column>
      <el-table-column prop="transferReason" label="调动原因" min-width="150" show-overflow-tooltip />
      <el-table-column prop="operatorName" label="操作人" width="100" />
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
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="服刑人员" prop="prisonerId">
              <el-select v-model="form.prisonerId" filterable placeholder="请选择服刑人员" style="width: 100%" :disabled="form.id !== 0">
                <el-option v-for="p in prisonerList" :key="p.id" :label="`${p.name} (${p.prisonerNumber})`" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调动类型" prop="transferType">
              <el-select v-model="form.transferType" style="width: 100%" :disabled="form.id !== 0">
                <el-option v-for="opt in transferTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="新监区">
              <el-select v-model="form.toAreaId" placeholder="请选择新监区" style="width: 100%" :disabled="form.id !== 0" @change="form.toCellId = null">
                <el-option v-for="area in prisonAreaList" :key="area.id" :label="area.areaName" :value="area.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新监舍">
              <el-select v-model="form.toCellId" placeholder="请选择新监舍" filterable style="width: 100%" :disabled="form.id !== 0">
                <el-option v-for="cell in filteredCells" :key="cell.id" :label="`${cell.cellNumber} (${cell.currentOccupancy}/${cell.capacity})`" :value="cell.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="调动时间" prop="transferTime">
              <el-date-picker v-model="form.transferTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作人">
              <el-input v-model="form.operatorName" placeholder="请输入操作人姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="调动原因" prop="transferReason">
          <el-input v-model="form.transferReason" type="textarea" :rows="2" placeholder="请输入调动原因" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注信息" />
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
.page-header h2 { font-size: 18px; color: #303133; margin-bottom: 16px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }
.search-input { width: 260px; }
.search-select { width: 140px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.location-cell { font-size: 13px; line-height: 1.6; }
.location-label { color: #909399; }
</style>
