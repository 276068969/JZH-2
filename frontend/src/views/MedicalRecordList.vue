<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

interface MedicalRecord {
  id: number
  prisonerName: string
  prisonerNumber: string
  recordDate: string
  diagnosis: string
  treatment: string
  doctor: string
  hospital: string
  status: string
  remark: string
}

const mockData: MedicalRecord[] = [
  { id: 1, prisonerName: '张三', prisonerNumber: 'P2024001', recordDate: '2024-07-20', diagnosis: '感冒发烧', treatment: '口服退烧药、感冒灵', doctor: '李秋菊', hospital: '监狱医务室', status: '已治愈', remark: '体温38.5度，给药后次日退烧' },
  { id: 2, prisonerName: '孙七', prisonerNumber: 'P2024005', recordDate: '2024-07-22', diagnosis: '高血压', treatment: '口服降压药、定期监测血压', doctor: '王医生', hospital: '市第二人民医院', status: '治疗中', remark: '血压150/95，需长期服药控制' },
  { id: 3, prisonerName: '李四', prisonerNumber: 'P2024002', recordDate: '2024-08-01', diagnosis: '急性胃炎', treatment: '输液消炎、胃药', doctor: '李秋菊', hospital: '监狱医务室', status: '已治愈', remark: '饮食不当引起，禁食1天后好转' },
  { id: 4, prisonerName: '吴九', prisonerNumber: 'P2024007', recordDate: '2024-08-05', diagnosis: '抑郁症', treatment: '口服抗抑郁药、心理咨询', doctor: '陈文博', hospital: '省精神卫生中心', status: '治疗中', remark: '每周一次心理咨询，药物控制中' },
  { id: 5, prisonerName: '周八', prisonerNumber: 'P2024006', recordDate: '2024-08-08', diagnosis: '牙周炎', treatment: '口腔消炎、牙齿清洁', doctor: '刘口腔', hospital: '监狱医务室', status: '已治愈', remark: '已安排牙科治疗' }
]

const tableData = ref<MedicalRecord[]>(mockData)
const searchForm = reactive({ keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<MedicalRecord>({
  id: 0, prisonerName: '', prisonerNumber: '', recordDate: '', diagnosis: '', treatment: '', doctor: '', hospital: '监狱医务室', status: '治疗中', remark: ''
})

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchForm.keyword) return tableData.value
  return tableData.value.filter((i) => i.prisonerName.includes(searchForm.keyword) || i.diagnosis.includes(searchForm.keyword) || i.doctor.includes(searchForm.keyword))
})
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

function handleSearch() { currentPage.value = 1 }
function handleAdd() {
  dialogTitle.value = '新增医疗记录'
  Object.assign(form, { id: 0, prisonerName: '', prisonerNumber: '', recordDate: '', diagnosis: '', treatment: '', doctor: '', hospital: '监狱医务室', status: '治疗中', remark: '' })
  dialogVisible.value = true
}
function handleEdit(row: MedicalRecord) { dialogTitle.value = '编辑医疗记录'; Object.assign(form, { ...row }); dialogVisible.value = true }
function handleDelete(row: MedicalRecord) {
  ElMessageBox.confirm(`确定要删除 ${row.prisonerName} 的医疗记录吗？`, '确认删除', { type: 'warning' }).then(() => {
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
    <div class="page-header"><h2>医疗记录</h2></div>
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索姓名 / 诊断 / 医生" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增记录</el-button>
    </div>
    <el-table :data="pagedData" border stripe>
      <el-table-column prop="prisonerName" label="服刑人员" width="90" />
      <el-table-column prop="prisonerNumber" label="编号" width="110" />
      <el-table-column prop="recordDate" label="就诊日期" width="110" />
      <el-table-column prop="diagnosis" label="诊断" width="120" />
      <el-table-column prop="treatment" label="治疗方案" min-width="180" show-overflow-tooltip />
      <el-table-column prop="doctor" label="医生" width="90" />
      <el-table-column prop="hospital" label="就诊机构" width="140" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '已治愈' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" width="150" show-overflow-tooltip />
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
          <el-col :span="12"><el-form-item label="服刑人员"><el-input v-model="form.prisonerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="编号"><el-input v-model="form.prisonerNumber" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="就诊日期"><el-date-picker v-model="form.recordDate" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态"><el-select v-model="form.status"><el-option label="治疗中" value="治疗中" /><el-option label="已治愈" value="已治愈" /><el-option label="转院" value="转院" /></el-select></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="诊断"><el-input v-model="form.diagnosis" /></el-form-item>
        <el-form-item label="治疗方案"><el-input v-model="form.treatment" type="textarea" :rows="2" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="医生"><el-input v-model="form.doctor" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="就诊机构"><el-input v-model="form.hospital" /></el-form-item></el-col>
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