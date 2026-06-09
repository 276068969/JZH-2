<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

interface Log {
  id: number
  operator: string
  module: string
  action: string
  detail: string
  ipAddress: string
  status: string
  createTime: string
}

const mockData: Log[] = [
  { id: 1, operator: 'admin', module: '用户管理', action: '登录', detail: '用户 admin 登录系统', ipAddress: '192.168.1.100', status: '成功', createTime: '2024-08-13 08:00:23' },
  { id: 2, operator: 'liujg', module: '服刑人员管理', action: '新增', detail: '新增服刑人员 张三（编号: P2024001）', ipAddress: '192.168.1.101', status: '成功', createTime: '2024-08-13 08:15:42' },
  { id: 3, operator: 'chenwd', module: '巡查管理', action: '更新', detail: '完成 B区 巡查记录 #12', ipAddress: '192.168.1.102', status: '成功', createTime: '2024-08-13 09:02:15' },
  { id: 4, operator: 'yangzq', module: '事件管理', action: '新增', detail: '上报 A-101 打架事件', ipAddress: '192.168.1.103', status: '成功', createTime: '2024-08-13 10:30:00' },
  { id: 5, operator: 'admin', module: '角色管理', action: '更新', detail: '修改 "监区长" 角色权限', ipAddress: '192.168.1.100', status: '成功', createTime: '2024-08-13 11:00:00' },
  { id: 6, operator: 'zhaom', module: '访客管理', action: '删除', detail: '删除访客记录 #8', ipAddress: '192.168.1.104', status: '成功', createTime: '2024-08-13 14:20:35' },
  { id: 7, operator: 'admin', module: '系统管理', action: '备份', detail: '执行数据库备份操作', ipAddress: '192.168.1.100', status: '成功', createTime: '2024-08-13 23:00:00' },
  { id: 8, operator: 'opviewer', module: '用户管理', action: '登录', detail: '登录失败：密码错误', ipAddress: '192.168.1.200', status: '失败', createTime: '2024-08-14 02:15:33' },
  { id: 9, operator: 'liujg', module: '监区管理', action: '更新', detail: '修改 A区监区 最大容量为 500', ipAddress: '192.168.1.101', status: '成功', createTime: '2024-08-14 09:30:00' },
  { id: 10, operator: 'admin', module: '用户管理', action: '新增', detail: '新增用户 查看员 (opviewer)', ipAddress: '192.168.1.100', status: '成功', createTime: '2024-08-14 10:00:00' },
  { id: 11, operator: 'chenwd', module: '医疗记录', action: '更新', detail: '更新 孙七 的医疗记录', ipAddress: '192.168.1.102', status: '成功', createTime: '2024-08-14 11:15:00' },
  { id: 12, operator: 'yangzq', module: '巡查管理', action: '新增', detail: '新增 A区常规巡查记录', ipAddress: '192.168.1.103', status: '成功', createTime: '2024-08-14 14:00:00' }
]

const tableData = ref<Log[]>(mockData)
const searchForm = reactive({ keyword: '', module: '', status: '' })

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  let list = tableData.value
  if (searchForm.keyword) {
    list = list.filter((i) => i.operator.includes(searchForm.keyword) || i.detail.includes(searchForm.keyword))
  }
  if (searchForm.module) {
    list = list.filter((i) => i.module === searchForm.module)
  }
  if (searchForm.status) {
    list = list.filter((i) => i.status === searchForm.status)
  }
  return list
})
const pagedData = computed(() => filteredData.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value))

const modules = [...new Set(mockData.map((i) => i.module))]

function handleSearch() { currentPage.value = 1 }
function handleClear() { Object.assign(searchForm, { keyword: '', module: '', status: '' }); currentPage.value = 1 }
function handleExport() { ElMessage.success('日志导出成功（演示）') }
</script>

<template>
  <div class="list-page">
    <div class="page-header"><h2>系统日志</h2></div>
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索操作人 / 详情" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-select v-model="searchForm.module" placeholder="模块筛选" clearable style="width: 140px">
        <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态筛选" clearable style="width: 120px">
        <el-option label="成功" value="成功" />
        <el-option label="失败" value="失败" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleClear">重置</el-button>
      <el-button type="success" @click="handleExport">导出日志</el-button>
    </div>
    <el-table :data="pagedData" border stripe max-height="calc(100vh - 280px)">
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column prop="module" label="操作模块" width="120">
        <template #default="{ row }">
          <el-tag size="small">{{ row.module }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="action" label="操作类型" width="90" />
      <el-table-column prop="detail" label="操作详情" min-width="250" show-overflow-tooltip />
      <el-table-column prop="ipAddress" label="IP地址" width="140" />
      <el-table-column prop="createTime" label="操作时间" width="180" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === '成功' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10,20,50]" :total="filteredData.length" layout="total, sizes, prev, pager, next" background />
    </div>
  </div>
</template>

<style scoped>
.list-page { background: #fff; border-radius: 8px; padding: 20px; }
.page-header h2 { font-size: 18px; color: #303133; margin-bottom: 16px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.search-input { width: 220px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>