<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

interface Guard {
  id: number
  name: string
  gender: string
  age: number
  badgeNumber: string
  department: string
  position: string
  phone: string
  entryDate: string
  status: string
}

const mockData: Guard[] = [
  { id: 1, name: '刘建国', gender: '男', age: 40, badgeNumber: 'G2023001', department: 'A区监区', position: '监区长', phone: '13800001001', entryDate: '2018-03-01', status: '在职' },
  { id: 2, name: '陈卫东', gender: '男', age: 35, badgeNumber: 'G2023002', department: 'B区监区', position: '副监区长', phone: '13800001002', entryDate: '2019-06-15', status: '在职' },
  { id: 3, name: '杨志强', gender: '男', age: 32, badgeNumber: 'G2023003', department: 'A区监区', position: '警员', phone: '13800001003', entryDate: '2020-01-10', status: '在职' },
  { id: 4, name: '赵敏', gender: '女', age: 29, badgeNumber: 'G2023004', department: 'C区监区', position: '警员', phone: '13800001004', entryDate: '2020-08-20', status: '在职' },
  { id: 5, name: '张伟民', gender: '男', age: 38, badgeNumber: 'G2023005', department: '巡查队', position: '队长', phone: '13800001005', entryDate: '2017-11-05', status: '在职' },
  { id: 6, name: '李秋菊', gender: '女', age: 31, badgeNumber: 'G2023006', department: '医疗室', position: '警员', phone: '13800001006', entryDate: '2021-03-12', status: '在职' }
]

const tableData = ref<Guard[]>(mockData)
const searchForm = reactive({ keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<Guard>({
  id: 0, name: '', gender: '男', age: 0, badgeNumber: '',
  department: '', position: '', phone: '', entryDate: '', status: '在职'
})

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchForm.keyword) return tableData.value
  return tableData.value.filter((item) =>
    item.name.includes(searchForm.keyword) ||
    item.badgeNumber.includes(searchForm.keyword) ||
    item.department.includes(searchForm.keyword)
  )
})

const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

function handleSearch() { currentPage.value = 1 }

function handleAdd() {
  dialogTitle.value = '新增警员'
  Object.assign(form, { id: 0, name: '', gender: '男', age: 0, badgeNumber: '', department: '', position: '', phone: '', entryDate: '', status: '在职' })
  dialogVisible.value = true
}

function handleEdit(row: Guard) {
  dialogTitle.value = '编辑警员'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

function handleDelete(row: Guard) {
  ElMessageBox.confirm(`确定要删除警员 "${row.name}" 吗？`, '确认删除', { type: 'warning' })
    .then(() => {
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
    <div class="page-header"><h2>警员管理</h2></div>
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索姓名 / 警号 / 部门" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增警员</el-button>
    </div>
    <el-table :data="pagedData" border stripe>
      <el-table-column prop="badgeNumber" label="警号" width="110" />
      <el-table-column prop="name" label="姓名" width="80" />
      <el-table-column prop="gender" label="性别" width="60" />
      <el-table-column prop="age" label="年龄" width="60" />
      <el-table-column prop="department" label="所属部门" width="120" />
      <el-table-column prop="position" label="职务" width="100" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="entryDate" label="入职日期" width="110" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === '在职' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
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
          <el-col :span="12">
            <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="警号" prop="badgeNumber"><el-input v-model="form.badgeNumber" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄"><el-input-number v-model="form.age" :min="18" :max="60" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门"><el-input v-model="form.department" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职务"><el-input v-model="form.position" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期">
              <el-date-picker v-model="form.entryDate" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
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