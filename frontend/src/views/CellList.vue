<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

interface Cell {
  id: number
  cellNumber: string
  areaName: string
  capacity: number
  currentCount: number
  cellType: string
  facilities: string
  status: string
}

const mockData: Cell[] = [
  { id: 1, cellNumber: 'A-101', areaName: 'A区监区', capacity: 12, currentCount: 11, cellType: '普通监舍', facilities: '双层床、储物柜、洗漱间', status: '使用中' },
  { id: 2, cellNumber: 'A-102', areaName: 'A区监区', capacity: 12, currentCount: 10, cellType: '普通监舍', facilities: '双层床、储物柜、洗漱间', status: '使用中' },
  { id: 3, cellNumber: 'A-103', areaName: 'A区监区', capacity: 8, currentCount: 8, cellType: '严管监舍', facilities: '单层床、监控设备', status: '已满' },
  { id: 4, cellNumber: 'B-201', areaName: 'B区监区', capacity: 14, currentCount: 12, cellType: '普通监舍', facilities: '双层床、储物柜、电视', status: '使用中' },
  { id: 5, cellNumber: 'B-202', areaName: 'B区监区', capacity: 14, currentCount: 14, cellType: '普通监舍', facilities: '双层床、储物柜、电视', status: '已满' },
  { id: 6, cellNumber: 'C-301', areaName: 'C区监区', capacity: 10, currentCount: 8, cellType: '女犯监舍', facilities: '双层床、梳妆台、洗漱间', status: '使用中' },
  { id: 7, cellNumber: 'D-401', areaName: 'D区隔离区', capacity: 4, currentCount: 2, cellType: '禁闭室', facilities: '单层床、24小时监控', status: '使用中' },
  { id: 8, cellNumber: 'B-203', areaName: 'B区监区', capacity: 8, currentCount: 0, cellType: '普通监舍', facilities: '双层床、储物柜', status: '空闲' }
]

const tableData = ref<Cell[]>(mockData)
const searchForm = reactive({ keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<Cell>({
  id: 0, cellNumber: '', areaName: '', capacity: 0, currentCount: 0, cellType: '', facilities: '', status: '使用中'
})

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchForm.keyword) return tableData.value
  return tableData.value.filter((i) => i.cellNumber.includes(searchForm.keyword) || i.areaName.includes(searchForm.keyword))
})
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

function handleSearch() { currentPage.value = 1 }
function handleAdd() {
  dialogTitle.value = '新增监舍'
  Object.assign(form, { id: 0, cellNumber: '', areaName: '', capacity: 0, currentCount: 0, cellType: '', facilities: '', status: '使用中' })
  dialogVisible.value = true
}
function handleEdit(row: Cell) { dialogTitle.value = '编辑监舍'; Object.assign(form, { ...row }); dialogVisible.value = true }
function handleDelete(row: Cell) {
  ElMessageBox.confirm(`确定要删除监舍 "${row.cellNumber}" 吗？`, '确认删除', { type: 'warning' }).then(() => {
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
    <div class="page-header"><h2>监舍管理</h2></div>
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索监舍号 / 所属监区" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增监舍</el-button>
    </div>
    <el-table :data="pagedData" border stripe>
      <el-table-column prop="cellNumber" label="监舍号" width="100" />
      <el-table-column prop="areaName" label="所属监区" width="120" />
      <el-table-column prop="capacity" label="容量" width="70" />
      <el-table-column prop="currentCount" label="当前人数" width="90" />
      <el-table-column prop="cellType" label="监舍类型" width="100" />
      <el-table-column prop="facilities" label="设施" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '已满' ? 'danger' : row.status === '空闲' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="监舍号" prop="cellNumber"><el-input v-model="form.cellNumber" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所属监区"><el-input v-model="form.areaName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="容量"><el-input-number v-model="form.capacity" :min="1" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="当前人数">
              <div class="readonly-count">
                <span class="count-num">{{ form.currentCount }}</span>
                <span class="count-tip">人（系统自动统计）</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="类型"><el-input v-model="form.cellType" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-select v-model="form.status"><el-option label="使用中" value="使用中" /><el-option label="已满" value="已满" /><el-option label="空闲" value="空闲" /><el-option label="维护中" value="维护中" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="设施"><el-input v-model="form.facilities" /></el-form-item>
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

.readonly-count {
  display: flex;
  align-items: baseline;
  gap: 4px;
  padding: 0 11px;
  height: 32px;
  line-height: 32px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
.readonly-count .count-num {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}
.readonly-count .count-tip {
  font-size: 12px;
  color: #909399;
}
</style>