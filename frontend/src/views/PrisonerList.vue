<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { post, get, del, put } from '@/utils/request'

interface Prisoner {
  id: number
  name: string
  gender: string
  age: number
  prisonerNumber: string
  prisonArea: string
  areaId: number
  cellNumber: string
  crimeType: string
  sentence: string
  sentenceTerm: number
  entryDate: string
  releaseDate: string
  status: string
  dangerLevel: string
  idCard: string
  nativePlace: string
  educationLevel: string
  maritalStatus: string
  occupation: string
  healthStatus: string
  remark: string
}

interface PrisonArea {
  id: number
  areaName: string
  areaCode: string
}

const mockData: Prisoner[] = [
  { id: 1, name: '张三', gender: '男', age: 35, prisonerNumber: 'P2024001', prisonArea: 'A区', areaId: 1, cellNumber: 'A-101', crimeType: '盗窃罪', sentence: '5年', sentenceTerm: 60, entryDate: '2024-01-15', releaseDate: '2029-01-14', status: '在押', dangerLevel: 'LOW', idCard: '110101199001011234', nativePlace: '北京市', educationLevel: '高中', maritalStatus: '已婚', occupation: '无业', healthStatus: '良好', remark: '表现良好' },
  { id: 2, name: '李四', gender: '男', age: 42, prisonerNumber: 'P2024002', prisonArea: 'A区', areaId: 1, cellNumber: 'A-102', crimeType: '诈骗罪', sentence: '8年', sentenceTerm: 96, entryDate: '2024-02-20', releaseDate: '2032-02-19', status: '在押', dangerLevel: 'MEDIUM', idCard: '310101198205155678', nativePlace: '上海市', educationLevel: '大专', maritalStatus: '离异', occupation: '公司职员', healthStatus: '高血压', remark: '需定期体检' },
  { id: 3, name: '王五', gender: '男', age: 28, prisonerNumber: 'P2024003', prisonArea: 'B区', areaId: 2, cellNumber: 'B-201', crimeType: '故意伤害', sentence: '3年', sentenceTerm: 36, entryDate: '2024-03-10', releaseDate: '2027-03-09', status: '在押', dangerLevel: 'HIGH', idCard: '440101199608209012', nativePlace: '广州市', educationLevel: '初中', maritalStatus: '未婚', occupation: '个体', healthStatus: '良好', remark: '有暴力倾向，需重点关注' },
  { id: 4, name: '赵六', gender: '女', age: 31, prisonerNumber: 'P2024004', prisonArea: 'C区', areaId: 3, cellNumber: 'C-301', crimeType: '贪污罪', sentence: '10年', sentenceTerm: 120, entryDate: '2024-01-05', releaseDate: '2034-01-04', status: '在押', dangerLevel: 'LOW', idCard: '510101199303103456', nativePlace: '成都市', educationLevel: '本科', maritalStatus: '已婚', occupation: '公务员', healthStatus: '良好', remark: '表现良好，积极参加改造' },
  { id: 5, name: '孙七', gender: '男', age: 45, prisonerNumber: 'P2024005', prisonArea: 'B区', areaId: 2, cellNumber: 'B-202', crimeType: '抢劫罪', sentence: '6年', sentenceTerm: 72, entryDate: '2024-04-18', releaseDate: '2030-04-17', status: '在押', dangerLevel: 'HIGH', idCard: '420101197906257890', nativePlace: '武汉市', educationLevel: '小学', maritalStatus: '已婚', occupation: '无业', healthStatus: '糖尿病', remark: '高危，需重点监控' },
  { id: 6, name: '周八', gender: '男', age: 38, prisonerNumber: 'P2024006', prisonArea: 'A区', areaId: 1, cellNumber: 'A-103', crimeType: '走私罪', sentence: '7年', sentenceTerm: 84, entryDate: '2024-05-22', releaseDate: '2031-05-21', status: '在押', dangerLevel: 'MEDIUM', idCard: '330101198609122345', nativePlace: '杭州市', educationLevel: '高中', maritalStatus: '已婚', occupation: '商人', healthStatus: '良好', remark: '' },
  { id: 7, name: '吴九', gender: '女', age: 26, prisonerNumber: 'P2024007', prisonArea: 'C区', areaId: 3, cellNumber: 'C-302', crimeType: '贩毒罪', sentence: '15年', sentenceTerm: 180, entryDate: '2024-06-08', releaseDate: '2039-06-07', status: '在押', dangerLevel: 'HIGH', idCard: '530101199802146789', nativePlace: '昆明市', educationLevel: '初中', maritalStatus: '未婚', occupation: '无业', healthStatus: '良好', remark: '涉毒人员，需重点关注' },
  { id: 8, name: '郑十', gender: '男', age: 33, prisonerNumber: 'P2024008', prisonArea: 'B区', areaId: 2, cellNumber: 'B-203', crimeType: '非法经营', sentence: '4年', sentenceTerm: 48, entryDate: '2024-07-12', releaseDate: '2028-07-11', status: '在押', dangerLevel: 'LOW', idCard: '320101199107304567', nativePlace: '南京市', educationLevel: '大专', maritalStatus: '已婚', occupation: '个体', healthStatus: '良好', remark: '' },
  { id: 9, name: '陈十一', gender: '男', age: 50, prisonerNumber: 'P2024009', prisonArea: 'A区', areaId: 1, cellNumber: 'A-104', crimeType: '受贿罪', sentence: '12年', sentenceTerm: 144, entryDate: '2023-08-15', releaseDate: '2035-08-14', status: '在押', dangerLevel: 'LOW', idCard: '110102197403088901', nativePlace: '北京市', educationLevel: '研究生', maritalStatus: '已婚', occupation: '公务员', healthStatus: '心脏病', remark: '需长期服药' },
  { id: 10, name: '林十二', gender: '女', age: 40, prisonerNumber: 'P2024010', prisonArea: 'C区', areaId: 3, cellNumber: 'C-303', crimeType: '故意杀人', sentence: '无期徒刑', sentenceTerm: 999, entryDate: '2022-11-20', releaseDate: '', status: '在押', dangerLevel: 'HIGH', idCard: '440301198405125678', nativePlace: '深圳市', educationLevel: '本科', maritalStatus: '离异', occupation: '公司经理', healthStatus: '良好', remark: '重刑犯，需严格监控' },
  { id: 11, name: '黄十三', gender: '男', age: 36, prisonerNumber: 'P2024011', prisonArea: 'D区', areaId: 4, cellNumber: 'D-101', crimeType: '寻衅滋事', sentence: '2年', sentenceTerm: 24, entryDate: '2024-09-01', releaseDate: '2026-08-31', status: '禁闭', dangerLevel: 'HIGH', idCard: '610101198810207890', nativePlace: '西安市', educationLevel: '高中', maritalStatus: '未婚', occupation: '无业', healthStatus: '良好', remark: '多次违反监规，关禁闭' },
  { id: 12, name: '刘十四', gender: '男', age: 55, prisonerNumber: 'P2024012', prisonArea: 'A区', areaId: 1, cellNumber: 'A-105', crimeType: '合同诈骗', sentence: '9年', sentenceTerm: 108, entryDate: '2023-12-10', releaseDate: '2032-12-09', status: '在押', dangerLevel: 'MEDIUM', idCard: '370101196906152345', nativePlace: '济南市', educationLevel: '本科', maritalStatus: '已婚', occupation: '商人', healthStatus: '高血压、糖尿病', remark: '老年犯，需特殊照顾' }
]

const prisonAreaList: PrisonArea[] = [
  { id: 1, areaName: 'A区', areaCode: 'AREA-A' },
  { id: 2, areaName: 'B区', areaCode: 'AREA-B' },
  { id: 3, areaName: 'C区', areaCode: 'AREA-C' },
  { id: 4, areaName: 'D区', areaCode: 'AREA-D' }
]

const dangerLevelOptions = [
  { label: '低危', value: 'LOW' },
  { label: '中危', value: 'MEDIUM' },
  { label: '高危', value: 'HIGH' }
]

const statusOptions = [
  { label: '在押', value: '在押' },
  { label: '禁闭', value: '禁闭' },
  { label: '就医', value: '就医' },
  { label: '调监', value: '调监' },
  { label: '已释放', value: '已释放' }
]

const tableData = ref<Prisoner[]>(mockData)
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
  id: 0, name: '', gender: '男', age: 0, prisonerNumber: '',
  prisonArea: '', areaId: 0, cellNumber: '', crimeType: '', sentence: '',
  sentenceTerm: 0, entryDate: '', releaseDate: '', status: '在押',
  dangerLevel: 'LOW', idCard: '', nativePlace: '', educationLevel: '',
  maritalStatus: '', occupation: '', healthStatus: '', remark: ''
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

const filteredData = computed(() => {
  let result = tableData.value

  if (searchForm.keyword) {
    const kw = searchForm.keyword.toLowerCase()
    result = result.filter(item =>
      item.name.toLowerCase().includes(kw) ||
      item.prisonerNumber.toLowerCase().includes(kw) ||
      item.idCard.includes(kw)
    )
  }

  if (searchForm.areaId) {
    result = result.filter(item => item.areaId === searchForm.areaId)
  }

  if (searchForm.dangerLevel) {
    result = result.filter(item => item.dangerLevel === searchForm.dangerLevel)
  }

  if (searchForm.status) {
    result = result.filter(item => item.status === searchForm.status)
  }

  if (searchForm.gender) {
    result = result.filter(item => item.gender === searchForm.gender)
  }

  if (searchForm.crimeType) {
    result = result.filter(item => item.crimeType.includes(searchForm.crimeType))
  }

  if (searchForm.minAge) {
    result = result.filter(item => item.age >= searchForm.minAge!)
  }

  if (searchForm.maxAge) {
    result = result.filter(item => item.age <= searchForm.maxAge!)
  }

  total.value = result.length
  return result
})

const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

function getDangerLevelTag(level: string) {
  const map: Record<string, { text: string; type: string }> = {
    LOW: { text: '低危', type: 'success' },
    MEDIUM: { text: '中危', type: 'warning' },
    HIGH: { text: '高危', type: 'danger' }
  }
  return map[level] || { text: '未知', type: 'info' }
}

function getStatusTag(status: string) {
  const map: Record<string, { text: string; type: string }> = {
    '在押': { text: '在押', type: 'danger' },
    '禁闭': { text: '禁闭', type: 'warning' },
    '就医': { text: '就医', type: 'warning' },
    '调监': { text: '调监', type: 'primary' },
    '已释放': { text: '已释放', type: 'success' }
  }
  return map[status] || { text: status, type: 'info' }
}

function handleSearch() {
  currentPage.value = 1
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
}

function handleViewDetail(row: Prisoner) {
  selectedPrisoner.value = row
  drawerVisible.value = true
}

function handleAdd() {
  dialogTitle.value = '新增服刑人员'
  Object.assign(form, {
    id: 0, name: '', gender: '男', age: 0, prisonerNumber: '',
    prisonArea: '', areaId: 0, cellNumber: '', crimeType: '', sentence: '',
    sentenceTerm: 0, entryDate: '', releaseDate: '', status: '在押',
    dangerLevel: 'LOW', idCard: '', nativePlace: '', educationLevel: '',
    maritalStatus: '', occupation: '', healthStatus: '', remark: ''
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
    .then(() => {
      tableData.value = tableData.value.filter((item) => item.id !== row.id)
      ElMessage.success('删除成功')
    })
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  if (form.id === 0) {
    form.id = Math.max(...tableData.value.map((i) => i.id), 0) + 1
    const area = prisonAreaList.find(a => a.id === form.areaId)
    form.prisonArea = area?.areaName || ''
    tableData.value.unshift({ ...form })
    ElMessage.success('新增成功')
  } else {
    const idx = tableData.value.findIndex((i) => i.id === form.id)
    if (idx > -1) {
      const area = prisonAreaList.find(a => a.id === form.areaId)
      form.prisonArea = area?.areaName || ''
      tableData.value[idx] = { ...form }
    }
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  loading.value = false
}

function closeDrawer() {
  drawerVisible.value = false
  selectedPrisoner.value = null
}

function getRemainingDays(releaseDate: string) {
  if (!releaseDate) return '无期'
  const today = new Date()
  const release = new Date(releaseDate)
  const diff = Math.ceil((release.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  if (diff <= 0) return '已到期'
  return `${diff}天`
}
</script>

<template>
  <div class="prisoner-list-page">
    <div class="page-header">
      <h2>服刑人员管理</h2>
      <el-button type="primary" link @click="advancedFilterVisible = !advancedFilterVisible">
        {{ advancedFilterVisible ? '收起筛选' : '高级筛选' }}
        <el-icon class="ml-5px"><component :is="advancedFilterVisible ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
      </el-button>
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

    <div class="filter-summary" v-if="filteredData.length !== tableData.length">
      <el-tag type="info">当前筛选条件下共 <b>{{ filteredData.length }}</b> 条记录</el-tag>
    </div>

    <el-table
      :data="pagedData"
      border
      stripe
      style="width: 100%"
      v-loading="tableLoading"
      highlight-current-row
    >
      <el-table-column type="expand" width="50">
        <template #default="{ row }">
          <div class="expand-info">
            <div class="info-item"><span class="label">身份证号：</span>{{ row.idCard }}</div>
            <div class="info-item"><span class="label">籍贯：</span>{{ row.nativePlace }}</div>
            <div class="info-item"><span class="label">文化程度：</span>{{ row.educationLevel }}</div>
            <div class="info-item"><span class="label">健康状况：</span>{{ row.healthStatus }}</div>
            <div class="info-item"><span class="label">剩余刑期：</span>{{ getRemainingDays(row.releaseDate) }}</div>
            <div class="info-item"><span class="label">备注：</span>{{ row.remark || '无' }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="prisonerNumber" label="编号" width="110" />
      <el-table-column prop="name" label="姓名" width="80" />
      <el-table-column prop="gender" label="性别" width="60" />
      <el-table-column prop="age" label="年龄" width="60" />
      <el-table-column prop="prisonArea" label="监区" width="70" />
      <el-table-column prop="cellNumber" label="监舍号" width="90" />
      <el-table-column prop="crimeType" label="罪名" width="100" />
      <el-table-column prop="sentence" label="刑期" width="80" />
      <el-table-column label="危险等级" width="80">
        <template #default="{ row }">
          <el-tag :type="getDangerLevelTag(row.dangerLevel).type" size="small">
            {{ getDangerLevelTag(row.dangerLevel).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="70">
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
        :total="filteredData.length"
        layout="total, sizes, prev, pager, next, jumper"
        background
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
              <el-tag type="info" size="small">{{ selectedPrisoner.prisonArea }}</el-tag>
            </div>
          </div>
        </div>

        <el-descriptions :column="1" border size="default" class="detail-descriptions">
          <el-descriptions-item label="编号">{{ selectedPrisoner.prisonerNumber }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ selectedPrisoner.gender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ selectedPrisoner.age }}岁</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ selectedPrisoner.idCard }}</el-descriptions-item>
          <el-descriptions-item label="籍贯">{{ selectedPrisoner.nativePlace }}</el-descriptions-item>
          <el-descriptions-item label="文化程度">{{ selectedPrisoner.educationLevel }}</el-descriptions-item>
          <el-descriptions-item label="婚姻状况">{{ selectedPrisoner.maritalStatus }}</el-descriptions-item>
          <el-descriptions-item label="职业">{{ selectedPrisoner.occupation || '无' }}</el-descriptions-item>
          <el-descriptions-item label="健康状况">{{ selectedPrisoner.healthStatus }}</el-descriptions-item>
          <el-descriptions-item label="监区">{{ selectedPrisoner.prisonArea }}</el-descriptions-item>
          <el-descriptions-item label="监舍号">{{ selectedPrisoner.cellNumber }}</el-descriptions-item>
          <el-descriptions-item label="罪名">{{ selectedPrisoner.crimeType }}</el-descriptions-item>
          <el-descriptions-item label="刑期">{{ selectedPrisoner.sentence }}</el-descriptions-item>
          <el-descriptions-item label="入狱日期">{{ selectedPrisoner.entryDate }}</el-descriptions-item>
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
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="18" :max="100" style="width: 100%" />
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
            <el-form-item label="刑期">
              <el-input v-model="form.sentence" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="危险等级">
              <el-select v-model="form.dangerLevel">
                <el-option
                  v-for="item in dangerLevelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status">
                <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="入狱日期">
              <el-date-picker
                v-model="form.entryDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="释放日期">
              <el-date-picker
                v-model="form.releaseDate"
                type="date"
                placeholder="选择日期（无期可留空）"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="健康状况">
          <el-input v-model="form.healthStatus" />
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

.filter-summary {
  margin-bottom: 12px;
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
