<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

interface Incident {
  id: number
  incidentDate: string
  areaName: string
  category: string
  level: string
  description: string
  handler: string
  status: string
  result: string
}

const mockData: Incident[] = [
  { id: 1, incidentDate: '2024-07-15', areaName: 'A区监区', category: '打架斗殴', level: '一般', description: 'A-101监舍两名服刑人员因琐事发生口角并动手，及时被制止', handler: '杨志强', status: '已处理', result: '对涉事人员分别禁闭3天' },
  { id: 2, incidentDate: '2024-07-20', areaName: 'B区监区', category: '医疗急救', level: '紧急', description: 'B-203监舍服刑人员突发心脏病，需紧急送医', handler: '陈卫东', status: '已处理', result: '送医后脱离危险，住院观察中' },
  { id: 3, incidentDate: '2024-07-28', areaName: 'D区隔离区', category: '违规物品', level: '一般', description: '例行检查中发现D-401室藏有自制锐器', handler: '张伟民', status: '已处理', result: '没收违禁品，相关责任人延长禁闭期限' },
  { id: 4, incidentDate: '2024-08-05', areaName: 'C区监区', category: '违反纪律', level: '轻微', description: 'C-301监舍女犯拒绝参加劳动改造', handler: '赵敏', status: '处理中', result: '进行思想教育中' },
  { id: 5, incidentDate: '2024-08-10', areaName: 'A区监区', category: '自残倾向', level: '严重', description: 'A-102监舍服刑人员表现出自残倾向，言语消极', handler: '刘建国', status: '处理中', result: '已隔离并安排心理疏导' }
]

const tableData = ref<Incident[]>(mockData)
const searchForm = reactive({ keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<Incident>({
  id: 0, incidentDate: '', areaName: '', category: '', level: '一般', description: '', handler: '', status: '处理中', result: ''
})

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchForm.keyword) return tableData.value
  return tableData.value.filter((i) => i.category.includes(searchForm.keyword) || i.areaName.includes(searchForm.keyword) || i.level.includes(searchForm.keyword))
})
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

function handleSearch() { currentPage.value = 1 }
function handleAdd() {
  dialogTitle.value = '新增事件'
  Object.assign(form, { id: 0, incidentDate: '', areaName: '', category: '', level: '一般', description: '', handler: '', status: '处理中', result: '' })
  dialogVisible.value = true
}
function handleEdit(row: Incident) { dialogTitle.value = '编辑事件'; Object.assign(form, { ...row }); dialogVisible.value = true }
function handleDelete(row: Incident) {
  ElMessageBox.confirm('确定要删除该事件记录吗？', '确认删除', { type: 'warning' }).then(() => {
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
    <div class="page-header"><h2>事件管理</h2></div>
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索类别 / 区域 / 级别" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增事件</el-button>
    </div>
    <el-table :data="pagedData" border stripe>
      <el-table-column prop="incidentDate" label="日期" width="110" />
      <el-table-column prop="areaName" label="区域" width="100" />
      <el-table-column prop="category" label="类别" width="100" />
      <el-table-column prop="level" label="级别" width="80">
        <template #default="{ row }">
          <el-tag :type="row.level === '严重' ? 'danger' : row.level === '紧急' ? 'danger' : row.level === '一般' ? 'warning' : 'info'" size="small">{{ row.level }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="事件描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="handler" label="处理人" width="90" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '已处理' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="result" label="处理结果" min-width="150" show-overflow-tooltip />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="日期"><el-date-picker v-model="form.incidentDate" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="区域"><el-input v-model="form.areaName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="类别">
            <el-select v-model="form.category"><el-option label="打架斗殴" value="打架斗殴" /><el-option label="违反纪律" value="违反纪律" /><el-option label="违规物品" value="违规物品" /><el-option label="医疗急救" value="医疗急救" /><el-option label="自残倾向" value="自残倾向" /><el-option label="逃跑未遂" value="逃跑未遂" /><el-option label="其他" value="其他" /></el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="级别"><el-select v-model="form.level"><el-option label="严重" value="严重" /><el-option label="紧急" value="紧急" /><el-option label="一般" value="一般" /><el-option label="轻微" value="轻微" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="处理人"><el-input v-model="form.handler" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-select v-model="form.status"><el-option label="处理中" value="处理中" /><el-option label="已处理" value="已处理" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="事件描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="处理结果"><el-input v-model="form.result" type="textarea" :rows="2" /></el-form-item>
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