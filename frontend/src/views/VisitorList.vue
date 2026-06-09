<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

interface Visitor {
  id: number
  visitorName: string
  relation: string
  idCard: string
  phone: string
  prisonerName: string
  visitDate: string
  visitTime: string
  status: string
  remark: string
}

const mockData: Visitor[] = [
  { id: 1, visitorName: '张美华', relation: '母亲', idCard: '320102197005120321', phone: '13900010001', prisonerName: '张三', visitDate: '2024-08-10', visitTime: '09:00-09:30', status: '已完成', remark: '' },
  { id: 2, visitorName: '李建国', relation: '父亲', idCard: '320102196803041025', phone: '13900010002', prisonerName: '李四', visitDate: '2024-08-12', visitTime: '10:00-10:30', status: '已完成', remark: '' },
  { id: 3, visitorName: '王丽', relation: '妻子', idCard: '320102199208152067', phone: '13900010003', prisonerName: '王五', visitDate: '2024-08-15', visitTime: '14:00-14:30', status: '已预约', remark: '结婚证已提交审核' },
  { id: 4, visitorName: '赵律师', relation: '律师', idCard: '320102198506102038', phone: '13900010004', prisonerName: '赵六', visitDate: '2024-08-16', visitTime: '15:00-15:30', status: '已预约', remark: '律师证号: 2023110401' },
  { id: 5, visitorName: '孙大明', relation: '兄弟', idCard: '320102198803154051', phone: '13900010005', prisonerName: '孙七', visitDate: '2024-08-18', visitTime: '09:00-09:30', status: '待审核', remark: '' }
]

const tableData = ref<Visitor[]>(mockData)
const searchForm = reactive({ keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<Visitor>({
  id: 0, visitorName: '', relation: '', idCard: '', phone: '', prisonerName: '', visitDate: '', visitTime: '', status: '已预约', remark: ''
})

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchForm.keyword) return tableData.value
  return tableData.value.filter((i) => i.visitorName.includes(searchForm.keyword) || i.prisonerName.includes(searchForm.keyword) || i.idCard.includes(searchForm.keyword))
})
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

function handleSearch() { currentPage.value = 1 }
function handleAdd() {
  dialogTitle.value = '新增访客记录'
  Object.assign(form, { id: 0, visitorName: '', relation: '', idCard: '', phone: '', prisonerName: '', visitDate: '', visitTime: '', status: '已预约', remark: '' })
  dialogVisible.value = true
}
function handleEdit(row: Visitor) { dialogTitle.value = '编辑访客记录'; Object.assign(form, { ...row }); dialogVisible.value = true }
function handleDelete(row: Visitor) {
  ElMessageBox.confirm(`确定要删除访客 "${row.visitorName}" 的记录吗？`, '确认删除', { type: 'warning' }).then(() => {
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
    <div class="page-header"><h2>访客管理</h2></div>
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索访客姓名 / 服刑人员 / 身份证号" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增访客记录</el-button>
    </div>
    <el-table :data="pagedData" border stripe>
      <el-table-column prop="visitorName" label="访客姓名" width="100" />
      <el-table-column prop="relation" label="关系" width="80" />
      <el-table-column prop="idCard" label="身份证号" width="180" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="prisonerName" label="被访人员" width="100" />
      <el-table-column prop="visitDate" label="会见日期" width="110" />
      <el-table-column prop="visitTime" label="会见时间" width="130" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '已完成' ? 'success' : row.status === '已预约' ? 'warning' : 'info'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" />
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
          <el-col :span="12"><el-form-item label="访客姓名" prop="visitorName"><el-input v-model="form.visitorName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="关系"><el-input v-model="form.relation" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="身份证号"><el-input v-model="form.idCard" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="被访人员"><el-input v-model="form.prisonerName" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="会见日期"><el-date-picker v-model="form.visitDate" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="会见时间"><el-input v-model="form.visitTime" placeholder="09:00-09:30" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-select v-model="form.status"><el-option label="已完成" value="已完成" /><el-option label="已预约" value="已预约" /><el-option label="待审核" value="待审核" /><el-option label="已取消" value="已取消" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
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