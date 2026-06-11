<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { get, post } from '@/utils/request'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

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

const mockStats: FollowUpStats = {
  todayPending: 2,
  weekPending: 3,
  monthPending: 4,
  overdue: 5,
  consecutiveMissed: 2,
  stillTreating: 8,
  keyAttention: 4,
  completed: 1,
  totalWithFollowUp: 13
}

const mockRecords: FollowUpRecord[] = [
  {
    id: 14, prisonerId: 4, prisonerNumber: 'P20240004', prisonerName: '李某', gender: '女',
    areaId: 3, areaName: '女监区', cellId: 6, cellNumber: 'F001-101',
    dangerLevel: 'LOW', prisonerStatus: 'INCARCERATED',
    recordDate: '2026-06-09', diagnosis: '心理咨询', treatment: '抑郁情绪评估，建议继续心理疏导',
    hospital: '省精神卫生中心', doctorName: '陈医生', medicalType: 'PSYCHOLOGICAL',
    result: 'TREATING', medicine: '舍曲林50mg qd',
    followUpDate: '2026-06-10', followUpStatus: 'OVERDUE', actualFollowUpDate: '',
    followUpResult: '', followUpRemark: '昨日应复诊，已过期',
    missedFollowUpCount: 0, isKeyAttention: true, keyAttentionReason: '治疗未结束、治疗中且逾期未复诊',
    daysUntilFollowUp: 0, daysOverdue: 1
  },
  {
    id: 13, prisonerId: 2, prisonerNumber: 'P20240002', prisonerName: '钱某', gender: '男',
    areaId: 1, areaName: '男监一区', cellId: 2, cellNumber: 'M001-102',
    dangerLevel: 'MEDIUM', prisonerStatus: 'INCARCERATED',
    recordDate: '2026-06-05', diagnosis: '常规体检', treatment: '血压偏高，余未见异常',
    hospital: '监狱医务室', doctorName: '张医生', medicalType: 'PHYSICAL',
    result: 'TREATING', medicine: '继续服用降压药',
    followUpDate: '2026-06-11', followUpStatus: 'PENDING', actualFollowUpDate: '',
    followUpResult: '', followUpRemark: '今日待复诊',
    missedFollowUpCount: 0, isKeyAttention: false, keyAttentionReason: '',
    daysUntilFollowUp: 0, daysOverdue: 0
  },
  {
    id: 2, prisonerId: 2, prisonerNumber: 'P20240002', prisonerName: '钱某', gender: '男',
    areaId: 1, areaName: '男监一区', cellId: 2, cellNumber: 'M001-102',
    dangerLevel: 'MEDIUM', prisonerStatus: 'INCARCERATED',
    recordDate: '2024-05-15', diagnosis: '高血压病', treatment: '给予降压治疗，定期监测血压',
    hospital: '监狱医务室', doctorName: '张医生', medicalType: 'OUTPATIENT',
    result: 'TREATING', medicine: '硝苯地平控释片30mg qd',
    followUpDate: '2026-06-15', followUpStatus: 'PENDING', actualFollowUpDate: '',
    followUpResult: '', followUpRemark: '每月复诊监测血压',
    missedFollowUpCount: 0, isKeyAttention: false, keyAttentionReason: '',
    daysUntilFollowUp: 4, daysOverdue: 0
  },
  {
    id: 8, prisonerId: 8, prisonerNumber: 'P20240008', prisonerName: '王某', gender: '男',
    areaId: 1, areaName: '男监一区', cellId: 2, cellNumber: 'M001-102',
    dangerLevel: 'HIGH', prisonerStatus: 'INCARCERATED',
    recordDate: '2026-05-15', diagnosis: '偏头痛', treatment: '对症止痛治疗',
    hospital: '监狱医务室', doctorName: '张医生', medicalType: 'OUTPATIENT',
    result: 'TREATING', medicine: '布洛芬缓释胶囊0.3g prn',
    followUpDate: '2026-06-15', followUpStatus: 'PENDING', actualFollowUpDate: '',
    followUpResult: '', followUpRemark: '',
    missedFollowUpCount: 0, isKeyAttention: true, keyAttentionReason: '治疗未结束、高危人员',
    daysUntilFollowUp: 4, daysOverdue: 0
  },
  {
    id: 4, prisonerId: 4, prisonerNumber: 'P20240004', prisonerName: '李某', gender: '女',
    areaId: 3, areaName: '女监区', cellId: 6, cellNumber: 'F001-101',
    dangerLevel: 'LOW', prisonerStatus: 'INCARCERATED',
    recordDate: '2026-05-20', diagnosis: '糖尿病', treatment: '饮食控制+口服降糖药',
    hospital: '市第一人民医院', doctorName: '王医生', medicalType: 'OUTPATIENT',
    result: 'TREATING', medicine: '二甲双胍0.5g bid',
    followUpDate: '2026-06-20', followUpStatus: 'PENDING', actualFollowUpDate: '',
    followUpResult: '', followUpRemark: '需定期监测血糖',
    missedFollowUpCount: 0, isKeyAttention: false, keyAttentionReason: '',
    daysUntilFollowUp: 9, daysOverdue: 0
  },
  {
    id: 6, prisonerId: 6, prisonerNumber: 'P20240006', prisonerName: '吴某', gender: '男',
    areaId: 1, areaName: '男监一区', cellId: 1, cellNumber: 'M001-101',
    dangerLevel: 'LOW', prisonerStatus: 'INCARCERATED',
    recordDate: '2026-05-28', diagnosis: '过敏性鼻炎', treatment: '抗过敏治疗',
    hospital: '监狱医务室', doctorName: '张医生', medicalType: 'OUTPATIENT',
    result: 'TREATING', medicine: '氯雷他定10mg qn',
    followUpDate: '2026-06-28', followUpStatus: 'PENDING', actualFollowUpDate: '',
    followUpResult: '', followUpRemark: '',
    missedFollowUpCount: 0, isKeyAttention: false, keyAttentionReason: '',
    daysUntilFollowUp: 17, daysOverdue: 0
  },
  {
    id: 5, prisonerId: 5, prisonerNumber: 'P20240005', prisonerName: '周某', gender: '男',
    areaId: 4, areaName: '高度戒备区', cellId: 9, cellNumber: 'H001-ISO2',
    dangerLevel: 'HIGH', prisonerStatus: 'INCARCERATED',
    recordDate: '2026-04-10', diagnosis: '冠心病', treatment: '扩冠、抗血小板聚集治疗',
    hospital: '市中心医院', doctorName: '赵医生', medicalType: 'OUTPATIENT',
    result: 'TREATING', medicine: '阿司匹林100mg qd，单硝酸异山梨酯20mg bid',
    followUpDate: '2026-05-10', followUpStatus: 'MISSED', actualFollowUpDate: '',
    followUpResult: '', followUpRemark: '逾期未复诊，需重点关注',
    missedFollowUpCount: 2, isKeyAttention: true, keyAttentionReason: '连续2次未复诊、治疗未结束、治疗中且逾期未复诊、高危人员',
    daysUntilFollowUp: 0, daysOverdue: 32
  },
  {
    id: 61, prisonerId: 5, prisonerNumber: 'P20240005', prisonerName: '周某', gender: '男',
    areaId: 4, areaName: '高度戒备区', cellId: 9, cellNumber: 'H001-ISO2',
    dangerLevel: 'HIGH', prisonerStatus: 'INCARCERATED',
    recordDate: '2026-03-05', diagnosis: '高血压合并冠心病', treatment: '调整降压方案，加强心功能监测',
    hospital: '市中心医院', doctorName: '赵医生', medicalType: 'HOSPITALIZATION',
    result: 'TREATING', medicine: '缬沙坦80mg qd',
    followUpDate: '2026-04-05', followUpStatus: 'MISSED', actualFollowUpDate: '',
    followUpResult: '', followUpRemark: '连续两次未复诊',
    missedFollowUpCount: 2, isKeyAttention: true, keyAttentionReason: '连续2次未复诊、治疗未结束、治疗中且逾期未复诊、高危人员',
    daysUntilFollowUp: 0, daysOverdue: 67
  },
  {
    id: 10, prisonerId: 10, prisonerNumber: 'P20240010', prisonerName: '陈某', gender: '男',
    areaId: 4, areaName: '高度戒备区', cellId: 9, cellNumber: 'H001-ISO2',
    dangerLevel: 'EXTREME', prisonerStatus: 'INCARCERATED',
    recordDate: '2026-04-20', diagnosis: '腰椎间盘突出', treatment: '理疗+止痛治疗',
    hospital: '市中医院', doctorName: '陈医生', medicalType: 'OUTPATIENT',
    result: 'TREATING', medicine: '塞来昔布0.2g qd',
    followUpDate: '2026-05-20', followUpStatus: 'MISSED', actualFollowUpDate: '',
    followUpResult: '', followUpRemark: '未按时复诊',
    missedFollowUpCount: 2, isKeyAttention: true, keyAttentionReason: '连续2次未复诊、治疗未结束、治疗中且逾期未复诊、高危人员',
    daysUntilFollowUp: 0, daysOverdue: 22
  },
  {
    id: 101, prisonerId: 10, prisonerNumber: 'P20240010', prisonerName: '陈某', gender: '男',
    areaId: 4, areaName: '高度戒备区', cellId: 9, cellNumber: 'H001-ISO2',
    dangerLevel: 'EXTREME', prisonerStatus: 'INCARCERATED',
    recordDate: '2026-03-15', diagnosis: '腰肌劳损', treatment: '理疗康复',
    hospital: '市中医院', doctorName: '陈医生', medicalType: 'PHYSICAL',
    result: 'TREATING', medicine: '外用扶他林乳膏',
    followUpDate: '2026-04-15', followUpStatus: 'MISSED', actualFollowUpDate: '',
    followUpResult: '', followUpRemark: '连续两次未复诊，高危人员需关注',
    missedFollowUpCount: 2, isKeyAttention: true, keyAttentionReason: '连续2次未复诊、治疗未结束、治疗中且逾期未复诊、高危人员',
    daysUntilFollowUp: 0, daysOverdue: 57
  },
  {
    id: 9, prisonerId: 1, prisonerNumber: 'P20240001', prisonerName: '赵某', gender: '男',
    areaId: 1, areaName: '男监一区', cellId: 1, cellNumber: 'M001-101',
    dangerLevel: 'LOW', prisonerStatus: 'INCARCERATED',
    recordDate: '2026-05-08', diagnosis: '慢性支气管炎', treatment: '止咳化痰、预防感染',
    hospital: '监狱医务室', doctorName: '李医生', medicalType: 'OUTPATIENT',
    result: 'TREATING', medicine: '氨溴索30mg tid',
    followUpDate: '2026-06-08', followUpStatus: 'COMPLETED', actualFollowUpDate: '2026-06-08',
    followUpResult: '病情稳定，继续原方案', followUpRemark: '复诊情况良好',
    missedFollowUpCount: 0, isKeyAttention: false, keyAttentionReason: '',
    daysUntilFollowUp: 0, daysOverdue: 0
  }
]

const stats = ref<FollowUpStats>({ ...mockStats })
const allRecords = ref<FollowUpRecord[]>([])
const loading = ref(false)
const tableLoading = ref(false)
const useMockData = ref(false)

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
  { label: '今日待复诊', value: 'TODAY' },
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

const prisonerStatusMap: Record<string, string> = {
  INCARCERATED: '在押',
  RELEASED: '已释放',
  TRANSFERRED: '转监',
  MEDICAL_PAROLE: '保外就医'
}

const currentPage = ref(1)
const pageSize = ref(10)
const activeFilter = ref<string | null>(null)

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

const today = computed(() => {
  const t = new Date()
  t.setHours(0, 0, 0, 0)
  return t
})

function parseLocalDate(dateStr: string): Date {
  if (!dateStr) return new Date(NaN)
  const parts = dateStr.split('-')
  if (parts.length === 3) {
    return new Date(
      parseInt(parts[0], 10),
      parseInt(parts[1], 10) - 1,
      parseInt(parts[2], 10)
    )
  }
  return new Date(dateStr)
}

function daysUntil(dateStr: string): number {
  const d = parseLocalDate(dateStr)
  if (isNaN(d.getTime())) return -1
  const t = new Date(today.value)
  return Math.ceil((d.getTime() - t.getTime()) / (1000 * 60 * 60 * 24))
}

const filteredRecords = computed(() => {
  try {
    return allRecords.value.filter(r => {
      if (filterForm.keyword) {
        const kw = filterForm.keyword.toLowerCase()
        if (!r.prisonerName.toLowerCase().includes(kw)
          && !r.prisonerNumber.toLowerCase().includes(kw)
          && !r.diagnosis.toLowerCase().includes(kw)
          && !r.doctorName.toLowerCase().includes(kw)) {
          return false
        }
      }
      if (filterForm.followUpStatus) {
        if (filterForm.followUpStatus === 'TODAY') {
          if (!(r.followUpStatus === 'PENDING' && daysUntil(r.followUpDate) === 0)) {
            return false
          }
        } else if (filterForm.followUpStatus === 'OVERDUE') {
          if (!(r.followUpStatus === 'OVERDUE' || r.followUpStatus === 'MISSED'
            || (r.followUpStatus === 'PENDING' && daysUntil(r.followUpDate) < 0))) {
            return false
          }
        } else {
          if (r.followUpStatus !== filterForm.followUpStatus) return false
        }
      }
      if (filterForm.followUpStartDate) {
        if (daysUntil(r.followUpDate) < daysUntil(filterForm.followUpStartDate)) return false
      }
      if (filterForm.followUpEndDate) {
        if (daysUntil(r.followUpDate) > daysUntil(filterForm.followUpEndDate)) return false
      }
      if (filterForm.dangerLevel) {
        if (r.dangerLevel !== filterForm.dangerLevel) return false
      }
      if (filterForm.onlyKeyAttention) {
        if (!r.isKeyAttention) return false
      }
      if (activeFilter.value === 'TODAY') {
        if (!(r.followUpStatus === 'PENDING' && daysUntil(r.followUpDate) === 0)) return false
      } else if (activeFilter.value === 'WEEK') {
        const d = daysUntil(r.followUpDate)
        if (!(r.followUpStatus === 'PENDING' && d > 0 && d <= 7)) return false
      } else if (activeFilter.value === 'MONTH') {
        const d = daysUntil(r.followUpDate)
        if (!(r.followUpStatus === 'PENDING' && d > 7 && d <= 30)) return false
      } else if (activeFilter.value === 'OVERDUE') {
        if (!(r.followUpStatus === 'OVERDUE' || r.followUpStatus === 'MISSED'
          || (r.followUpStatus === 'PENDING' && daysUntil(r.followUpDate) < 0))) return false
      } else if (activeFilter.value === 'MISSED') {
        if (!r.isKeyAttention || !(r.missedFollowUpCount >= 2)) return false
      } else if (activeFilter.value === 'TREATING') {
        if (r.result !== 'TREATING') return false
      } else if (activeFilter.value === 'KEY') {
        if (!r.isKeyAttention) return false
      } else if (activeFilter.value === 'DONE') {
        if (r.followUpStatus !== 'COMPLETED') return false
      }
      return true
    })
  } catch (e) {
    console.error('filteredRecords error', e)
    return []
  }
})

const pagedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRecords.value.slice(start, start + pageSize.value)
})

function handleCardClick(key: string) {
  activeFilter.value = activeFilter.value === key ? null : key
  currentPage.value = 1
}

function handleFilter() {
  currentPage.value = 1
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
    if (!useMockData.value) {
      await post('/follow-up/mark', markForm)
    }
    ElMessage.success('操作成功')
    markDialogVisible.value = false
    await fetchData()
  } catch (e) {
    console.error('handleMarkSubmit error', e)
    if (useMockData.value) {
      const idx = allRecords.value.findIndex(r => r.id === markForm.medicalRecordId)
      if (idx > -1) {
        allRecords.value[idx].followUpStatus = markForm.followUpStatus
        if (markForm.actualFollowUpDate) {
          allRecords.value[idx].actualFollowUpDate = markForm.actualFollowUpDate
        }
        if (markForm.followUpResult) {
          allRecords.value[idx].followUpResult = markForm.followUpResult
        }
        if (markForm.followUpRemark) {
          allRecords.value[idx].followUpRemark = markForm.followUpRemark
        }
      }
      ElMessage.success('操作成功（模拟数据）')
      markDialogVisible.value = false
    }
  } finally {
    markLoading.value = false
  }
}

async function fetchStats() {
  loading.value = true
  try {
    const res = await get('/follow-up/stats')
    if (res && res.data && res.data.code === 200 && res.data.data) {
      stats.value = res.data.data
      useMockData.value = false
    } else {
      stats.value = { ...mockStats }
      useMockData.value = true
    }
  } catch (e) {
    console.warn('stats API failed, using mock', e)
    stats.value = { ...mockStats }
    useMockData.value = true
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
    if (filterForm.followUpStatus && !['TODAY'].includes(filterForm.followUpStatus)) {
      params.followUpStatus = filterForm.followUpStatus
    }
    if (filterForm.followUpStartDate) params.followUpStartDate = filterForm.followUpStartDate
    if (filterForm.followUpEndDate) params.followUpEndDate = filterForm.followUpEndDate
    if (filterForm.dangerLevel) params.dangerLevel = filterForm.dangerLevel
    if (filterForm.onlyKeyAttention) params.onlyKeyAttention = filterForm.onlyKeyAttention

    const res = await get('/follow-up/workbench', params)
    if (res && res.data && res.data.code === 200 && res.data.data) {
      const pageData = res.data.data
      const records = pageData.records || []
      if (Array.isArray(records) && records.length > 0) {
        allRecords.value = records
        useMockData.value = false
      } else {
        allRecords.value = [...mockRecords]
        useMockData.value = true
      }
    } else {
      allRecords.value = [...mockRecords]
      useMockData.value = true
    }
  } catch (e) {
    console.warn('workbench API failed, using mock', e)
    allRecords.value = [...mockRecords]
    useMockData.value = true
  } finally {
    tableLoading.value = false
  }
}

async function fetchData() {
  await Promise.all([fetchStats(), fetchWorkbench()])
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="workbench-page">
    <div class="page-header">
      <h2>医疗复诊工作台</h2>
      <el-tag v-if="useMockData" type="info" size="small" style="margin-left:12px">当前使用模拟数据</el-tag>
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

    <el-table
      v-loading="tableLoading"
      :data="pagedRecords"
      border
      stripe
      style="width:100%"
      empty-text="暂无符合条件的复诊记录"
      :row-class-name="({ row }) => row.isKeyAttention ? 'key-row' : ''"
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
        :total="filteredRecords.length"
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
              placeholder="请输入复诊后的诊断或治疗建议"
            />
          </el-form-item>
        </template>

        <el-form-item label="备注">
          <el-input
            v-model="markForm.followUpRemark"
            type="textarea"
            :rows="2"
            :placeholder="markActionType === 'MISS' ? '请说明未复诊的原因或后续跟进计划' : '其他需要说明的情况'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="markDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="markLoading" @click="handleMarkSubmit">确定</el-button>
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
