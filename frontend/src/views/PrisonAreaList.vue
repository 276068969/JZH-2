<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

interface PrisonArea {
  id: number
  areaName: string
  areaCode: string
  manager: string
  capacity: number
  currentCount: number
  cellCount: number
  description: string
  status: string
}

const mockData: PrisonArea[] = [
  { id: 1, areaName: 'A区监区', areaCode: 'AREA-A', manager: '刘建国', capacity: 500, currentCount: 432, cellCount: 16, description: '重型犯监区', status: '运行中' },
  { id: 2, areaName: 'B区监区', areaCode: 'AREA-B', manager: '陈卫东', capacity: 400, currentCount: 356, cellCount: 14, description: '普通犯监区', status: '运行中' },
  { id: 3, areaName: 'C区监区', areaCode: 'AREA-C', manager: '王海明', capacity: 350, currentCount: 310, cellCount: 12, description: '女犯监区', status: '运行中' },
  { id: 4, areaName: 'D区隔离区', areaCode: 'AREA-D', manager: '张伟民', capacity: 150, currentCount: 68, cellCount: 6, description: '禁闭/隔离区域', status: '运行中' }
]

const tableData = ref<PrisonArea[]>(mockData)
const searchForm = reactive({ keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<PrisonArea>({
  id: 0, areaName: '', areaCode: '', manager: '', capacity: 0,
  currentCount: 0, cellCount: 0, description: '', status: '运行中'
})

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchForm.keyword) return tableData.value
  return tableData.value.filter((i) => i.areaName.includes(searchForm.keyword) || i.areaCode.includes(searchForm.keyword) || i.manager.includes(searchForm.keyword))
})

const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

function handleSearch() { currentPage.value = 1 }
function handleAdd() {
  dialogTitle.value = '新增监区'
  Object.assign(form, { id: 0, areaName: '', areaCode: '', manager: '', capacity: 0, currentCount: 0, cellCount: 0, description: '', status: '运行中' })
  dialogVisible.value = true
}
function handleEdit(row: PrisonArea) {
  dialogTitle.value = '编辑监区'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}
function handleDelete(row: PrisonArea) {
  ElMessageBox.confirm(`确定要删除监区 "${row.areaName}" 吗？`, '确认删除', { type: 'warning' }).then(() => {
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
    <div class="page-header"><h2>监区管理</h2></div>
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索监区名称 / 编号 / 负责人" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增监区</el-button>
    </div>
    <el-table :data="pagedData" border stripe>
      <el-table-column prop="areaName" label="监区名称" width="130" />
      <el-table-column prop="areaCode" label="监区编号" width="120" />
      <el-table-column prop="manager" label="负责人" width="100" />
      <el-table-column prop="capacity" label="最大容量" width="90" />
      <el-table-column prop="currentCount" label="当前在押" width="90" />
      <el-table-column prop="cellCount" label="监舍数量" width="90" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag type="success" size="small">{{ row.status }}</el-tag>
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
      <el-form ref="formRef" :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="监区名称" prop="areaName"><el-input v-model="form.areaName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="编号" prop="areaCode"><el-input v-model="form.areaCode" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="负责人"><el-input v-model="form.manager" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="最大容量"><el-input-number v-model="form.capacity" :min="1" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="当前在押"><el-input-number v-model="form.currentCount" :min="0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="监舍数量"><el-input-number v-model="form.cellCount" :min="1" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
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