<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
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
  createTime: string
  updateTime: string
}

interface StatusStats {
  pending: number
  approved: number
  inProgress: number
  rejected: number
  completed: number
  cancelled: number
}

const statusMap: Record<string, { label: string; type: string; color: string }> = {
  PENDING: { label: '待审核', type: 'warning', color: '#e6a23c' },
  APPROVED: { label: '已通过', type: 'success', color: '#67c23a' },
  REJECTED: { label: '已驳回', type: 'danger', color: '#f56c6c' },
  IN_PROGRESS: { label: '会见中', type: 'primary', color: '#409eff' },
  COMPLETED: { label: '已完成', type: 'info', color: '#909399' },
  CANCELLED: { label: '已取消', type: 'info', color: '#909399' }
}

const visitTypeMap: Record<string, { label: string; tag: string }> = {
  FAMILY: { label: '家属会见', tag: 'success' },
  LAWYER: { label: '律师会见', tag: 'primary' },
  OTHER: { label: '其他', tag: 'info' }
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

const tableData = ref<Visitor[]>([])
const statusStats = ref<StatusStats>({
  pending: 0,
  approved: 0,
  inProgress: 0,
  rejected: 0,
  completed: 0,
  cancelled: 0
})
const total = ref(0)
const loading = ref(false)
const searchForm = reactive({
  keyword: '',
  status: '',
  visitType: ''
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
  remark: ''
})

const canApprove = computed(() => authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_GUARD']))
const canEdit = computed(() => authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_GUARD']))
const canDelete = computed(() => authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_MANAGER']))

async function fetchList() {
  loading.value = true
  try {
    const res = await get('/visitors', {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status || undefined,
      visitType: searchForm.visitType || undefined
    })
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
    const res = await get('/visitors/statistics/status')
    if (res.data.code === 200) {
      statusStats.value = res.data.data
    }
  } catch (e) {
    console.error('获取统计数据失败', e)
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
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row: Visitor) {
  dialogTitle.value = '编辑访客记录'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  formLoading.value = true
  try {
    if (form.id === 0) {
      const res = await post('/visitors', form)
      if (res.data.code === 200) {
        ElMessage.success('新增成功')
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
  ElMessageBox.confirm(`确定要删除访客 "${row.visitorName}" 的记录吗？`, '确认删除', {
    type: 'warning'
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
      ElMessage.success(approvalType.value === 'approve' ? '审批通过' : '已驳回')
      approvalDialogVisible.value = false
      fetchList()
      fetchStatistics()
    }
  } finally {
    formLoading.value = false
  }
}

async function handleStartVisit(row: Visitor) {
  ElMessageBox.confirm(`确定要开始 "${row.visitorName}" 的会见吗？`, '确认开始', {
    type: 'info'
  }).then(async () => {
    const res = await post(`/visitors/${row.id}/start`)
    if (res.data.code === 200) {
      ElMessage.success('会见已开始')
      fetchList()
      fetchStatistics()
    }
  })
}

async function handleEndVisit(row: Visitor) {
  ElMessageBox.confirm(`确定要结束 "${row.visitorName}" 的会见吗？`, '确认结束', {
    type: 'warning'
  }).then(async () => {
    const res = await post(`/visitors/${row.id}/end`)
    if (res.data.code === 200) {
      ElMessage.success('会见已结束')
      fetchList()
      fetchStatistics()
    }
  })
}

function getStatusInfo(status: string) {
  return statusMap[status] || { label: status, type: 'info', color: '#909399' }
}

function getVisitTypeInfo(type: string) {
  return visitTypeMap[type] || { label: '其他', tag: 'info' }
}

function getRelationLabel(relation: string) {
  return relationMap[relation] || relation
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return dateStr
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
      <p class="subtitle">访客预约审批与会见管理</p>
    </div>

    <div class="stats-cards">
      <div class="stat-card pending" @click="searchForm.status = 'PENDING'; handleSearch()">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statusStats.pending }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>
      <div class="stat-card approved" @click="searchForm.status = 'APPROVED'; handleSearch()">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statusStats.approved }}</div>
          <div class="stat-label">已通过</div>
        </div>
      </div>
      <div class="stat-card in-progress" @click="searchForm.status = 'IN_PROGRESS'; handleSearch()">
        <div class="stat-icon">
          <el-icon><VideoPlay /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statusStats.inProgress }}</div>
          <div class="stat-label">会见中</div>
        </div>
      </div>
      <div class="stat-card completed" @click="searchForm.status = 'COMPLETED'; handleSearch()">
        <div class="stat-icon">
          <el-icon><Finished /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statusStats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      <div class="stat-card rejected" @click="searchForm.status = 'REJECTED'; handleSearch()">
        <div class="stat-icon">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statusStats.rejected }}</div>
          <div class="stat-label">已驳回</div>
        </div>
      </div>
    </div>

    <div class="search-bar">
      <div class="search-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索访客姓名 / 身份证号 / 关系"
          clearable
          class="search-input"
          style="width: 280px"
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
        <el-select v-model="searchForm.visitType" placeholder="全部类型" clearable style="width: 140px">
          <el-option label="家属会见" value="FAMILY" />
          <el-option label="律师会见" value="LAWYER" />
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

    <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="visitorName" label="访客姓名" width="100" />
      <el-table-column label="会见类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getVisitTypeInfo(row.visitType).tag" size="small">
            {{ getVisitTypeInfo(row.visitType).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="与服刑人员关系" width="110">
        <template #default="{ row }">
          {{ getRelationLabel(row.relation) }}
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
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusInfo(row.status).type" size="small" effect="light">
            {{ getStatusInfo(row.status).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审批意见" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.approveRemark || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)" v-if="canEdit">
            编辑
          </el-button>
          <el-button
            type="success"
            link
            size="small"
            @click="handleApprove(row)"
            v-if="canApprove && row.status === 'PENDING'"
          >
            通过
          </el-button>
          <el-button
            type="danger"
            link
            size="small"
            @click="handleReject(row)"
            v-if="canApprove && row.status === 'PENDING'"
          >
            驳回
          </el-button>
          <el-button
            type="primary"
            link
            size="small"
            @click="handleStartVisit(row)"
            v-if="canApprove && row.status === 'APPROVED'"
          >
            开始会见
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="100px" :rules="{}">
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
          <el-input v-model="form.purpose" placeholder="请输入会见目的" />
        </el-form-item>
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
      width="500px"
      destroy-on-close
    >
      <div v-if="currentVisitor" class="approval-info">
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
        <div
          v-if="approvalType === 'approve' && currentVisitor.visitType === 'LAWYER'"
          class="lawyer-tip"
        >
          <el-icon><InfoFilled /></el-icon>
          律师会见需核验律师执业证、律师事务所证明和委托书
        </div>
        <div
          v-if="approvalType === 'approve' && currentVisitor.visitType === 'FAMILY'"
          class="family-tip"
        >
          <el-icon><InfoFilled /></el-icon>
          家属会见需核验身份证件和亲属关系证明
        </div>
      </div>
      <el-form ref="approvalFormRef" :model="approvalForm" label-width="80px" style="margin-top: 16px">
        <el-form-item
          :label="approvalType === 'approve' ? '审批意见' : '驳回理由'"
          prop="approveRemark"
          :rules="[{ required: true, message: '请输入审批意见', trigger: 'blur' }]"
        >
          <el-input
            v-model="approvalForm.approveRemark"
            type="textarea"
            :rows="4"
            :placeholder="approvalType === 'approve' ? '请输入审批意见' : '请输入驳回理由'"
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
          {{ approvalType === 'approve' ? '确认通过' : '确认驳回' }}
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
}

.stat-card {
  flex: 1;
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

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 260px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.approval-info {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 6px;
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
  color: #909399;
  width: 80px;
  flex-shrink: 0;
}

.info-row .value {
  color: #303133;
  flex: 1;
}

.lawyer-tip,
.family-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  margin-top: 12px;
  border-radius: 4px;
  font-size: 13px;
}

.lawyer-tip {
  background: #ecf5ff;
  color: #409eff;
}

.family-tip {
  background: #f0f9eb;
  color: #67c23a;
}
</style>
