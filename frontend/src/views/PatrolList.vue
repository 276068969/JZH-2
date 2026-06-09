<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

interface Patrol {
  id: number
  patrolDate: string
  patrolTime: string
  areaName: string
  guardName: string
  patrolType: string
  result: string
  description: string
}

const mockData: Patrol[] = [
  { id: 1, patrolDate: '2024-08-01', patrolTime: '08:00-10:00', areaName: 'A区监区', guardName: '杨志强', patrolType: '常规巡查', result: '正常', description: '各监舍秩序良好，无异常情况' },
  { id: 2, patrolDate: '2024-08-01', patrolTime: '14:00-16:00', areaName: 'B区监区', guardName: '陈卫东', patrolType: '专项巡查', result: '正常', description: '消防设施检查，灭火器均在有效期内' },
  { id: 3, patrolDate: '2024-08-01', patrolTime: '20:00-22:00', areaName: 'C区监区', guardName: '赵敏', patrolType: '夜间巡查', result: '正常', description: '夜间就寝情况良好，无串寝现象' },
  { id: 4, patrolDate: '2024-08-02', patrolTime: '08:00-10:00', areaName: 'D区隔离区', guardName: '张伟民', patrolType: '重点巡查', result: '异常', description: 'D-401禁闭室犯人情绪不稳定，已加派人员看守' },
  { id: 5, patrolDate: '2024-08-02', patrolTime: '10:00-12:00', areaName: 'A区监区', guardName: '刘建国', patrolType: '常规巡查', result: '正常', description: '劳动改造区域正常运转，无安全隐患' },
  { id: 6, patrolDate: '2024-08-02', patrolTime: '16:00-18:00', areaName: 'B区监区', guardName: '杨志强', patrolType: '突击检查', result: '正常', description: '抽查各监舍违禁品，未发现异常物品' }
]

const tableData = ref<Patrol[]>(mockData)
const searchForm = reactive({ keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<Patrol>({
  id: 0, patrolDate: '', patrolTime: '', areaName: '', guardName: '', patrolType: '', result: '正常', description: ''
})

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchForm.keyword) return tableData.value
  return tableData.value.filter((i) => i.areaName.includes(searchForm.keyword) || i.guardName.includes(searchForm.keyword) || i.result.includes(searchForm.keyword))
})
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

function handleSearch() { currentPage.value = 1 }
function handleAdd() {
  dialogTitle.value = '新增巡查记录'
  Object.assign(form, { id: 0, patrolDate: '', patrolTime: '', areaName: '', guardName: '', patrolType: '', result: '正常', description: '' })
  dialogVisible.value = true
}
function handleEdit(row: Patrol) { dialogTitle.value = '编辑巡查记录'; Object.assign(form, { ...row }); dialogVisible.value = true }
function handleDelete(row: Patrol) {
  ElMessageBox.confirm(`确定要删除该巡查记录吗？`, '确认删除', { type: 'warning' }).then(() => {
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
    <div class="page-header"><h2>巡查管理</h2></div>
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索监区 / 巡查人 / 结果" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增巡查</el-button>
    </div>
    <el-table :data="pagedData" border stripe>
      <el-table-column prop="patrolDate" label="巡查日期" width="110" />
      <el-table-column prop="patrolTime" label="巡查时段" width="130" />
      <el-table-column prop="areaName" label="巡查区域" width="110" />
      <el-table-column prop="guardName" label="巡查人" width="90" />
      <el-table-column prop="patrolType" label="巡查类型" width="100" />
      <el-table-column prop="result" label="结果" width="80">
        <template #default="{ row }">
          <el-tag :type="row.result === '正常' ? 'success' : 'danger'" size="small">{{ row.result }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="巡查描述" min-width="200" />
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
            <el-form-item label="巡查日期" prop="patrolDate">
              <el-date-picker v-model="form.patrolDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="巡查时段"><el-input v-model="form.patrolTime" placeholder="08:00-10:00" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="巡查区域"><el-input v-model="form.areaName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="巡查人"><el-input v-model="form.guardName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="巡查类型">
            <el-select v-model="form.patrolType">
              <el-option label="常规巡查" value="常规巡查" />
              <el-option label="专项巡查" value="专项巡查" />
              <el-option label="夜间巡查" value="夜间巡查" />
              <el-option label="重点巡查" value="重点巡查" />
              <el-option label="突击检查" value="突击检查" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结果"><el-select v-model="form.result"><el-option label="正常" value="正常" /><el-option label="异常" value="异常" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
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