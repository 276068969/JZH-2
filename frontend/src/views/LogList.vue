<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get } from '@/utils/request'

interface Log {
  id: number
  operatorUsername: string
  operatorRealName: string
  module: string
  moduleText: string
  action: string
  actionText: string
  detail: string
  targetType: string
  targetId: number
  targetName: string
  ipAddress: string
  status: string
  failReason: string
  requestMethod: string
  requestUrl: string
  createTime: string
}

interface PageResult {
  records: Log[]
  total: number
  size: number
  current: number
  pages: number
}

const moduleTextMap: Record<string, string> = {
  AUTH: '用户认证',
  PRISONER: '服刑人员管理',
  PRISONER_TRANSFER: '服刑人员调动',
  INCIDENT: '事件管理',
  VISITOR: '访客管理',
  PATROL: '巡查管理',
  PATROL_HANDOVER: '巡查交接班',
  MEDICAL: '医疗记录',
  USER: '用户管理',
  ROLE: '角色管理',
  GUARD: '狱警管理',
  PRISON_AREA: '监区管理',
  CELL: '监舍管理',
  FOLLOW_UP: '重点关注',
  SYSTEM: '系统管理'
}

const actionTextMap: Record<string, string> = {
  LOGIN: '登录',
  LOGIN_FAIL: '登录失败',
  LOGOUT: '登出',
  CREATE: '新增',
  UPDATE: '修改',
  DELETE: '删除',
  APPROVE: '审批通过',
  REJECT: '审批驳回',
  START_PROCESSING: '开始处置',
  RESOLVE: '处置完成',
  CLOSE: '关闭',
  START_VISIT: '开始会见',
  END_VISIT: '结束会见',
  CANCEL: '取消',
  TRANSFER: '调动',
  EXPORT: '导出'
}

function getModuleText(code: string) {
  return moduleTextMap[code] || code
}

function getActionText(code: string) {
  return actionTextMap[code] || code
}

function getStatusText(code: string) {
  return code === 'SUCCESS' ? '成功' : code === 'FAILURE' ? '失败' : code
}

const tableData = ref<Log[]>([])
const total = ref(0)
const loading = ref(false)
const moduleOptions = ref<{ code: string; description: string }[]>([])

const searchForm = reactive({
  keyword: '',
  module: '',
  status: ''
})

const currentPage = ref(1)
const pageSize = ref(10)

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchForm.keyword || undefined,
      module: searchForm.module || undefined,
      status: searchForm.status || undefined
    }
    const res = await get<any>('/sys-logs', params)
    if (res.data?.code === 200) {
      const data: PageResult = res.data.data
      tableData.value = data.records.map((item) => ({
        ...item,
        moduleText: getModuleText(item.module),
        actionText: getActionText(item.action)
      }))
      total.value = data.total
    }
  } catch (e) {
    console.error('加载日志失败', e)
  } finally {
    loading.value = false
  }
}

async function loadModules() {
  try {
    const res = await get<any>('/sys-logs/modules')
    if (res.data?.code === 200) {
      moduleOptions.value = res.data.data
    }
  } catch (e) {
    console.error('加载模块列表失败', e)
  }
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handleClear() {
  Object.assign(searchForm, { keyword: '', module: '', status: '' })
  currentPage.value = 1
  loadData()
}

function handleExport() {
  ElMessage.success('日志导出功能开发中')
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadData()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

onMounted(() => {
  loadModules()
  loadData()
})
</script>

<template>
  <div class="list-page">
    <div class="page-header"><h2>系统日志</h2></div>
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索操作人 / 详情"
        clearable
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="searchForm.module" placeholder="模块筛选" clearable style="width: 160px">
        <el-option
          v-for="m in moduleOptions"
          :key="m.code"
          :label="m.description"
          :value="m.code"
        />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态筛选" clearable style="width: 120px">
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAILURE" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleClear">重置</el-button>
      <el-button type="success" @click="handleExport">导出日志</el-button>
    </div>
    <el-table
      :data="tableData"
      border
      stripe
      v-loading="loading"
      max-height="calc(100vh - 280px)"
    >
      <el-table-column label="操作人" width="130">
        <template #default="{ row }">
          <div>{{ row.operatorRealName || row.operatorUsername }}</div>
          <div v-if="row.operatorRealName && row.operatorUsername" style="font-size: 12px; color: #909399">
            {{ '@' + row.operatorUsername }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="moduleText" label="操作模块" width="130">
        <template #default="{ row }">
          <el-tag size="small">{{ row.moduleText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="actionText" label="操作类型" width="100" />
      <el-table-column prop="detail" label="操作详情" min-width="300" show-overflow-tooltip />
      <el-table-column label="操作对象" width="150" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.targetName">{{ row.targetName }}</span>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" label="IP地址" width="140" />
      <el-table-column prop="createTime" label="操作时间" width="180" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
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
  flex-wrap: wrap;
}
.search-input {
  width: 220px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
