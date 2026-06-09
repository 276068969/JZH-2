<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

interface User {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  role: string
  status: string
  createTime: string
}

const mockData: User[] = [
  { id: 1, username: 'admin', realName: '系统管理员', email: 'admin@prison.cn', phone: '13800000001', role: '超级管理员', status: '启用', createTime: '2024-01-01' },
  { id: 2, username: 'liujg', realName: '刘建国', email: 'liujg@prison.cn', phone: '13800000002', role: '监区长', status: '启用', createTime: '2024-01-15' },
  { id: 3, username: 'chenwd', realName: '陈卫东', email: 'chenwd@prison.cn', phone: '13800000003', role: '副监区长', status: '启用', createTime: '2024-02-01' },
  { id: 4, username: 'yangzq', realName: '杨志强', email: 'yangzq@prison.cn', phone: '13800000004', role: '警员', status: '启用', createTime: '2024-02-15' },
  { id: 5, username: 'zhaom', realName: '赵敏', email: 'zhaom@prison.cn', phone: '13800000005', role: '警员', status: '启用', createTime: '2024-03-01' },
  { id: 6, username: 'opviewer', realName: '查看员', email: 'viewer@prison.cn', phone: '13800000006', role: '访客', status: '禁用', createTime: '2024-03-15' }
]

const tableData = ref<User[]>(mockData)
const searchForm = reactive({ keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<User>({
  id: 0, username: '', realName: '', email: '', phone: '', role: '警员', status: '启用', createTime: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchForm.keyword) return tableData.value
  return tableData.value.filter((i) => i.username.includes(searchForm.keyword) || i.realName.includes(searchForm.keyword) || i.role.includes(searchForm.keyword))
})
const pagedData = computed(() => filteredData.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value))

function handleSearch() { currentPage.value = 1 }
function handleAdd() {
  dialogTitle.value = '新增用户'
  Object.assign(form, { id: 0, username: '', realName: '', email: '', phone: '', role: '警员', status: '启用', createTime: '' })
  dialogVisible.value = true
}
function handleEdit(row: User) { dialogTitle.value = '编辑用户'; Object.assign(form, { ...row }); dialogVisible.value = true }
function handleDelete(row: User) {
  ElMessageBox.confirm(`确定要删除用户 "${row.realName}" 吗？`, '确认删除', { type: 'warning' }).then(() => {
    tableData.value = tableData.value.filter((i) => i.id !== row.id)
    ElMessage.success('删除成功')
  })
}
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  if (form.id === 0) {
    form.id = Math.max(...tableData.value.map((i) => i.id), 0) + 1
    form.createTime = new Date().toISOString().split('T')[0]
    tableData.value.unshift({ ...form })
    ElMessage.success('新增成功')
  } else {
    const idx = tableData.value.findIndex((i) => i.id === form.id)
    if (idx > -1) tableData.value[idx] = { ...form }
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  loading.value = false
}
</script>

<template>
  <div class="list-page">
    <div class="page-header"><h2>用户管理</h2></div>
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索用户名 / 姓名 / 角色" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增用户</el-button>
    </div>
    <el-table :data="pagedData" border stripe>
      <el-table-column prop="username" label="用户名" width="110" />
      <el-table-column prop="realName" label="真实姓名" width="110" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="role" label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="row.role === '超级管理员' ? '' : row.role === '监区长' ? 'success' : 'info'" size="small">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === '启用' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="120" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[5,10,20]" :total="filteredData.length" layout="total, sizes, prev, pager, next" background />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="真实姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="角色" prop="role"><el-select v-model="form.role"><el-option label="超级管理员" value="超级管理员" /><el-option label="监区长" value="监区长" /><el-option label="副监区长" value="副监区长" /><el-option label="警员" value="警员" /><el-option label="访客" value="访客" /></el-select></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态"><el-select v-model="form.status"><el-option label="启用" value="启用" /><el-option label="禁用" value="禁用" /></el-select></el-form-item>
          </el-col>
        </el-row>
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
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.search-input { width: 260px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>