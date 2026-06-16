<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { get, post, put, del } from '@/utils/request'

interface Prisoner {
  id: number
  name: string
  gender: string
  prisonerNumber: string
  areaId: number | null
  cellId: number | null
  cellNumber: string
  crimeType: string
  sentenceTerm: number
  entryDate: string
  releaseDate: string
  status: string
  dangerLevel: string
  idCard: string
  nativePlace: string
  birthDate: string
  educationLevel: string
  maritalStatus: string
  occupation: string
  healthStatus: string
  photoUrl: string
  remark: string
}

interface PrisonArea {
  id: number
  areaName: string
  areaCode: string
}

const tableData = ref<Prisoner[]>([])
const prisonAreaList = ref<PrisonArea[]>([])
const advancedFilterVisible = ref(false)
const drawerVisible = ref(false)
const selectedPrisoner = ref<Prisoner | null>(null)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)
const tableLoading = ref(false)

const searchForm = reactive({
  keyword: '',
  areaId: null as number | null,
  dangerLevel: '',
  status: '',
  gender: '',
  crimeType: '',
  minAge: null as number | null,
  maxAge: null as number | null
})

const form = reactive<Prisoner>({
  id: 0,
  name: '',
  gender: '男',
  prisonerNumber: '',
  areaId: null,
  cellId: null,
  cellNumber: '',
  crimeType: '',
  sentenceTerm: 0,
  entryDate: '',
  releaseDate: '',
  status: 'INCARCERATED',
  dangerLevel: 'LOW',
  idCard: '',
  nativePlace: '',
  birthDate: '',
  educationLevel: '',
  maritalStatus: '',
  occupation: '',
  healthStatus: '',
  photoUrl: '',
  remark: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  prisonerNumber: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  areaId: [{ required: true, message: '请选择监区', trigger: 'change' }],
  cellNumber: [{ required: true, message: '请输入监舍号', trigger: 'blur' }]
}

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dangerLevelOptions = [
  { label: '低危', value: 'LOW' },
  { label: '中危', value: 'MEDIUM' },
  { label: '高危', value: 'HIGH' },
  { label: '极高危', value: 'EXTREME' }
]

const statusOptions = [
  { label: '在押', value: 'INCARCERATED' },
  { label: '已释放', value: 'RELEASED' },
  { label: '调监', value: 'TRANSFERRED' },
  { label: '保外就医', value: 'MEDICAL_PAROLE' }
]

const genderOptions = [
  { label: '男', value: '男' },
  { label: '女', value: '女' }
]

const educationOptions = [
  { label: '小学', value: '小学' },
  { label: '初中', value: '初中' },
  { label: '高中', value: '高中' },
  { label: '大专', value: '大专' },
  { label: '本科', value: '本科' },
  { label: '研究生', value: '研究生' }
]

const maritalOptions = [
  { label: '未婚', value: '未婚' },
  { label: '已婚', value: '已婚' },
  { label: '离异', value: '离异' },
  { label: '丧偶', value: '丧偶' }
]

function getDangerLevelTag(level: string) {
  const map: Record<string, { text: string; type: string }> = {
    LOW: { text: '低危', type: 'success' },
    MEDIUM: { text: '中危', type: 'warning' },
    HIGH: { text: '高危', type: 'danger' },
    EXTREME: { text: '极高危', type: 'danger' }
  }
  return map[level] || { text: '未知', type: 'info' }
}

function getStatusTag(status: string) {
  const map: Record<string, { text: string; type: string }> = {
    INCARCERATED: { text: '在押', type: 'danger' },
    RELEASED: { text: '已释放', type: 'success' },
    TRANSFERRED: { text: '调监', type: 'primary' },
    MEDICAL_PAROLE: { text: '保外就医', type: 'warning' }
  }
  return map[status] || { text: status, type: 'info' }
}

function getAreaName(areaId: number | null) {
  if (!areaId) return '-'
  const area = prisonAreaList.value.find(a => a.id === areaId)
  return area?.areaName || '-'
}

function calculateAge(birthDate: string) {
  if (!birthDate) return 0
  const today = new Date()
  const birth = new Date(birthDate)
  let age = today.getFullYear() - birth.getFullYear()
  const m = today.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
    age--
  }
  return age
}

function formatSentence(months: number) {
  if (!months) return '无期'
  const years = Math.floor(months / 12)
  const remainMonths = months % 12
  if (years === 0) return `${remainMonths}个月`
  if (remainMonths === 0) return `${years}年`
  return `${years}年${remainMonths}个月`
}

function getRemainingDays(releaseDate: string) {
  if (!releaseDate) return '无期'
  const today = new Date()
  const release = new Date(releaseDate)
  const diff = Math.ceil((release.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  if (diff <= 0) return '已到期'
  return `${diff}天`
}

async function fetchList() {
  tableLoading.value = true
  try {
    const params: Record<string, any> = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchForm.keyword || undefined
    }
    if (searchForm.areaId) params.areaId = searchForm.areaId
    if (searchForm.dangerLevel) params.dangerLevel = searchForm.dangerLevel
    if (searchForm.status) params.status = searchForm.status
    if (searchForm.gender) params.gender = searchForm.gender
    if (searchForm.crimeType) params.crimeType = searchForm.crimeType
    if (searchForm.minAge) params.minAge = searchForm.minAge
    if (searchForm.maxAge) params.maxAge = searchForm.maxAge

    const res = await post('/prisoners/search', params)
    if (res.data.code === 200) {
      const data = res.data.data
      tableData.value = data.records || data.list || []
      total.value = data.total || 0
    }
  } catch (e) {
    console.error('获取服刑人员列表失败', e)
  } finally {
    tableLoading.value = false
  }
}

async function fetchPrisonAreas() {
  try {
    const res = await get('/prison-areas/all')
    if (res.data.code === 200) {
      prisonAreaList.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取监区列表失败', e)
  }
}

async function fetchPrisonerDetail(id: number) {
  try {
    const res = await get(`/prisoners/${id}`)
    if (res.data.code === 200) {
      return res.data.data
    }
  } catch (e) {
    console.error('获取服刑人员详情失败', e)
  }
  return null
}

function handleSearch() {
  currentPage.value = 1
  fetchList()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.areaId = null
  searchForm.dangerLevel = ''
  searchForm.status = ''
  searchForm.gender = ''
  searchForm.crimeType = ''
  searchForm.minAge = null
  searchForm.maxAge = null
  currentPage.value = 1
  fetchList()
}

async function handleViewDetail(row: Prisoner) {
  const detail = await fetchPrisonerDetail(row.id)
  if (detail) {
    selectedPrisoner.value = detail
  } else {
    selectedPrisoner.value = row
  }
  drawerVisible.value = true
}

function handleAdd() {
  dialogTitle.value = '新增服刑人员'
  Object.assign(form, {
    id: 0,
    name: '',
    gender: '男',
    prisonerNumber: '',
    areaId: null,
    cellId: null,
    cellNumber: '',
    crimeType: '',
    sentenceTerm: 0,
    entryDate: '',
    releaseDate: '',
    status: 'INCARCERATED',
    dangerLevel: 'LOW',
    idCard: '',
    nativePlace: '',
    birthDate: '',
    educationLevel: '',
    maritalStatus: '',
    occupation: '',
    healthStatus: '',
    photoUrl: '',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row: Prisoner) {
  dialogTitle.value = '编辑服刑人员'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

function handleDelete(row: Prisoner) {
  ElMessageBox.confirm(`确定要删除服刑人员 "${row.name}" 吗？`, '确认删除', { type: 'warning' })
    .then(async () => {
      try {
        const res = await del(`/prisoners/${row.id}`)
        if (res.data.code === 200) {
          ElMessage.success('删除成功')
          fetchList()
        }
      } catch (e) {
        console.error('删除失败', e)
      }
    })
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    if (form.id === 0) {
      const res = await post('/prisoners', form)
      if (res.data.code === 200) {
        ElMessage.success('新增成功')
        dialogVisible.value = false
        fetchList()
      }
    } else {
      const res = await put(`/prisoners/${form.id}`, form)
      if (res.data.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchList()
        if (selectedPrisoner.value && selectedPrisoner.value.id === form.id) {
          selectedPrisoner.value = { ...form } as Prisoner
        }
      }
    }
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    loading.value = false
  }
}

function closeDrawer() {
  drawerVisible.value = false
  selectedPrisoner.value = null
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

function handleRefresh() {
  fetchList()
  ElMessage.success('已刷新')
}

onMounted(() => {
  fetchPrisonAreas()
  fetchList()
})
</script>

<template>
  <div class="prisoner-list-page">
    <div class="page-header">
      <h2>服刑人员管理</h2>
      <div class="header-actions">
        <el-button type="primary" link @click="advancedFilterVisible = !advancedFilterVisible">
          {{ advancedFilterVisible ? '收起筛选' : '高级筛选' }}
          <el-icon class="ml-5px"><component :is="advancedFilterVisible ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
        </el-button>
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索姓名 / 编号 / 身份证号"
        clearable
        class="keyword-input"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="searchForm.areaId"
        placeholder="选择监区"
        clearable
        class="filter-select"
        @change="handleSearch"
      >
        <el-option
          v-for="area in prisonAreaList"
          :key="area.id"
          :label="area.areaName"
          :value="area.id"
        />
      </el-select>
      <el-select
        v-model="searchForm.dangerLevel"
        placeholder="危险等级"
        clearable
        class="filter-select"
        @change="handleSearch"
      >
        <el-option
          v-for="item in dangerLevelOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-select
        v-model="searchForm.status"
        placeholder="在押状态"
        clearable
        class="filter-select"
        @change="handleSearch"
      >
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="primary" @click="handleAdd">新增服刑人员</el-button>
    </div>

    <el-collapse-transition>
      <div v-show="advancedFilterVisible" class="advanced-filter-panel">
        <el-form :inline="true" label-width="80px">
          <el-form-item label="性别">
            <el-select v-model="searchForm.gender" placeholder="请选择" clearable @change="handleSearch">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
          <el-form-item label="罪名">
            <el-input
              v-model="searchForm.crimeType"
              placeholder="输入罪名关键词"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="年龄范围">
            <el-input-number
              v-model="searchForm.minAge"
              :min="18"
              :max="100"
              placeholder="最小"
              style="width: 100px"
              @change="handleSearch"
            />
            <span class="age-separator">-</span>
            <el-input-number
              v-model="searchForm.maxAge"
              :min="18"
              :max="100"
              placeholder="最大"
              style="width: 100px"
              @change="handleSearch"
            />
          </el-form-item>
        </el-form>
      </div>
    </el-collapse-transition>

    <el-table
      :data="tableData"
      border
      stripe
      style="width: 100%"
      v-loading="tableLoading"
      highlight-current-row
    >
      <el-table-column type="expand" width="50">
        <template #default="{ row }">
          <div class="expand-info">
            <div class="info-item"><span class="label">身份证号：</span>{{ row.idCard || '-' }}</div>
            <div class="info-item"><span class="label">籍贯：</span>{{ row.nativePlace || '-' }}</div>
            <div class="info-item"><span class="label">文化程度：</span>{{ row.educationLevel || '-' }}</div>
            <div class="info-item"><span class="label">健康状况：</span>{{ row.healthStatus || '健康' }}</div>
            <div class="info-item"><span class="label">剩余刑期：</span>{{ getRemainingDays(row.releaseDate) }}</div>
            <div class="info-item"><span class="label">备注：</span>{{ row.remark || '无' }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="prisonerNumber" label="编号" width="110" />
      <el-table-column prop="name" label="姓名" width="80" />
      <el-table-column prop="gender" label="性别" width="60" />
      <el-table-column label="年龄" width="60">
        <template #default="{ row }">
          {{ calculateAge(row.birthDate) || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="监区" width="90">
        <template #default="{ row }">
          {{ getAreaName(row.areaId) }}
        </template>
      </el-table-column>
      <el-table-column prop="cellNumber" label="监舍号" width="90" />
      <el-table-column prop="crimeType" label="罪名" width="100" />
      <el-table-column label="刑期" width="90">
        <template #default="{ row }">
          {{ formatSentence(row.sentenceTerm) }}
        </template>
      </el-table-column>
      <el-table-column label="危险等级" width="80">
        <template #default="{ row }">
          <el-tag :type="getDangerLevelTag(row.dangerLevel).type" size="small">
            {{ getDangerLevelTag(row.dangerLevel).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="健康状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.healthStatus && row.healthStatus !== '健康'" type="warning" size="small">
            {{ row.healthStatus }}
          </el-tag>
          <span v-else class="health-normal">健康</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status).type" size="small">
            {{ getStatusTag(row.status).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="entryDate" label="入狱日期" width="110" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
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

    <el-drawer
      v-model="drawerVisible"
      title="服刑人员详情"
      direction="rtl"
      size="480px"
      :before-close="closeDrawer"
    >
      <div v-if="selectedPrisoner" class="detail-drawer">
        <div class="detail-header">
          <div class="avatar">{{ selectedPrisoner.name.charAt(0) }}</div>
          <div class="basic-info">
            <h3>{{ selectedPrisoner.name }}</h3>
            <div class="tags">
              <el-tag :type="getStatusTag(selectedPrisoner.status).type" size="small">
                {{ getStatusTag(selectedPrisoner.status).text }}
              </el-tag>
              <el-tag :type="getDangerLevelTag(selectedPrisoner.dangerLevel).type" size="small">
                {{ getDangerLevelTag(selectedPrisoner.dangerLevel).text }}
              </el-tag>
              <el-tag type="info" size="small">{{ getAreaName(selectedPrisoner.areaId) }}</el-tag>
            </div>
          </div>
        </div>

        <el-descriptions :column="1" border size="default" class="detail-descriptions">
          <el-descriptions-item label="编号">{{ selectedPrisoner.prisonerNumber }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ selectedPrisoner.gender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ calculateAge(selectedPrisoner.birthDate) }}岁</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ selectedPrisoner.idCard }}</el-descriptions-item>
          <el-descriptions-item label="籍贯">{{ selectedPrisoner.nativePlace || '-' }}</el-descriptions-item>
          <el-descriptions-item label="文化程度">{{ selectedPrisoner.educationLevel || '-' }}</el-descriptions-item>
          <el-descriptions-item label="婚姻状况">{{ selectedPrisoner.maritalStatus || '-' }}</el-descriptions-item>
          <el-descriptions-item label="职业">{{ selectedPrisoner.occupation || '无' }}</el-descriptions-item>
          <el-descriptions-item label="健康状况">
            <el-tag v-if="selectedPrisoner.healthStatus" type="warning" size="small">
              {{ selectedPrisoner.healthStatus }}
            </el-tag>
            <span v-else>健康</span>
          </el-descriptions-item>
          <el-descriptions-item label="监区">{{ getAreaName(selectedPrisoner.areaId) }}</el-descriptions-item>
          <el-descriptions-item label="监舍号">{{ selectedPrisoner.cellNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="罪名">{{ selectedPrisoner.crimeType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="刑期">{{ formatSentence(selectedPrisoner.sentenceTerm) }}</el-descriptions-item>
          <el-descriptions-item label="入狱日期">{{ selectedPrisoner.entryDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="释放日期">{{ selectedPrisoner.releaseDate || '无期徒刑' }}</el-descriptions-item>
          <el-descriptions-item label="剩余刑期">{{ getRemainingDays(selectedPrisoner.releaseDate) }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-remark" v-if="selectedPrisoner.remark">
          <div class="remark-title">
            <el-icon><Warning /></el-icon>
            <span>重要备注</span>
          </div>
          <div class="remark-content">{{ selectedPrisoner.remark }}</div>
        </div>

        <div class="detail-actions">
          <el-button type="primary" @click="handleEdit(selectedPrisoner)">编辑信息</el-button>
          <el-button @click="closeDrawer">关闭</el-button>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="编号" prop="prisonerNumber">
              <el-input v-model="form.prisonerNumber" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender" style="width: 100%">
                <el-option v-for="opt in genderOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="form.birthDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="form.idCard" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="籍贯">
              <el-input v-model="form.nativePlace" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="监区" prop="areaId">
              <el-select v-model="form.areaId" style="width: 100%">
                <el-option
                  v-for="area in prisonAreaList"
                  :key="area.id"
                  :label="area.areaName"
                  :value="area.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="监舍号" prop="cellNumber">
              <el-input v-model="form.cellNumber" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="罪名">
              <el-input v-model="form.crimeType" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="刑期(月)">
              <el-input-number v-model="form.sentenceTerm" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="危险等级">
              <el-select v-model="form.dangerLevel" style="width: 100%">
                <el-option v-for="opt in dangerLevelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="入狱日期">
              <el-date-picker v-model="form.entryDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="释放日期">
              <el-date-picker v-model="form.releaseDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="文化程度">
              <el-select v-model="form.educationLevel" style="width: 100%" clearable>
                <el-option v-for="opt in educationOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="婚姻状况">
              <el-select v-model="form.maritalStatus" style="width: 100%" clearable>
                <el-option v-for="opt in maritalOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="职业">
          <el-input v-model="form.occupation" />
        </el-form-item>
        <el-form-item label="健康状况">
          <el-input v-model="form.healthStatus" placeholder="如：高血压、糖尿病等" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="特殊情况说明" />
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
.prisoner-list-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  font-size: 18px;
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.ml-5px {
  margin-left: 5px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.keyword-input {
  width: 260px;
}

.filter-select {
  width: 140px;
}

.advanced-filter-panel {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 6px;
  margin-bottom: 16px;
}

.age-separator {
  margin: 0 8px;
  color: #909399;
}

.expand-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 8px 0;
}

.info-item {
  font-size: 13px;
  color: #606266;
}

.info-item .label {
  color: #909399;
}

.health-normal {
  color: #67c23a;
  font-size: 12px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-drawer {
  padding: 0 10px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #667eea);
  color: #fff;
  font-size: 28px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

.basic-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #303133;
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-descriptions {
  margin-bottom: 20px;
}

.detail-remark {
  background: #fdf6ec;
  border: 1px solid #f5dab1;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 20px;
}

.remark-title {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e6a23c;
  font-weight: bold;
  margin-bottom: 8px;
}

.remark-content {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}

.detail-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
