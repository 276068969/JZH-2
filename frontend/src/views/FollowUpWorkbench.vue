<script setup lang="ts">
import { reactive, ref, computed, onMounted, watch } from 'vue'
import { get, post } from '@/utils/request'
import { ElMessage, type FormInstance } from 'element-plus'

interface FollowUpRecord {
  id: number
  prisonerId: number
  prisonerNumber: string
  prisonerName: string
  gender: string
  areaId: number
  areaName: string
  cellId: number
  cellNumber: string
  dangerLevel: string
  prisonerStatus: string

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

  missedFollowUpCount: number
  isKeyAttention: boolean
  keyAttentionReason: string

  daysUntilFollowUp: number
  daysOverdue: number
}

interface FollowUpStats {
  todayPending: number
  weekPending: number
  monthPending: number
  overdue: number
  consecutiveMissed: number
  stillTreating: number
  keyAttention: number
  completed: number
  totalWithFollowUp: number
}

const stats = ref<FollowUpStats>({
  todayPending: 0,
  weekPending: 0,
  monthPending: 0,
  overdue: 0,
  consecutiveMissed: 0,
  stillTreating: 0,
  keyAttention: 0,
  completed: 0,
  totalWithFollowUp: 0
})

const allRecords = ref<FollowUpRecord[]>([])
const totalRecords = ref(0)
const loading = ref(false)
const tableLoading = ref(false)
const apiError = ref('')

const filterForm = reactive({
  keyword: '',
  followUpStatus: '',
  followUpStartDate: '',
  followUpEndDate: '',
  dangerLevel: '',
  onlyKeyAttention: false
})

const followUpStatusOptions = [
  { label: '全部状态', value: '' },
  { label: '待复诊', value: 'PENDING' },
  { label: '已过期', value: 'OVERDUE' },
  { label: '未复诊', value: 'MISSED' },
  { label: '已复诊', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const dangerLevelOptions = [
  { label: '全部等级', value: '' },
  { label: '低危', value: 'LOW' },
  { label: '中危', value: 'MEDIUM' },
  { label: '高危', value: 'HIGH' },
  { label: '极高危', value: 'EXTREME' }
]

const followUpStatusMap: Record<string, { text: string; type: string }> = {
  PENDING: { text: '待复诊', type: 'primary' },
  OVERDUE: { text: '已过期', type: 'danger' },
  MISSED: { text: '未复诊', type: 'warning' },
  COMPLETED: { text: '已复诊', type: 'success' },
  CANCELLED: { text: '已取消', type: 'info' }
}

const dangerLevelMap: Record<string, string> = {
  LOW: '低危',
  MEDIUM: '中危',
  HIGH: '高危',
  EXTREME: '极高危'
}

const dangerLevelColor: Record<string, string> = {
  LOW: 'success',
  MEDIUM: 'warning',
  HIGH: 'danger',
  EXTREME: 'danger'
}

const medicalTypeMap: Record<string, string> = {
  PHYSICAL: '体检',
  OUTPATIENT: '门诊',
  EMERGENCY: '急诊',
  HOSPITALIZATION: '住院',
  PSYCHOLOGICAL: '心理'
}

const resultMap: Record<string, string> = {
  RECOVERED: '已治愈',
  TREATING: '治疗中',
  TRANSFERRED: '转院',
  DECEASED: '已故'
}

const currentPage = ref(1)
const pageSize = ref(10)
const activeFilter = ref<string | null>(null)

function getRowClassName({ row }: { row: FollowUpRecord }) {
  return row.isKeyAttention ? 'key-row' : ''
}

const summaryCards = computed(() => [
  { key: 'TODAY', label: '今日待复诊', color: '#f56c6c', icon: 'Clock', count: stats.value.todayPending, desc: '今日需处理' },
  { key: 'WEEK', label: '7天内待复诊', color: '#e6a23c', icon: 'Calendar', count: stats.value.weekPending, desc: '本周需跟进' },
  { key: 'MONTH', label: '30天内待复诊', color: '#409eff', icon: 'Date', count: stats.value.monthPending, desc: '本月计划' },
  { key: 'OVERDUE', label: '已过期未处理', color: '#f56c6c', icon: 'Warning', count: stats.value.overdue, desc: '需尽快处理' },
  { key: 'MISSED', label: '连续未复诊人员', color: '#909399', icon: 'UserFilled', count: stats.value.consecutiveMissed, desc: '重点关注' },
  { key: 'TREATING', label: '仍在治疗中', color: '#67c23a', icon: 'FirstAidKit', count: stats.value.stillTreating, desc: '持续跟进' },
  { key: 'KEY', label: '重点关注对象', color: '#8e44ad', icon: 'StarFilled', count: stats.value.keyAttention, desc: '不可遗漏' },
  { key: 'DONE', label: '已完成复诊', color: '#909399', icon: 'CircleCheck', count: stats.value.completed, desc: '累计完成' }
])

function handleCardClick(key: string) {
  activeFilter.value = activeFilter.value === key ? null : key
  currentPage.value = 1
  fetchWorkbench()
}

function handleFilter() {
  currentPage.value = 1
  fetchWorkbench()
}

function handleReset() {
  filterForm.keyword = ''
  filterForm.followUpStatus = ''
  filterForm.followUpStartDate = ''
  filterForm.followUpEndDate = ''
  filterForm.dangerLevel = ''
  filterForm.onlyKeyAttention = false
  activeFilter.value = null
  currentPage.value = 1
  fetchWorkbench()
}

function getStatusTag(status: string) {
  return followUpStatusMap[status] || { text: status || '-', type: 'info' }
}

const markDialogVisible = ref(false)
const markDialogTitle = ref('')
const markFormRef = ref<FormInstance>()
const markActionType = ref<'COMPLETE' | 'MISS' | 'CANCEL'>('COMPLETE')
const currentRecord = ref<FollowUpRecord | null>(null)

const markForm = reactive({
  medicalRecordId: 0,
  followUpStatus: '',
  actualFollowUpDate: '',
  followUpResult: '',
  followUpRemark: '',
  nextFollowUpDate: ''
})

const markLoading = ref(false)

function openMarkDialog(row: FollowUpRecord, action: 'COMPLETE' | 'MISS' | 'CANCEL') {
  currentRecord.value = row
  markActionType.value = action

  Object.assign(markForm, {
    medicalRecordId: row.id,
    followUpStatus: action === 'COMPLETE' ? 'COMPLETED' : action === 'MISS' ? 'MISSED' : 'CANCELLED',
    actualFollowUpDate: action === 'COMPLETE' ? formatDate(new Date()) : '',
    followUpResult: '',
    followUpRemark: action === 'MISS' ? (row.followUpRemark || '未按时复诊') : '',
    nextFollowUpDate: ''
  })

  markDialogTitle.value = action === 'COMPLETE'
    ? `标记已复诊 - ${row.prisonerName}`
    : action === 'MISS'
    ? `标记未复诊 - ${row.prisonerName}`
    : `取消复诊计划 - ${row.prisonerName}`

  markDialogVisible.value = true
}

function formatDate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

async function handleMarkSubmit() {
  const valid = await markFormRef.value?.validate().catch(() => false)
  if (!valid) return

  markLoading.value = true
  try {
    await post('/follow-up/mark', markForm)
    ElMessage.success('复诊状态已保存')
    markDialogVisible.value = false
    await fetchData()
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '保存失败'
    ElMessage.error('操作失败：' + msg)
  } finally {
    markLoading.value = false
  }
}

async function fetchStats() {
  loading.value = true
  apiError.value = ''
  try {
    const res = await get('/follow-up/stats')
    if (res && res.data && res.data.code === 200 && res.data.data) {
      stats.value = res.data.data
    } else {
      ElMessage.error('统计数据加载异常：' + (res?.data?.message || '返回格式不正确'))
    }
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '网络错误'
    apiError.value = msg
    ElMessage.error('加载统计数据失败：' + msg + '（请确认后端服务已启动且数据库已初始化）')
  } finally {
    loading.value = false
  }
}

async function fetchWorkbench() {
  tableLoading.value = true
  try {
    const params: Record<string, any> = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (filterForm.keyword) params.keyword = filterForm.keyword
    if (filterForm.followUpStatus) params.followUpStatus = filterForm.followUpStatus
    if (filterForm.followUpStartDate) params.followUpStartDate = filterForm.followUpStartDate
    if (filterForm.followUpEndDate) params.followUpEndDate = filterForm.followUpEndDate
    if (filterForm.dangerLevel) params.dangerLevel = filterForm.dangerLevel
    if (filterForm.onlyKeyAttention) params.onlyKeyAttention = filterForm.onlyKeyAttention
    if (activeFilter.value) params.activeFilter = activeFilter.value

    const res = await get('/follow-up/workbench', params)
    if (res && res.data && res.data.code === 200 && res.data.data) {
      const pageData = res.data.data
      allRecords.value = pageData.records || []
      totalRecords.value = pageData.total || 0
    } else {
      ElMessage.error('列表数据加载异常：' + (res?.data?.message || '返回格式不正确'))
      allRecords.value = []
      totalRecords.value = 0
    }
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '网络错误'
    ElMessage.error('加载列表失败：' + msg + '（请确认后端服务已启动且数据库已初始化）')
    allRecords.value = []
    totalRecords.value = 0
  } finally {
    tableLoading.value = false
  }
}

async function fetchData() {
  await Promise.all([fetchStats(), fetchWorkbench()])
}

watch([currentPage, pageSize], () => {
  fetchWorkbench()
})

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="workbench-page">
    <div class="page-header">
      <h2>医疗复诊工作台</h2>
      <el-tag v-if="apiError" type="danger" size="small" style="margin-left:12px">
        ⚠ 后端连接异常：{{ apiError }}
      </el-tag>
    </div>

    <div class="summary-grid">
      <div
        v-for="card in summaryCards"
        :key="card.key"
        class="summary-card"
        :class="{ active: activeFilter === card.key }"
        @click="handleCardClick(card.key)"
      >
        <div class="card-icon" :style="{ background: card.color + '15', color: card.color }">
          <el-icon :size="26">
            <component :is="card.icon" />
          </el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">{{ card.label }}</div>
          <div class="card-count" :style="{ color: card.color }">{{ card.count }}</div>
          <div class="card-desc">{{ card.desc }}</div>
        </div>
        <div class="card-badge" v-if="card.count > 0 && ['OVERDUE','MISSED','KEY'].includes(card.key)" :style="{ background: card.color }">!</div>
      </div>
    </div>

    <div class="filter-bar">
      <el-input v-model="filterForm.keyword" placeholder="搜索姓名/编号/诊断/医生" clearable class="search-input" @keyup.enter="handleFilter" />
      <el-select v-model="filterForm.followUpStatus" placeholder="复诊状态" clearable style="width:150px" @change="handleFilter">
        <el-option v-for="opt in followUpStatusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-date-picker
        v-model="filterForm.followUpStartDate"
        type="date"
        placeholder="复诊日期起"
        value-format="YYYY-MM-DD"
        style="width:150px"
        @change="handleFilter"
      />
      <el-date-picker
        v-model="filterForm.followUpEndDate"
        type="date"
        placeholder="复诊日期止"
        value-format="YYYY-MM-DD"
        style="width:150px"
        @change="handleFilter"
      />
      <el-select v-model="filterForm.dangerLevel" placeholder="危险等级" clearable style="width:130px" @change="handleFilter">
        <el-option v-for="opt in dangerLevelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-checkbox v-model="filterForm.onlyKeyAttention" @change="handleFilter">仅看重点对象</el-checkbox>
      <el-button type="primary" :loading="loading" @click="fetchData">刷新</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-alert
      v-if="apiError"
      type="error"
      :closable="false"
      style="margin-bottom: 16px"
    >
      <template #title>
        无法连接后端服务，请确认：
        <ol style="margin: 6px 0 0 20px; line-height: 1.8">
          <li>已执行 <code>docker-compose up -d</code> 或本地启动后端（端口 8088）</li>
          <li>MySQL 已启动且初始化脚本 <code>init.sql</code> 已执行</li>
          <li>登录账号使用真实用户名密码（如 doctor/doctor123）</li>
        </ol>
      </template>
    </el-alert>

    <el-table
      v-loading="tableLoading"
      :data="allRecords"
      border
      stripe
      style="width:100%"
      empty-text="暂无符合条件的复诊记录（请检查筛选条件，或确认数据库中已存在医疗复诊数据）"
      :row-class-name="getRowClassName"
    >
      <el-table-column label="服刑人员" width="200" fixed="left">
        <template #default="{ row }">
          <div class="prisoner-cell">
            <div class="prisoner-main">
              <span class="prisoner-name">{{ row.prisonerName }}</span>
              <el-tag v-if="row.isKeyAttention" type="danger" effect="dark" size="small" class="key-tag">重点</el-tag>
            </div>
            <div class="prisoner-sub">{{ row.prisonerNumber }} · {{ row.gender }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="areaName" label="监区" width="100" />
      <el-table-column prop="cellNumber" label="监舍" width="100" />
      <el-table-column label="危险等级" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.dangerLevel" :type="dangerLevelColor[row.dangerLevel] || 'info'" size="small" effect="dark">
            {{ dangerLevelMap[row.dangerLevel] || row.dangerLevel }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="诊断" width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.diagnosis || '-' }}</template>
      </el-table-column>
      <el-table-column label="就诊类型" width="80">
        <template #default="{ row }">{{ medicalTypeMap[row.medicalType] || row.medicalType || '-' }}</template>
      </el-table-column>
      <el-table-column label="治疗结果" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.result" :type="row.result === 'RECOVERED' ? 'success' : row.result === 'TREATING' ? 'warning' : 'info'" size="small">
            {{ resultMap[row.result] || row.result }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="就诊日期" width="100">
        <template #default="{ row }">{{ row.recordDate || '-' }}</template>
      </el-table-column>
      <el-table-column label="计划复诊" width="100">
        <template #default="{ row }">{{ row.followUpDate || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.followUpStatus).type" size="small">
            {{ getStatusTag(row.followUpStatus).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间提示" width="120">
        <template #default="{ row }">
          <template v-if="row.followUpStatus === 'COMPLETED'">
            <span class="text-success">于 {{ row.actualFollowUpDate }} 完成</span>
          </template>
          <template v-else-if="row.daysOverdue && row.daysOverdue > 0">
            <span class="text-danger">已过期 {{ row.daysOverdue }} 天</span>
          </template>
          <template v-else-if="row.daysUntilFollowUp === 0">
            <span class="text-danger">今日需复诊</span>
          </template>
          <template v-else>
            <span class="text-primary">还有 {{ row.daysUntilFollowUp }} 天</span>
          </template>
        </template>
      </el-table-column>
      <el-table-column label="未复诊次数" width="90">
        <template #default="{ row }">
          <span v-if="row.missedFollowUpCount > 0" :class="row.missedFollowUpCount >= 2 ? 'text-danger bold' : 'text-warning'">
            {{ row.missedFollowUpCount }} 次
          </span>
          <span v-else class="text-muted">0</span>
        </template>
      </el-table-column>
      <el-table-column label="关注原因" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.isKeyAttention" class="text-danger">{{ row.keyAttentionReason }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <template v-if="row.followUpStatus !== 'COMPLETED' && row.followUpStatus !== 'CANCELLED'">
            <el-button type="success" link size="small" @click="openMarkDialog(row, 'COMPLETE')">标记已复诊</el-button>
            <el-button type="warning" link size="small" @click="openMarkDialog(row, 'MISS')">标记未复诊</el-button>
            <el-button type="info" link size="small" @click="openMarkDialog(row, 'CANCEL')">取消</el-button>
          </template>
          <template v-else>
            <el-tooltip v-if="row.followUpResult" :content="row.followUpResult" placement="top">
              <el-button type="primary" link size="small">查看结果</el-button>
            </el-tooltip>
            <span v-else class="text-muted">已处理</span>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalRecords"
        layout="total, sizes, prev, pager, next"
        background
      />
    </div>

    <el-dialog v-model="markDialogVisible" :title="markDialogTitle" width="560px" destroy-on-close>
      <el-form ref="markFormRef" :model="markForm" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="服刑人员">
              <el-input :value="currentRecord?.prisonerName + ' / ' + currentRecord?.prisonerNumber" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原诊断">
              <el-input :value="currentRecord?.diagnosis" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="计划复诊日">
              <el-input :value="currentRecord?.followUpDate" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="复诊状态" required>
              <el-select v-model="markForm.followUpStatus" style="width:100%" disabled>
                <el-option label="已复诊" value="COMPLETED" />
                <el-option label="未复诊" value="MISSED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="markActionType === 'COMPLETE'">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="实际复诊日" required>
                <el-date-picker
                  v-model="markForm.actualFollowUpDate"
                  type="date"
                  style="width:100%"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择实际复诊日期"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="下次复诊日">
                <el-date-picker
                  v-model="markForm.nextFollowUpDate"
                  type="date"
                  style="width:100%"
                  value-format="YYYY-MM-DD"
                  placeholder="如需继续复诊请填写"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="复诊结果">
            <el-input
              v-model="markForm.followUpResult"
              type="textarea"
              :rows="2"
              placeholder="请输入复诊后的诊断或治疗建议，将保存到数据库"
            />
          </el-form-item>
        </template>

        <el-form-item label="备注">
          <el-input
            v-model="markForm.followUpRemark"
            type="textarea"
            :rows="2"
            :placeholder="markActionType === 'MISS' ? '请说明未复诊的原因或后续跟进计划，将保存到数据库' : '其他需要说明的情况，将保存到数据库'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="markDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="markLoading" @click="handleMarkSubmit">保存到数据库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.workbench-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}

.page-header h2 {
  font-size: 18px;
  color: #303133;
  margin: 0;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 20px;
}

.summary-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  border: 2px solid transparent;
  background: #fafbfc;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.summary-card.active {
  border-color: #409eff;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8f4fd 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.18);
}

.card-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
}

.card-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.card-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.card-count {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.15;
}

.card-desc {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 2px;
}

.card-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  width: 260px;
}

.prisoner-cell {
  display: flex;
  flex-direction: column;
}

.prisoner-main {
  display: flex;
  align-items: center;
  gap: 6px;
}

.prisoner-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.key-tag {
  transform: scale(0.85);
}

.prisoner-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

:deep(.key-row) {
  background-color: #fef0f0 !important;
}

:deep(.el-table--striped .key-row.el-table__row--striped td) {
  background-color: #fef0f0 !important;
}

.text-success { color: #67c23a; font-size: 12px; }
.text-danger { color: #f56c6c; }
.text-primary { color: #409eff; font-size: 12px; }
.text-warning { color: #e6a23c; }
.text-muted { color: #c0c4cc; font-size: 12px; }
.bold { font-weight: 700; }

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
