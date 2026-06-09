<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

interface Role {
  id: number
  roleName: string
  roleCode: string
  description: string
  userCount: number
  permissions: string[]
  createTime: string
  status: string
}

const allPermissions = ['dashboard', 'prisoners', 'guards', 'prisonAreas', 'cells', 'patrols', 'incidents', 'visitors', 'medical', 'users', 'roles', 'logs']

const mockData: Role[] = [
  { id: 1, roleName: '超级管理员', roleCode: 'ADMIN', description: '系统最高权限，可管理所有模块', userCount: 1, permissions: [...allPermissions], createTime: '2024-01-01', status: '启用' },
  { id: 2, roleName: '监区长', roleCode: 'WARDEN', description: '监区负责人，管理本监区事务', userCount: 2, permissions: ['dashboard', 'prisoners', 'guards', 'prisonAreas', 'cells', 'patrols', 'incidents', 'visitors', 'medical'], createTime: '2024-01-15', status: '启用' },
  { id: 3, roleName: '警员', roleCode: 'GUARD', description: '日常巡查和管理', userCount: 5, permissions: ['dashboard', 'prisoners', 'cells', 'patrols', 'incidents', 'visitors'], createTime: '2024-02-01', status: '启用' },
  { id: 4, roleName: '访客', roleCode: 'VIEWER', description: '仅查看权限', userCount: 1, permissions: ['dashboard', 'prisoners', 'guards'], createTime: '2024-03-01', status: '启用' }
]

const tableData = ref<Role[]>(mockData)
const searchForm = reactive({ keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<Role>({
  id: 0, roleName: '', roleCode: '', description: '', userCount: 0, permissions: [], createTime: '', status: '启用'
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchForm.keyword) return tableData.value
  return tableData.value.filter((i) => i.roleName.includes(searchForm.keyword) || i.roleCode.includes(searchForm.keyword))
})
const pagedData = computed(() => filteredData.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value))

function handleSearch() { currentPage.value = 1 }
function handleAdd() {
  dialogTitle.value = '新增角色'
  Object.assign(form, { id: 0, roleName: '', roleCode: '', description: '', userCount: 0, permissions: [], createTime: '', status: '启用' })
  dialogVisible.value = true
}
function handleEdit(row: Role) { dialogTitle.value = '编辑角色'; Object.assign(form, { ...row }); dialogVisible.value = true }
function handleDelete(row: Role) {
  ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '确认删除', { type: 'warning' }).then(() => {
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

const permLabels: Record<string, string> = {
  dashboard: '数据看板', prisoners: '服刑人员', guards: '警员管理',
  prisonAreas: '监区管理', cells: '监舍管理', patrols: '巡查管理',
  incidents: '事件管理', visitors: '访客管理', medical: '医疗记录',
  users: '用户管理', roles: '角色管理', logs: '系统日志'
}
</script>

<template>
  <div class="list-page">
    <div class="page-header"><h2>角色管理</h2></div>
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索角色名称 / 编码" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增角色</el-button>
    </div>
    <el-table :data="pagedData" border stripe>
      <el-table-column prop="roleName" label="角色名称" width="120" />
      <el-table-column prop="roleCode" label="角色编码" width="120" />
      <el-table-column prop="description" label="描述" width="200" show-overflow-tooltip />
      <el-table-column prop="userCount" label="用户数" width="80" />
      <el-table-column label="权限" min-width="300">
        <template #default="{ row }">
          <el-tag v-for="p in row.permissions" :key="p" size="small" style="margin: 2px 4px 2px 0">{{ permLabels[p] || p }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === '启用' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="角色名称" prop="roleName"><el-input v-model="form.roleName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="角色编码" prop="roleCode"><el-input v-model="form.roleCode" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="权限">
          <el-checkbox-group v-model="form.permissions">
            <el-checkbox v-for="p in allPermissions" :key="p" :label="p" :value="p">{{ permLabels[p] }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option label="启用" value="启用" /><el-option label="禁用" value="禁用" /></el-select>
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
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.search-input { width: 260px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>