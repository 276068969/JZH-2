<script setup lang="ts">
import { reactive, ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { get, post, put, del } from '@/utils/request'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

interface Visitor {
  id: number
  visitorName: string
  idCard: string
  phone: string
  relation: string
  prisonerId: number
  visitDate: string
  visitTimeSlot: string
  status: string
  idCardPhoto: string
  visitorCount: number
  purpose: string
  approveGuardId: number
  approveRemark: string
  approveTime: string
  visitType: string
  actualStartTime: string
  actualEndTime: string
  remark: string
  lawyerLicenseNo: string
  lawFirmName: string
  powerOfAttorneyNo: string
  caseType: string
  needsTranslator: boolean
  recordingRequired: boolean
  lawyerLicenseValidDate: string
  isLegalAid: boolean
  assistantLawyerName: string
  assistantLawyerLicenseNo: string
  meetingSecurityLevel: string
  isUrgentLawyerMeeting: boolean
  lawyerEmail: string
  meetingStage: string
  roomTypeRequired: string
  hasAssistant: boolean
  createTime: string
  updateTime: string
  isLawyerVisit: boolean
  statusText: string
  caseTypeText: string
  meetingStageText: string
  meetingSecurityLevelText: string
  roomTypeText: string
  lawyerLicenseExpired: boolean
}

interface StatusStats {
  pending: number
  approved: number
  inProgress: number
  rejected: number
  completed: number
  cancelled: number
  lawyerPending: number
  familyPending: number
  lawyerApproved: number
  lawyerInProgress: number
  lawyerCompleted: number
  lawyerRejected: number
  lawyerCancelled: number
  lawyerUrgentPending: number
  lawyerLicenseExpiredPending: number
  familyApproved: number
  familyInProgress: number
  familyCompleted: number
  family: number
  lawyer: number
  other: number
}

interface VerificationItem {
  code: string
  name: string
  passed: boolean
  missingRemark: string
}

interface LawyerMeetingDetail {
  id: number
  visitorName: string
  idCard: string
  phone: string
  lawyerEmail: string
  prisonerId: number
  prisonerName: string
  prisonerNumber: string
  visitDate: string
  visitTimeSlot: string
  status: string
  statusText: string
  purpose: string
  approveRemark: string
  approveTime: string
  approveGuardName: string
  lawyerLicenseNo: string
  lawyerLicenseValidDate: string
  lawyerLicenseExpired: boolean
  lawFirmName: string
  powerOfAttorneyNo: string
  caseType: string
  caseTypeText: string
  meetingStage: string
  meetingStageText: string
  meetingSecurityLevel: string
  meetingSecurityLevelText: string
  roomTypeRequired: string
  roomTypeRequiredText: string
  needsTranslator: boolean
  recordingRequired: boolean
  isLegalAid: boolean
  isUrgentLawyerMeeting: boolean
  hasAssistant: boolean
  assistantLawyerName: string
  assistantLawyerLicenseNo: string
  verificationChecklist: VerificationItem[]
  createTime: string
}

const statusMap: Record<string, { label: string; type: string; color: string }> = {
  PENDING: { label: '待审核', type: 'warning', color: '#e6a23c' },
  APPROVED: { label: '已通过', type: 'success', color: '#67c23a' },
  REJECTED: { label: '已驳回', type: 'danger', color: '#f56c6c' },
  IN_PROGRESS: { label: '会见中', type: 'primary', color: '#409eff' },
  COMPLETED: { label: '已完成', type: 'info', color: '#909399' },
  CANCELLED: { label: '已取消', type: 'info', color: '#909399' }
}

const lawyerStatusMap: Record<string, { label: string; type: string; color: string }> = {
  PENDING: { label: '律师待审', type: 'warning', color: '#e6a23c' },
  APPROVED: { label: '律师已批', type: 'success', color: '#67c23a' },
  REJECTED: { label: '律师已驳', type: 'danger', color: '#f56c6c' },
  IN_PROGRESS: { label: '律师会见中', type: 'primary', color: '#1d4ed8' },
  COMPLETED: { label: '律师已完成', type: 'info', color: '#6b7280' },
  CANCELLED: { label: '律师已取消', type: 'info', color: '#909399' }
}

const visitTypeMap: Record<string, { label: string; tag: string; icon: string }> = {
  FAMILY: { label: '家属会见', tag: 'success', icon: 'HomeFilled' },
  LAWYER: { label: '律师会见', tag: 'primary', icon: 'ScaleToOriginal' },
  OTHER: { label: '其他', tag: 'info', icon: 'User' }
}

const relationMap: Record<string, string> = {
  PARENT: '父母',
  SPOUSE: '配偶',
  SIBLING: '兄弟姐妹',
  CHILD: '子女',
  FRIEND: '朋友',
  LAWYER: '律师',
  OTHER: '其他'
}

const caseTypeMap: Record<string, string> = {
  CRIMINAL: '刑事',
  CIVIL: '民事',
  ADMINISTRATIVE: '行政',
  OTHER: '其他'
}

const meetingStageMap: Record<string, string> = {
  INVESTIGATION: '侦查阶段',
  PROSECUTION: '审查起诉阶段',
  TRIAL: '审判阶段',
  EXECUTION: '执行阶段'
}

const securityLevelMap: Record<string, { label: string; type: string }> = {
  STANDARD: { label: '标准级', type: 'info' },
  ELEVATED: { label: '加强级', type: 'warning' },
  STRICT: { label: '严格级', type: 'danger' }
}

const roomTypeMap: Record<string, string> = {
  NORMAL: '普通会见室',
  ISOLATION: '隔离会见室',
  REMOTE: '远程会见室'
}

const tableData = ref<Visitor[]>([])
const statusStats = ref<StatusStats>({
  pending: 0,
  approved: 0,
  inProgress: 0,
  rejected: 0,
  completed: 0,
  cancelled: 0,
  lawyerPending: 0,
  familyPending: 0,
  lawyerApproved: 0,
  lawyerInProgress: 0,
  lawyerCompleted: 0,
  lawyerRejected: 0,
  lawyerCancelled: 0,
  lawyerUrgentPending: 0,
  lawyerLicenseExpiredPending: 0,
  familyApproved: 0,
  familyInProgress: 0,
  familyCompleted: 0,
  family: 0,
  lawyer: 0,
  other: 0
})
const total = ref(0)
const loading = ref(false)
const listMode = ref<'all' | 'lawyer' | 'family'>('all')
const searchForm = reactive({
  keyword: '',
  status: '',
  visitType: '',
  relation: ''
})
const currentPage = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const formLoading = ref(false)

const approvalDialogVisible = ref(false)
const approvalType = ref<'approve' | 'reject'>('approve')
const currentVisitor = ref<Visitor | null>(null)
const approvalForm = reactive({
  approveRemark: ''
})
const approvalFormRef = ref<FormInstance>()

const lawyerDetailVisible = ref(false)
const lawyerDetail = ref<LawyerMeetingDetail | null>(null)
const lawyerDetailLoading = ref(false)

const cancelDialogVisible = ref(false)
const cancelForm = reactive({
  approveRemark: ''
})
const cancelFormRef = ref<FormInstance>()

const form = reactive<Partial<Visitor>>({
  id: 0,
  visitorName: '',
  idCard: '',
  phone: '',
  relation: 'PARENT',
  prisonerId: 0,
  visitDate: '',
  visitTimeSlot: 'AM',
  status: 'PENDING',
  visitorCount: 1,
  purpose: '',
  visitType: 'FAMILY',
  remark: '',
  lawyerLicenseNo: '',
  lawFirmName: '',
  powerOfAttorneyNo: '',
  caseType: 'CRIMINAL',
  needsTranslator: false,
  recordingRequired: false,
  lawyerLicenseValidDate: '',
  isLegalAid: false,
  assistantLawyerName: '',
  assistantLawyerLicenseNo: '',
  meetingSecurityLevel: '',
  isUrgentLawyerMeeting: false,
  lawyerEmail: '',
  meetingStage: 'EXECUTION',
  roomTypeRequired: '',
  hasAssistant: false
})

const isLawyerVisit = computed(() => {
  return form.relation === 'LAWYER' || form.visitType === 'LAWYER'
})

const baseRules: FormRules = {
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  relation: [{ required: true, message: '请选择与服刑人员关系', trigger: 'change' }],
  prisonerId: [{ required: true, message: '请选择服刑人员', trigger: 'blur' }],
  visitDate: [{ required: true, message: '请选择会见日期', trigger: 'change' }],
  visitTimeSlot: [{ required: true, message: '请选择会见时段', trigger: 'change' }]
}

const lawyerRules: FormRules = {
  lawyerLicenseNo: [{ required: true, message: '请输入律师执业证号', trigger: 'blur' }],
  lawFirmName: [{ required: true, message: '请输入律师事务所名称', trigger: 'blur' }],
  powerOfAttorneyNo: [{ required: true, message: '请输入委托书/公函编号', trigger: 'blur' }],
  caseType: [{ required: true, message: '请选择案件类型', trigger: 'change' }]
}

const formRules = computed<FormRules>(() => {
  if (isLawyerVisit.value) {
    return { ...baseRules, ...lawyerRules }
  }
  return baseRules
})

const approvalRules = computed<FormRules>(() => {
  const isLawyer = currentVisitor.value?.visitType === 'LAWYER' || currentVisitor.value?.relation === 'LAWYER'
  if (approvalType.value === 'approve' && isLawyer) {
    return {
      approveRemark: [
        { required: true, message: '律师会见审批必须填写核验意见', trigger: 'blur' },
        { min: 5, message: '审批意见至少5个字，请详细说明核验情况', trigger: 'blur' }
      ]
    }
  }
  if (approvalType.value === 'reject' && isLawyer) {
    return {
      approveRemark: [
        { required: true, message: '驳回律师会见必须说明具体理由', trigger: 'blur' },
        { min: 5, message: '驳回理由至少5个字，请详细说明原因', trigger: 'blur' }
      ]
    }
  }
  return {
    approveRemark: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
  }
})

watch(() => form.relation, (newVal) => {
  if (newVal === 'LAWYER') {
    form.visitType = 'LAWYER'
    if (form.recordingRequired === undefined || form.recordingRequired === null) {
      form.recordingRequired = true
    }
    if (!form.caseType) {
      form.caseType = 'CRIMINAL'
    }
    if (!form.meetingStage) {
      form.meetingStage = 'EXECUTION'
    }
  }
})

watch(() => form.visitType, (newVal) => {
  if (newVal === 'LAWYER') {
    form.relation = 'LAWYER'
    if (form.recordingRequired === undefined || form.recordingRequired === null) {
      form.recordingRequired = true
    }
    if (!form.caseType) {
      form.caseType = 'CRIMINAL'
    }
    if (!form.meetingStage) {
      form.meetingStage = 'EXECUTION'
    }
  }
})

const canApprove = computed(() => authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_GUARD']))
const canEdit = computed(() => authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_GUARD']))
const canDelete = computed(() => authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_MANAGER']))

function tableRowClassName({ row }: { row: Visitor }) {
  if (row.visitType === 'LAWYER' || row.relation === 'LAWYER') {
    return 'lawyer-row'
  }
  return ''
}

const cancelRules = computed<FormRules>(() => {
  const isLawyer = currentVisitor.value?.visitType === 'LAWYER' || currentVisitor.value?.relation === 'LAWYER'
  if (isLawyer) {
    return {
      approveRemark: [
        { required: true, message: '取消律师会见必须说明原因', trigger: 'blur' },
        { min: 5, message: '取消原因至少5个字，请详细说明', trigger: 'blur' }
      ]
    }
  }
  return {
    approveRemark: [{ required: true, message: '请输入取消原因', trigger: 'blur' }]
  }
})

async function fetchList() {
  loading.value = true
  try {
    let url = '/visitors'
    const params: Record<string, any> = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status || undefined
    }
    if (listMode.value === 'lawyer') {
      url = '/visitors/lawyer'
    } else if (listMode.value === 'family') {
      url = '/visitors/family'
    } else {
      params.visitType = searchForm.visitType || undefined
      params.relation = searchForm.relation || undefined
    }
    const res = await get(url, params)
    if (res.data.code === 200) {
      tableData.value = res.data.data.records
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

async function fetchStatistics() {
  try {
    const res = await get('/visitors/statistics/detailed')
    if (res.data.code === 200) {
      statusStats.value = res.data.data
    }
  } catch (e) {
    console.error('获取统计数据失败', e)
    try {
      const res = await get('/visitors/statistics/status')
      if (res.data.code === 200) {
        Object.assign(statusStats.value, res.data.data)
      }
    } catch (e2) {
      console.error('获取基础统计数据也失败', e2)
    }
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchList()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.visitType = ''
  searchForm.relation = ''
  listMode.value = 'all'
  currentPage.value = 1
  fetchList()
}

function handleListModeChange(mode: 'all' | 'lawyer' | 'family') {
  listMode.value = mode
  currentPage.value = 1
  fetchList()
}

function handlePageChange(val: number) {
  currentPage.value = val
  fetchList()
}

function handleSizeChange(val: number) {
  pageSize.value = val
  currentPage.value = 1
  fetchList()
}

function handleAdd() {
  dialogTitle.value = '新增访客预约'
  Object.assign(form, {
    id: 0,
    visitorName: '',
    idCard: '',
    phone: '',
    relation: 'PARENT',
    prisonerId: 0,
    visitDate: '',
    visitTimeSlot: 'AM',
    status: 'PENDING',
    visitorCount: 1,
    purpose: '',
    visitType: 'FAMILY',
    remark: '',
    lawyerLicenseNo: '',
    lawFirmName: '',
    powerOfAttorneyNo: '',
    caseType: 'CRIMINAL',
    needsTranslator: false,
    recordingRequired: false,
    lawyerLicenseValidDate: '',
    isLegalAid: false,
    assistantLawyerName: '',
    assistantLawyerLicenseNo: '',
    meetingSecurityLevel: '',
    isUrgentLawyerMeeting: false,
    lawyerEmail: '',
    meetingStage: 'EXECUTION',
    roomTypeRequired: '',
    hasAssistant: false
  })
  dialogVisible.value = true
}

function handleEdit(row: Visitor) {
  dialogTitle.value = '编辑访客记录'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function handleViewLawyerDetail(row: Visitor) {
  lawyerDetailVisible.value = true
  lawyerDetailLoading.value = true
  lawyerDetail.value = null
  try {
    const res = await get(`/visitors/${row.id}/lawyer-detail`)
    if (res.data.code === 200) {
      lawyerDetail.value = res.data.data
    }
  } finally {
    lawyerDetailLoading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  formLoading.value = true
  try {
    if (form.id === 0) {
      const res = await post('/visitors', form)
      if (res.data.code === 200) {
        ElMessage.success(isLawyerVisit.value ? '律师会见申请已提交' : '新增成功')
        dialogVisible.value = false
        fetchList()
        fetchStatistics()
      }
    } else {
      const res = await put(`/visitors/${form.id}`, form)
      if (res.data.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchList()
        fetchStatistics()
      }
    }
  } finally {
    formLoading.value = false
  }
}

function handleDelete(row: Visitor) {
  const isLawyer = row.visitType === 'LAWYER' || row.relation === 'LAWYER'
  const msg = isLawyer
    ? `确定要删除律师 "${row.visitorName}" 的会见记录吗？此操作不可撤销。`
    : `确定要删除访客 "${row.visitorName}" 的记录吗？`
  ElMessageBox.confirm(msg, '确认删除', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(async () => {
    const res = await del(`/visitors/${row.id}`)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      fetchList()
      fetchStatistics()
    }
  })
}

function handleApprove(row: Visitor) {
  currentVisitor.value = row
  approvalType.value = 'approve'
  approvalForm.approveRemark = ''
  approvalDialogVisible.value = true
}

function handleReject(row: Visitor) {
  currentVisitor.value = row
  approvalType.value = 'reject'
  approvalForm.approveRemark = ''
  approvalDialogVisible.value = true
}

async function handleApprovalSubmit() {
  const valid = await approvalFormRef.value?.validate().catch(() => false)
  if (!valid || !currentVisitor.value) return
  formLoading.value = true
  try {
    const url = approvalType.value === 'approve'
      ? `/visitors/${currentVisitor.value.id}/approve`
      : `/visitors/${currentVisitor.value.id}/reject`
    const res = await post(url, approvalForm)
    if (res.data.code === 200) {
      const isLawyer = currentVisitor.value.visitType === 'LAWYER' || currentVisitor.value.relation === 'LAWYER'
      if (approvalType.value === 'approve') {
        ElMessage.success(isLawyer ? '律师会见已批准' : '审批通过')
      } else {
        ElMessage.success(isLawyer ? '律师会见已驳回' : '已驳回')
      }
      approvalDialogVisible.value = false
      fetchList()
      fetchStatistics()
    }
  } finally {
    formLoading.value = false
  }
}

async function handleStartVisit(row: Visitor) {
  const isLawyer = row.visitType === 'LAWYER' || row.relation === 'LAWYER'
  const msg = isLawyer
    ? `确定要开始律师 "${row.visitorName}" 的会见吗？请确认律师三证已核验完毕。`
    : `确定要开始 "${row.visitorName}" 的会见吗？`
  ElMessageBox.confirm(msg, isLawyer ? '律师会见确认' : '确认开始', {
    type: 'info'
  }).then(async () => {
    const res = await post(`/visitors/${row.id}/start`)
    if (res.data.code === 200) {
      ElMessage.success(isLawyer ? '律师会见已开始' : '会见已开始')
      fetchList()
      fetchStatistics()
    }
  })
}

async function handleEndVisit(row: Visitor) {
  const isLawyer = row.visitType === 'LAWYER' || row.relation === 'LAWYER'
  ElMessageBox.confirm(`确定要结束 "${row.visitorName}" 的会见吗？`, '确认结束', {
    type: 'warning'
  }).then(async () => {
    const res = await post(`/visitors/${row.id}/end`)
    if (res.data.code === 200) {
      ElMessage.success(isLawyer ? '律师会见已结束' : '会见已结束')
      fetchList()
      fetchStatistics()
    }
  })
}

function getStatusInfo(row: Visitor) {
  const isLawyer = isLawyerRow(row)
  const baseMap = isLawyer ? lawyerStatusMap : statusMap
  if (row.statusText) {
    const base = baseMap[row.status] || { label: row.statusText, type: 'info', color: '#909399' }
    return { ...base, label: row.statusText }
  }
  return baseMap[row.status] || { label: row.status, type: 'info', color: '#909399' }
}

function handleCancel(row: Visitor) {
  currentVisitor.value = row
  cancelForm.approveRemark = ''
  cancelDialogVisible.value = true
}

async function handleCancelSubmit() {
  const valid = await cancelFormRef.value?.validate().catch(() => false)
  if (!valid || !currentVisitor.value) return
  formLoading.value = true
  try {
    const res = await post(`/visitors/${currentVisitor.value.id}/cancel`, cancelForm)
    if (res.data.code === 200) {
      const isLawyer = isLawyerRow(currentVisitor.value)
      ElMessage.success(isLawyer ? '律师会见已取消' : '已取消')
      cancelDialogVisible.value = false
      fetchList()
      fetchStatistics()
    }
  } finally {
    formLoading.value = false
  }
}

function getVisitTypeInfo(type: string) {
  return visitTypeMap[type] || { label: '其他', tag: 'info', icon: 'User' }
}

function getRelationLabel(relation: string) {
  return relationMap[relation] || relation
}

function getCaseTypeLabel(caseType: string) {
  return caseTypeMap[caseType] || '-'
}

function getMeetingStageLabel(stage: string) {
  return meetingStageMap[stage] || '-'
}

function getSecurityLevelInfo(level: string) {
  return securityLevelMap[level] || { label: level || '-', type: 'info' }
}

function getRoomTypeLabel(roomType: string) {
  return roomTypeMap[roomType] || '-'
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return dateStr
}

function isLawyerRow(row: Visitor) {
  if (row.isLawyerVisit !== undefined && row.isLawyerVisit !== null) {
    return row.isLawyerVisit
  }
  return row.visitType === 'LAWYER' || row.relation === 'LAWYER'
}

onMounted(() => {
  fetchList()
  fetchStatistics()
})
</script>

<template>
  <div class="visitor-page">
    <div class="page-header">
      <h2>访客管理</h2>
      <p class="subtitle">访客预约审批与会见管理 - 律师会见与家属会见分类处理，法律会见独立审核</p>
    </div>

    <div class="list-mode-tabs">
      <el-radio-group v-model="listMode" size="default" @change="handleListModeChange">
        <el-radio-button value="all">
          <el-icon><Tickets /></el-icon>
          全部记录
        </el-radio-button>
        <el-radio-button value="lawyer">
          <el-icon><ScaleToOriginal /></el-icon>
          律师会见
          <el-badge
            v-if="statusStats.lawyerPending > 0"
            :value="statusStats.lawyerPending"
            class="mode-badge"
            type="warning"
          />
        </el-radio-button>
        <el-radio-button value="family">
          <el-icon><HomeFilled /></el-icon>
          家属会见
          <el-badge
            v-if="statusStats.familyPending > 0"
            :value="statusStats.familyPending"
            class="mode-badge"
            type="success"
          />
        </el-radio-button>
      </el-radio-group>
      <div class="mode-stats-hint" v-if="listMode === 'lawyer'">
        <el-tag v-if="statusStats.lawyerUrgentPending > 0" type="danger" effect="dark" size="small">
          紧急待审 {{ statusStats.lawyerUrgentPending }}
        </el-tag>
        <el-tag v-if="statusStats.lawyerLicenseExpiredPending > 0" type="warning" effect="dark" size="small">
          证件过期待审 {{ statusStats.lawyerLicenseExpiredPending }}
        </el-tag>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card pending" @click="searchForm.status = 'PENDING'; handleSearch()">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statusStats.pending }}</div>
          <div class="stat-label">待审核</div>
          <div class="stat-sub" v-if="statusStats.lawyerPending > 0 || statusStats.familyPending > 0">
            <span class="lawyer-sub" v-if="statusStats.lawyerPending > 0">律师{{ statusStats.lawyerPending }}</span>
            <span class="family-sub" v-if="statusStats.familyPending > 0">家属{{ statusStats.familyPending }}</span>
          </div>
        </div>
      </div>
      <div class="stat-card approved" @click="searchForm.status = 'APPROVED'; handleSearch()">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statusStats.approved }}</div>
          <div class="stat-label">已通过</div>
          <div class="stat-sub" v-if="statusStats.lawyerApproved > 0 || statusStats.familyApproved > 0">
            <span class="lawyer-sub" v-if="statusStats.lawyerApproved > 0">律师{{ statusStats.lawyerApproved }}</span>
            <span class="family-sub" v-if="statusStats.familyApproved > 0">家属{{ statusStats.familyApproved }}</span>
          </div>
        </div>
      </div>
      <div class="stat-card in-progress" @click="searchForm.status = 'IN_PROGRESS'; handleSearch()">
        <div class="stat-icon">
          <el-icon><VideoPlay /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statusStats.inProgress }}</div>
          <div class="stat-label">会见中</div>
          <div class="stat-sub" v-if="statusStats.lawyerInProgress > 0 || statusStats.familyInProgress > 0">
            <span class="lawyer-sub" v-if="statusStats.lawyerInProgress > 0">律师{{ statusStats.lawyerInProgress }}</span>
            <span class="family-sub" v-if="statusStats.familyInProgress > 0">家属{{ statusStats.familyInProgress }}</span>
          </div>
        </div>
      </div>
      <div class="stat-card completed" @click="searchForm.status = 'COMPLETED'; handleSearch()">
        <div class="stat-icon">
          <el-icon><Finished /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statusStats.completed }}</div>
          <div class="stat-label">已完成</div>
          <div class="stat-sub" v-if="statusStats.lawyerCompleted > 0 || statusStats.familyCompleted > 0">
            <span class="lawyer-sub" v-if="statusStats.lawyerCompleted > 0">律师{{ statusStats.lawyerCompleted }}</span>
            <span class="family-sub" v-if="statusStats.familyCompleted > 0">家属{{ statusStats.familyCompleted }}</span>
          </div>
        </div>
      </div>
      <div class="stat-card rejected" @click="searchForm.status = 'REJECTED'; handleSearch()">
        <div class="stat-icon">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statusStats.rejected }}</div>
          <div class="stat-label">已驳回</div>
          <div class="stat-sub" v-if="statusStats.lawyerRejected > 0">
            <span class="lawyer-sub">律师{{ statusStats.lawyerRejected }}</span>
          </div>
        </div>
      </div>
      <div class="stat-card type-summary">
        <div class="stat-icon">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-row">
            <span class="type-label">律师：</span>
            <span class="type-number lawyer-sub">{{ statusStats.lawyer || 0 }}</span>
          </div>
          <div class="stat-row">
            <span class="type-label">家属：</span>
            <span class="type-number family-sub">{{ statusStats.family || 0 }}</span>
          </div>
          <div class="stat-row">
            <span class="type-label">其他：</span>
            <span class="type-number">{{ statusStats.other || 0 }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="search-bar">
      <div class="search-left">
        <el-input
          v-model="searchForm.keyword"
          :placeholder="listMode === 'lawyer' ? '搜索律师姓名 / 身份证号 / 律师证号 / 律所 / 案件类型' : (listMode === 'family' ? '搜索姓名 / 身份证号 / 关系' : '搜索姓名 / 身份证号 / 关系 / 律师证号 / 律所')"
          clearable
          class="search-input"
          style="width: 320px"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="会见中" value="IN_PROGRESS" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-select v-if="listMode === 'all'" v-model="searchForm.visitType" placeholder="全部类型" clearable style="width: 140px">
          <el-option label="家属会见" value="FAMILY" />
          <el-option label="律师会见" value="LAWYER" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-select v-if="listMode === 'all' || listMode === 'family'" v-model="searchForm.relation" placeholder="全部关系" clearable style="width: 140px">
          <el-option label="父母" value="PARENT" />
          <el-option label="配偶" value="SPOUSE" />
          <el-option label="兄弟姐妹" value="SIBLING" />
          <el-option label="子女" value="CHILD" />
          <el-option label="朋友" value="FRIEND" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <div class="search-right">
        <el-button type="primary" @click="handleAdd" v-if="canEdit">
          <el-icon><Plus /></el-icon>
          新增预约
        </el-button>
      </div>
    </div>

    <el-table
      :data="tableData"
      border
      stripe
      v-loading="loading"
      style="width: 100%"
      :row-class-name="tableRowClassName"
    >
      <el-table-column label="标识" width="60" align="center">
        <template #default="{ row }">
          <div v-if="isLawyerRow(row)" class="lawyer-badge" title="律师会见">
            <el-icon :size="18"><ScaleToOriginal /></el-icon>
          </div>
          <div v-else class="family-badge" title="家属会见">
            <el-icon :size="16"><HomeFilled /></el-icon>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="visitorName" label="访客姓名" width="100" />
      <el-table-column label="会见类型" width="110">
        <template #default="{ row }">
          <div class="visit-type-cell" :class="{ 'lawyer-type': isLawyerRow(row) }">
            <el-tag :type="getVisitTypeInfo(row.visitType).tag" size="small" effect="light">
              <el-icon style="margin-right: 2px">
                <component :is="getVisitTypeInfo(row.visitType).icon" />
              </el-icon>
              {{ getVisitTypeInfo(row.visitType).label }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="与服刑人员关系" width="110">
        <template #default="{ row }">
          <span :class="{ 'lawyer-text': isLawyerRow(row) }">
            {{ getRelationLabel(row.relation) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="律师信息" min-width="220">
        <template #default="{ row }">
          <div v-if="isLawyerRow(row)" class="lawyer-info-cell">
            <div class="lawyer-license" :class="{ 'license-expired': row.lawyerLicenseExpired }">
              <el-icon><Document /></el-icon>
              <span>{{ row.lawyerLicenseNo || '-' }}</span>
              <el-tag v-if="row.lawyerLicenseExpired" type="danger" size="small" effect="dark" style="margin-left: 4px">
                过期
              </el-tag>
            </div>
            <div class="law-firm" v-if="row.lawFirmName">
              <el-icon><OfficeBuilding /></el-icon>
              {{ row.lawFirmName }}
            </div>
            <div class="lawyer-tags">
              <el-tag
                size="small"
                type="primary"
                effect="plain"
              >
                {{ row.caseTypeText || getCaseTypeLabel(row.caseType) }}
              </el-tag>
              <el-tag
                v-if="row.meetingStage || row.meetingStageText"
                size="small"
                type="info"
                effect="plain"
              >
                {{ row.meetingStageText || getMeetingStageLabel(row.meetingStage) }}
              </el-tag>
              <el-tag
                v-if="row.meetingSecurityLevel || row.meetingSecurityLevelText"
                size="small"
                :type="getSecurityLevelInfo(row.meetingSecurityLevel).type"
                effect="plain"
              >
                {{ row.meetingSecurityLevelText || getSecurityLevelInfo(row.meetingSecurityLevel).label }}
              </el-tag>
              <el-tag
                v-if="row.isUrgentLawyerMeeting"
                size="small"
                type="danger"
                effect="dark"
              >
                紧急
              </el-tag>
              <el-tag
                v-if="row.isLegalAid"
                size="small"
                type="success"
                effect="plain"
              >
                法律援助
              </el-tag>
            </div>
          </div>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="idCard" label="身份证号" width="180" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="visitDate" label="会见日期" width="110">
        <template #default="{ row }">
          {{ formatDate(row.visitDate) }}
        </template>
      </el-table-column>
      <el-table-column label="会见时段" width="90">
        <template #default="{ row }">
          {{ row.visitTimeSlot === 'AM' ? '上午' : '下午' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag
            :type="getStatusInfo(row).type"
            size="small"
            :effect="isLawyerRow(row) ? 'dark' : 'light'"
            :class="{ 'lawyer-status-tag': isLawyerRow(row) }"
          >
            {{ getStatusInfo(row).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审批意见" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.approveRemark || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="360" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="isLawyerRow(row)"
            type="primary"
            link
            size="small"
            @click="handleViewLawyerDetail(row)"
          >
            律师详情
          </el-button>
          <el-button type="primary" link size="small" @click="handleEdit(row)" v-if="canEdit && row.status !== 'IN_PROGRESS' && row.status !== 'COMPLETED'">
            编辑
          </el-button>
          <el-button
            type="success"
            link
            size="small"
            @click="handleApprove(row)"
            v-if="canApprove && row.status === 'PENDING'"
          >
            {{ isLawyerRow(row) ? '律师通过' : '通过' }}
          </el-button>
          <el-button
            type="danger"
            link
            size="small"
            @click="handleReject(row)"
            v-if="canApprove && row.status === 'PENDING'"
          >
            {{ isLawyerRow(row) ? '律师驳回' : '驳回' }}
          </el-button>
          <el-button
            type="info"
            link
            size="small"
            @click="handleCancel(row)"
            v-if="canApprove && (row.status === 'PENDING' || row.status === 'APPROVED')"
          >
            {{ isLawyerRow(row) ? '取消律师会见' : '取消' }}
          </el-button>
          <el-button
            type="primary"
            link
            size="small"
            @click="handleStartVisit(row)"
            v-if="canApprove && row.status === 'APPROVED'"
          >
            {{ isLawyerRow(row) ? '开始律师会见' : '开始会见' }}
          </el-button>
          <el-button
            type="warning"
            link
            size="small"
            @click="handleEndVisit(row)"
            v-if="canApprove && row.status === 'IN_PROGRESS'"
          >
            结束会见
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)" v-if="canDelete">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" destroy-on-close class="visitor-dialog">
      <el-form ref="formRef" :model="form" label-width="120px" :rules="formRules">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="访客姓名" prop="visitorName">
              <el-input v-model="form.visitorName" placeholder="请输入访客姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会见类型" prop="visitType">
              <el-select v-model="form.visitType" style="width: 100%">
                <el-option label="家属会见" value="FAMILY" />
                <el-option label="律师会见" value="LAWYER" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="与服刑人员关系" prop="relation">
              <el-select v-model="form.relation" style="width: 100%">
                <el-option label="父母" value="PARENT" />
                <el-option label="配偶" value="SPOUSE" />
                <el-option label="兄弟姐妹" value="SIBLING" />
                <el-option label="子女" value="CHILD" />
                <el-option label="朋友" value="FRIEND" />
                <el-option label="律师" value="LAWYER" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服刑人员ID" prop="prisonerId">
              <el-input-number v-model="form.prisonerId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="会见日期" prop="visitDate">
              <el-date-picker
                v-model="form.visitDate"
                type="date"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                placeholder="选择会见日期"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会见时段" prop="visitTimeSlot">
              <el-select v-model="form.visitTimeSlot" style="width: 100%">
                <el-option label="上午" value="AM" />
                <el-option label="下午" value="PM" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="来访人数" prop="visitorCount">
              <el-input-number v-model="form.visitorCount" :min="1" :max="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status" v-if="form.id !== 0">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="待审核" value="PENDING" />
                <el-option label="已通过" value="APPROVED" />
                <el-option label="已驳回" value="REJECTED" />
                <el-option label="会见中" value="IN_PROGRESS" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="会见目的" prop="purpose">
          <el-input v-model="form.purpose" :placeholder="isLawyerVisit ? '请输入会见目的（紧急会见必填）' : '请输入会见目的'" />
        </el-form-item>

        <div v-if="isLawyerVisit" class="lawyer-section">
          <div class="section-title">
            <el-icon><ScaleToOriginal /></el-icon>
            律师会见专属信息
            <el-tag type="primary" effect="dark" size="small" style="margin-left: 8px">法律会见</el-tag>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="律师执业证号" prop="lawyerLicenseNo">
                <el-input v-model="form.lawyerLicenseNo" placeholder="请输入律师执业证号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="执业证有效期">
                <el-date-picker
                  v-model="form.lawyerLicenseValidDate"
                  type="date"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                  placeholder="选择执业证有效期"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="律师事务所名称" prop="lawFirmName">
            <el-input v-model="form.lawFirmName" placeholder="请输入律师事务所名称" />
          </el-form-item>

          <el-form-item label="委托书/公函编号" prop="powerOfAttorneyNo">
            <el-input v-model="form.powerOfAttorneyNo" placeholder="请输入委托书或法律援助公函编号" />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="案件类型" prop="caseType">
                <el-select v-model="form.caseType" style="width: 100%">
                  <el-option label="刑事" value="CRIMINAL" />
                  <el-option label="民事" value="CIVIL" />
                  <el-option label="行政" value="ADMINISTRATIVE" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="诉讼阶段">
                <el-select v-model="form.meetingStage" style="width: 100%">
                  <el-option label="侦查阶段" value="INVESTIGATION" />
                  <el-option label="审查起诉阶段" value="PROSECUTION" />
                  <el-option label="审判阶段" value="TRIAL" />
                  <el-option label="执行阶段" value="EXECUTION" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="安全等级">
                <el-select v-model="form.meetingSecurityLevel" style="width: 100%" placeholder="自动根据服刑人员危险等级判定">
                  <el-option label="标准级" value="STANDARD" />
                  <el-option label="加强级" value="ELEVATED" />
                  <el-option label="严格级" value="STRICT" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="会见室类型">
                <el-select v-model="form.roomTypeRequired" style="width: 100%" placeholder="自动根据安全等级分配">
                  <el-option label="普通会见室" value="NORMAL" />
                  <el-option label="隔离会见室" value="ISOLATION" />
                  <el-option label="远程会见室" value="REMOTE" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="是否法律援助">
                <el-switch v-model="form.isLegalAid" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="是否紧急会见">
                <el-switch v-model="form.isUrgentLawyerMeeting" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="需要翻译">
                <el-switch v-model="form.needsTranslator" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="录音录像">
                <el-switch v-model="form.recordingRequired" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="律师邮箱">
                <el-input v-model="form.lawyerEmail" placeholder="请输入律师联系邮箱" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="携带助理会见">
            <el-switch v-model="form.hasAssistant" />
          </el-form-item>

          <div v-if="form.hasAssistant" class="assistant-section">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="协办律师姓名">
                  <el-input v-model="form.assistantLawyerName" placeholder="请输入协办律师姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="协办律师证号">
                  <el-input v-model="form.assistantLawyerLicenseNo" placeholder="请输入协办律师执业证号" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <div class="lawyer-notice lawyer-notice-important">
            <el-icon><WarningFilled /></el-icon>
            <div class="notice-content">
              <div class="notice-title">律师会见必须携带以下材料（三证）：</div>
              <ul>
                <li>✓ 律师执业证书（原件）</li>
                <li>✓ 律师事务所证明（律所函）</li>
                <li>✓ 委托书或者法律援助公函</li>
              </ul>
            </div>
          </div>
        </div>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="approvalDialogVisible"
      :title="approvalType === 'approve' ? '审批通过' : '审批驳回'"
      width="600px"
      destroy-on-close
      class="approval-dialog"
    >
      <div v-if="currentVisitor" class="approval-info" :class="{ 'lawyer-approval': isLawyerRow(currentVisitor) }">
        <div v-if="isLawyerRow(currentVisitor)" class="approval-header lawyer-approval-header">
          <el-icon :size="24"><ScaleToOriginal /></el-icon>
          <span>律师会见审批</span>
        </div>
        <div v-else class="approval-header family-approval-header">
          <el-icon :size="24"><HomeFilled /></el-icon>
          <span>家属会见审批</span>
        </div>

        <div class="info-row">
          <span class="label">访客姓名：</span>
          <span class="value">{{ currentVisitor.visitorName }}</span>
        </div>
        <div class="info-row">
          <span class="label">会见类型：</span>
          <span class="value">
            <el-tag :type="getVisitTypeInfo(currentVisitor.visitType).tag" size="small">
              {{ getVisitTypeInfo(currentVisitor.visitType).label }}
            </el-tag>
          </span>
        </div>
        <div class="info-row">
          <span class="label">会见日期：</span>
          <span class="value">{{ formatDate(currentVisitor.visitDate) }}</span>
        </div>
        <div class="info-row">
          <span class="label">会见目的：</span>
          <span class="value">{{ currentVisitor.purpose || '-' }}</span>
        </div>

        <div v-if="isLawyerRow(currentVisitor)" class="lawyer-approval-section">
          <div class="section-subtitle">
            <el-icon><List /></el-icon>
            律师会见材料核验清单
          </div>
          <div class="verify-list">
            <div class="verify-item" :class="{ 'verify-pass': currentVisitor.lawyerLicenseNo, 'verify-fail': !currentVisitor.lawyerLicenseNo }">
              <span class="verify-icon">
                <el-icon v-if="currentVisitor.lawyerLicenseNo"><CircleCheckFilled /></el-icon>
                <el-icon v-else><CircleCloseFilled /></el-icon>
              </span>
              <span class="verify-label">律师执业证号：</span>
              <span class="verify-value">{{ currentVisitor.lawyerLicenseNo || '未提供' }}</span>
            </div>
            <div class="verify-item" :class="{ 'verify-pass': currentVisitor.lawFirmName, 'verify-fail': !currentVisitor.lawFirmName }">
              <span class="verify-icon">
                <el-icon v-if="currentVisitor.lawFirmName"><CircleCheckFilled /></el-icon>
                <el-icon v-else><CircleCloseFilled /></el-icon>
              </span>
              <span class="verify-label">律师事务所：</span>
              <span class="verify-value">{{ currentVisitor.lawFirmName || '未提供' }}</span>
            </div>
            <div class="verify-item" :class="{ 'verify-pass': currentVisitor.powerOfAttorneyNo, 'verify-fail': !currentVisitor.powerOfAttorneyNo }">
              <span class="verify-icon">
                <el-icon v-if="currentVisitor.powerOfAttorneyNo"><CircleCheckFilled /></el-icon>
                <el-icon v-else><CircleCloseFilled /></el-icon>
              </span>
              <span class="verify-label">委托书/公函编号：</span>
              <span class="verify-value">{{ currentVisitor.powerOfAttorneyNo || '未提供' }}</span>
            </div>
            <div class="verify-item">
              <span class="verify-icon">
                <el-icon><InfoFilled /></el-icon>
              </span>
              <span class="verify-label">案件类型：</span>
              <span class="verify-value">{{ getCaseTypeLabel(currentVisitor.caseType) }}</span>
            </div>
            <div class="verify-item" v-if="currentVisitor.meetingStage">
              <span class="verify-icon">
                <el-icon><InfoFilled /></el-icon>
              </span>
              <span class="verify-label">诉讼阶段：</span>
              <span class="verify-value">{{ getMeetingStageLabel(currentVisitor.meetingStage) }}</span>
            </div>
            <div class="verify-item">
              <span class="verify-icon">
                <el-icon><VideoCamera /></el-icon>
              </span>
              <span class="verify-label">录音录像：</span>
              <span class="verify-value">
                <el-tag :type="currentVisitor.recordingRequired ? 'danger' : 'info'" size="small">
                  {{ currentVisitor.recordingRequired ? '需要' : '不需要' }}
                </el-tag>
              </span>
            </div>
            <div class="verify-item" v-if="currentVisitor.hasAssistant">
              <span class="verify-icon">
                <el-icon><User /></el-icon>
              </span>
              <span class="verify-label">协办律师：</span>
              <span class="verify-value">{{ currentVisitor.assistantLawyerName || '-' }} {{ currentVisitor.assistantLawyerLicenseNo || '' }}</span>
            </div>
            <div class="verify-item" v-if="currentVisitor.isLegalAid">
              <span class="verify-icon">
                <el-icon><InfoFilled /></el-icon>
              </span>
              <span class="verify-label">法律援助：</span>
              <span class="verify-value">是</span>
            </div>
            <div class="verify-item" v-if="currentVisitor.isUrgentLawyerMeeting">
              <span class="verify-icon">
                <el-icon><WarningFilled /></el-icon>
              </span>
              <span class="verify-label">紧急会见：</span>
              <span class="verify-value">是</span>
            </div>
          </div>
        </div>

        <div
          v-if="approvalType === 'approve' && isLawyerRow(currentVisitor)"
          class="lawyer-tip"
        >
          <el-icon><WarningFilled /></el-icon>
          <div>
            <strong>律师会见审批须知：</strong>
            请逐项核验律师执业证、律师事务所证明、委托书或法律援助公函的原件。审批意见需详细说明核验情况。
          </div>
        </div>
        <div
          v-if="approvalType === 'reject' && isLawyerRow(currentVisitor)"
          class="lawyer-reject-tip"
        >
          <el-icon><WarningFilled /></el-icon>
          <div>
            <strong>驳回律师会见须知：</strong>
            请详细说明驳回理由，如材料缺失、证件无效、会见时段冲突等。驳回理由至少5个字。
          </div>
        </div>
        <div
          v-if="approvalType === 'approve' && !isLawyerRow(currentVisitor)"
          class="family-tip"
        >
          <el-icon><InfoFilled /></el-icon>
          家属会见需核验身份证件和亲属关系证明
        </div>
      </div>
      <el-form ref="approvalFormRef" :model="approvalForm" label-width="100px" style="margin-top: 16px" :rules="approvalRules">
        <el-form-item
          :label="approvalType === 'approve' ? '审批意见' : '驳回理由'"
          prop="approveRemark"
        >
          <el-input
            v-model="approvalForm.approveRemark"
            type="textarea"
            :rows="4"
            :placeholder="approvalType === 'approve'
              ? (isLawyerRow(currentVisitor!) ? '请详细说明律师三证核验情况...' : '请输入审批意见')
              : (isLawyerRow(currentVisitor!) ? '请详细说明驳回律师会见的理由...' : '请输入驳回理由')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button
          :type="approvalType === 'approve' ? 'success' : 'danger'"
          :loading="formLoading"
          @click="handleApprovalSubmit"
        >
          {{ approvalType === 'approve'
            ? (isLawyerRow(currentVisitor!) ? '确认批准律师会见' : '确认通过')
            : (isLawyerRow(currentVisitor!) ? '确认驳回律师会见' : '确认驳回') }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="lawyerDetailVisible"
      title="律师会见详情"
      width="680px"
      destroy-on-close
      class="lawyer-detail-dialog"
    >
      <div v-loading="lawyerDetailLoading" v-if="lawyerDetail" class="lawyer-detail-content">
        <div class="detail-header">
          <div class="detail-title">
            <el-icon :size="22"><ScaleToOriginal /></el-icon>
            <span>{{ lawyerDetail.visitorName }} - 律师会见详情</span>
            <el-tag type="primary" effect="dark" size="small" style="margin-left: 8px">
              {{ lawyerDetail.statusText }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="律师姓名">{{ lawyerDetail.visitorName }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ lawyerDetail.idCard }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ lawyerDetail.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="律师邮箱">{{ lawyerDetail.lawyerEmail || '-' }}</el-descriptions-item>
          <el-descriptions-item label="律师执业证号">
            <span :class="{ 'text-danger': lawyerDetail.lawyerLicenseExpired }">
              {{ lawyerDetail.lawyerLicenseNo }}
              <el-tag v-if="lawyerDetail.lawyerLicenseExpired" type="danger" size="small" style="margin-left: 4px">已过期</el-tag>
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="执业证有效期">
            {{ lawyerDetail.lawyerLicenseValidDate || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="律师事务所" :span="2">{{ lawyerDetail.lawFirmName }}</el-descriptions-item>
          <el-descriptions-item label="委托书/公函编号" :span="2">{{ lawyerDetail.powerOfAttorneyNo }}</el-descriptions-item>
          <el-descriptions-item label="案件类型">{{ lawyerDetail.caseTypeText }}</el-descriptions-item>
          <el-descriptions-item label="诉讼阶段">{{ lawyerDetail.meetingStageText }}</el-descriptions-item>
          <el-descriptions-item label="安全等级">
            <el-tag :type="getSecurityLevelInfo(lawyerDetail.meetingSecurityLevel).type" size="small">
              {{ lawyerDetail.meetingSecurityLevelText }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="会见室类型">{{ lawyerDetail.roomTypeRequiredText }}</el-descriptions-item>
          <el-descriptions-item label="服刑人员">{{ lawyerDetail.prisonerName }} ({{ lawyerDetail.prisonerNumber }})</el-descriptions-item>
          <el-descriptions-item label="会见日期">{{ formatDate(lawyerDetail.visitDate) }}</el-descriptions-item>
          <el-descriptions-item label="会见时段">{{ lawyerDetail.visitTimeSlot === 'AM' ? '上午' : '下午' }}</el-descriptions-item>
          <el-descriptions-item label="录音录像">
            {{ lawyerDetail.recordingRequired ? '需要' : '不需要' }}
          </el-descriptions-item>
          <el-descriptions-item label="是否法律援助">
            {{ lawyerDetail.isLegalAid ? '是' : '否' }}
          </el-descriptions-item>
          <el-descriptions-item label="是否紧急会见">
            <el-tag v-if="lawyerDetail.isUrgentLawyerMeeting" type="danger" size="small">紧急</el-tag>
            <span v-else>否</span>
          </el-descriptions-item>
          <el-descriptions-item label="是否需要翻译">
            {{ lawyerDetail.needsTranslator ? '需要' : '不需要' }}
          </el-descriptions-item>
          <el-descriptions-item label="会见目的" :span="2">{{ lawyerDetail.purpose || '-' }}</el-descriptions-item>
          <template v-if="lawyerDetail.hasAssistant">
            <el-descriptions-item label="协办律师">{{ lawyerDetail.assistantLawyerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="协办律师证号">{{ lawyerDetail.assistantLawyerLicenseNo || '-' }}</el-descriptions-item>
          </template>
          <el-descriptions-item label="审批人" v-if="lawyerDetail.approveGuardName">
            {{ lawyerDetail.approveGuardName }}
          </el-descriptions-item>
          <el-descriptions-item label="审批时间" v-if="lawyerDetail.approveTime">
            {{ lawyerDetail.approveTime }}
          </el-descriptions-item>
          <el-descriptions-item label="审批意见" :span="2" v-if="lawyerDetail.approveRemark">
            {{ lawyerDetail.approveRemark }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="verification-section">
          <div class="section-subtitle">
            <el-icon><ListCheck /></el-icon>
            材料核验清单
          </div>
          <div class="verification-list">
            <div
              v-for="item in lawyerDetail.verificationChecklist"
              :key="item.code"
              class="verification-item"
              :class="{ 'v-pass': item.passed, 'v-fail': !item.passed }"
            >
              <span class="v-icon">
                <el-icon v-if="item.passed" :size="18"><CircleCheckFilled /></el-icon>
                <el-icon v-else :size="18"><CircleCloseFilled /></el-icon>
              </span>
              <span class="v-name">{{ item.name }}</span>
              <span v-if="item.missingRemark" class="v-remark">{{ item.missingRemark }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="lawyerDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="cancelDialogVisible"
      title="取消会见"
      width="560px"
      destroy-on-close
      class="cancel-dialog"
    >
      <div v-if="currentVisitor" class="cancel-info" :class="{ 'lawyer-cancel': isLawyerRow(currentVisitor) }">
        <div v-if="isLawyerRow(currentVisitor)" class="cancel-header lawyer-cancel-header">
          <el-icon :size="22"><WarningFilled /></el-icon>
          <span>取消律师会见确认</span>
        </div>
        <div v-else class="cancel-header family-cancel-header">
          <el-icon :size="22"><InfoFilled /></el-icon>
          <span>取消家属会见确认</span>
        </div>

        <div class="info-row">
          <span class="label">访客姓名：</span>
          <span class="value">{{ currentVisitor.visitorName }}</span>
        </div>
        <div class="info-row">
          <span class="label">会见类型：</span>
          <span class="value">
            <el-tag :type="getVisitTypeInfo(currentVisitor.visitType).tag" size="small">
              {{ getVisitTypeInfo(currentVisitor.visitType).label }}
            </el-tag>
          </span>
        </div>
        <div class="info-row">
          <span class="label">会见日期：</span>
          <span class="value">{{ formatDate(currentVisitor.visitDate) }}</span>
        </div>
        <div class="info-row">
          <span class="label">当前状态：</span>
          <span class="value">
            <el-tag :type="getStatusInfo(currentVisitor).type" size="small">
              {{ getStatusInfo(currentVisitor).label }}
            </el-tag>
          </span>
        </div>

        <div v-if="isLawyerRow(currentVisitor)" class="lawyer-cancel-tip">
          <el-icon><WarningFilled /></el-icon>
          <div>
            <strong>取消律师会见须知：</strong>
            请详细说明取消原因，如时间冲突、材料补充、当事人变更委托等。取消原因至少5个字，将记录在案。
          </div>
        </div>
        <div v-else class="family-cancel-tip">
          <el-icon><InfoFilled /></el-icon>
          <div>请说明取消会见的原因</div>
        </div>
      </div>
      <el-form ref="cancelFormRef" :model="cancelForm" label-width="100px" style="margin-top: 16px" :rules="cancelRules">
        <el-form-item label="取消原因" prop="approveRemark">
          <el-input
            v-model="cancelForm.approveRemark"
            type="textarea"
            :rows="3"
            :placeholder="isLawyerRow(currentVisitor!) ? '请详细说明取消律师会见的原因...' : '请输入取消原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button
          type="warning"
          :loading="formLoading"
          @click="handleCancelSubmit"
        >
          {{ isLawyerRow(currentVisitor!) ? '确认取消律师会见' : '确认取消' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.visitor-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 4px 0;
}

.subtitle {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.stats-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 140px;
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card .stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-card.pending {
  background: linear-gradient(135deg, #fef6e4 0%, #fff7ed 100%);
}
.stat-card.pending .stat-icon {
  background: #e6a23c;
  color: #fff;
}

.stat-card.approved {
  background: linear-gradient(135deg, #e8f7e8 0%, #f0fff0 100%);
}
.stat-card.approved .stat-icon {
  background: #67c23a;
  color: #fff;
}

.stat-card.in-progress {
  background: linear-gradient(135deg, #e6f2ff 0%, #f0f7ff 100%);
}
.stat-card.in-progress .stat-icon {
  background: #409eff;
  color: #fff;
}

.stat-card.completed {
  background: linear-gradient(135deg, #f0f2f5 0%, #f5f7fa 100%);
}
.stat-card.completed .stat-icon {
  background: #909399;
  color: #fff;
}

.stat-card.rejected {
  background: linear-gradient(135deg, #ffecec 0%, #fff0f0 100%);
}
.stat-card.rejected .stat-icon {
  background: #f56c6c;
  color: #fff;
}

.stat-content .stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-content .stat-label {
  font-size: 13px;
  color: #606266;
}

.stat-sub {
  margin-top: 4px;
  font-size: 11px;
  display: flex;
  gap: 8px;
}
.lawyer-sub {
  color: #1d4ed8;
  font-weight: 500;
}
.family-sub {
  color: #67c23a;
  font-weight: 500;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.search-left {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 260px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.lawyer-row {
  background-color: #eff6ff !important;
}
.lawyer-row td.el-table__cell {
  background-color: #eff6ff !important;
}
.lawyer-row:hover td.el-table__cell {
  background-color: #dbeafe !important;
}

.lawyer-badge {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #1d4ed8 0%, #3b82f6 100%);
  color: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 2px 6px rgba(29, 78, 216, 0.3);
}

.family-badge {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #059669 0%, #34d399 100%);
  color: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 2px 6px rgba(5, 150, 105, 0.3);
}

.visit-type-cell {
  display: flex;
  align-items: center;
}
.visit-type-cell.lawyer-type .el-tag {
  background: #dbeafe;
  border-color: #93c5fd;
  color: #1d4ed8;
  font-weight: 500;
}

.lawyer-text {
  color: #1d4ed8;
  font-weight: 500;
}

.lawyer-status-tag {
  font-weight: 600;
  letter-spacing: 0.3px;
}

.lawyer-info-cell {
  font-size: 12px;
  line-height: 1.6;
}
.lawyer-info-cell .lawyer-license,
.lawyer-info-cell .law-firm {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #374151;
  margin-bottom: 2px;
}
.lawyer-info-cell .lawyer-tags {
  margin-top: 4px;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.text-muted {
  color: #9ca3af;
}

.text-danger {
  color: #ef4444;
  font-weight: 500;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  padding: 12px 16px;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-radius: 6px;
  margin-bottom: 16px;
  border-left: 4px solid #3b82f6;
}

.assistant-section {
  background: #f9fafb;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 12px;
  border: 1px dashed #d1d5db;
}

.lawyer-notice {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  margin-top: 12px;
  border-radius: 6px;
  font-size: 13px;
}
.lawyer-notice-important {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fcd34d;
}
.lawyer-notice-important .notice-title {
  font-weight: 600;
  margin-bottom: 4px;
}
.lawyer-notice-important ul {
  margin: 4px 0 0 0;
  padding-left: 16px;
}
.lawyer-notice-important li {
  margin-bottom: 2px;
}

.approval-info {
  padding: 16px;
  border-radius: 8px;
  background: #f5f7fa;
}
.approval-info.lawyer-approval {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
}

.approval-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}
.lawyer-approval-header {
  color: #1d4ed8;
}
.family-approval-header {
  color: #059669;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}
.info-row:last-child {
  margin-bottom: 0;
}
.info-row .label {
  color: #6b7280;
  width: 100px;
  flex-shrink: 0;
}
.info-row .value {
  color: #1f2937;
  flex: 1;
}

.lawyer-approval-section {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #93c5fd;
}

.section-subtitle {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #1d4ed8;
  margin-bottom: 12px;
}

.verify-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.verify-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 6px;
  background: #fff;
  font-size: 13px;
}
.verify-item.verify-pass {
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
}
.verify-item.verify-fail {
  background: #fef2f2;
  border: 1px solid #fecaca;
}
.verify-icon {
  display: flex;
  align-items: center;
}
.verify-pass .verify-icon {
  color: #10b981;
}
.verify-fail .verify-icon {
  color: #ef4444;
}
.verify-label {
  color: #6b7280;
  flex-shrink: 0;
}
.verify-value {
  color: #1f2937;
  font-weight: 500;
}

.lawyer-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  margin-top: 12px;
  border-radius: 6px;
  background: #dbeafe;
  color: #1e40af;
  font-size: 13px;
  line-height: 1.5;
}
.lawyer-tip strong {
  display: block;
  margin-bottom: 2px;
}

.lawyer-reject-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  margin-top: 12px;
  border-radius: 6px;
  background: #fee2e2;
  color: #991b1b;
  font-size: 13px;
  line-height: 1.5;
}
.lawyer-reject-tip strong {
  display: block;
  margin-bottom: 2px;
}

.family-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  margin-top: 12px;
  border-radius: 6px;
  background: #d1fae5;
  color: #065f46;
  font-size: 13px;
}

.lawyer-detail-content .detail-header {
  margin-bottom: 16px;
}
.lawyer-detail-content .detail-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 17px;
  font-weight: 600;
  color: #1f2937;
}

.verification-section {
  margin-top: 20px;
}
.verification-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.verification-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 14px;
}
.verification-item.v-pass {
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
}
.verification-item.v-fail {
  background: #fef2f2;
  border: 1px solid #fecaca;
}
.verification-item .v-icon {
  flex-shrink: 0;
}
.verification-item.v-pass .v-icon {
  color: #10b981;
}
.verification-item.v-fail .v-icon {
  color: #ef4444;
}
.verification-item .v-name {
  flex: 1;
  font-weight: 500;
  color: #1f2937;
}
.verification-item .v-remark {
  font-size: 12px;
  color: #ef4444;
  background: #fff;
  padding: 2px 8px;
  border-radius: 4px;
}

.list-mode-tabs {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.list-mode-tabs .mode-badge {
  margin-left: 6px;
}

.mode-stats-hint {
  display: flex;
  gap: 8px;
  align-items: center;
}

.stat-card.type-summary {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.stat-card.type-summary .stat-icon {
  background: #6366f1;
  color: #fff;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  line-height: 1.8;
}

.type-label {
  color: #6b7280;
}

.type-number {
  font-weight: 600;
  color: #1f2937;
}

.license-expired {
  color: #ef4444;
  font-weight: 500;
}

.license-expired span {
  text-decoration: line-through;
  opacity: 0.7;
}

.cancel-info {
  padding: 16px;
  border-radius: 8px;
  background: #f5f7fa;
}
.cancel-info.lawyer-cancel {
  background: #fff7ed;
  border: 1px solid #fed7aa;
}

.cancel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}
.lawyer-cancel-header {
  color: #c2410c;
}
.family-cancel-header {
  color: #059669;
}

.lawyer-cancel-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  margin-top: 12px;
  border-radius: 6px;
  background: #ffedd5;
  color: #9a3412;
  font-size: 13px;
  line-height: 1.5;
}
.lawyer-cancel-tip strong {
  display: block;
  margin-bottom: 2px;
}

.family-cancel-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  margin-top: 12px;
  border-radius: 6px;
  background: #d1fae5;
  color: #065f46;
  font-size: 13px;
}
</style>