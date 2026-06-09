<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'

interface Prisoner {
  id: number
  name: string
  gender: string
  age: number
  prisonerNumber: string
  prisonArea: string
  cellNumber: string
  crimeType: string
  sentence: string
  entryDate: string
  releaseDate: string
  status: string
}

const mockData: Prisoner[] = [
  { id: 1, name: '张三', gender: '男', age: 35, prisonerNumber: 'P2024001', prisonArea: 'A区', cellNumber: 'A-101', crimeType: '盗窃罪', sentence: '5年', entryDate: '2024-01-15', releaseDate: '2029-01-14', status: '在押' },
  { id: 2, name: '李四', gender: '男', age: 42, prisonerNumber: 'P2024002', prisonArea: 'A区', cellNumber: 'A-102', crimeType: '诈骗罪', sentence: '8年', entryDate: '2024-02-20', releaseDate: '2032-02-19', status: '在押' },
  { id: 3, name: '王五', gender: '男', age: 28, prisonerNumber: 'P2024003', prisonArea: 'B区', cellNumber: 'B-201', crimeType: '故意伤害', sentence: '3年', entryDate: '2024-03-10', releaseDate: '2027-03-09', status: '在押' },
  { id: 4, name: '赵六', gender: '女', age: 31, prisonerNumber: 'P2024004', prisonArea: 'C区', cellNumber: 'C-301', crimeType: '贪污罪', sentence: '10年', entryDate: '2024-01-05', releaseDate: '2034-01-04', status: '在押' },
  { id: 5, name: '孙七', gender: '男', age: 45, prisonerNumber: 'P2024005', prisonArea: 'B区', cellNumber: 'B-202', crimeType: '抢劫罪', sentence: '6年', entryDate: '2024-04-18', releaseDate: '2030-04-17', status: '在押' },
  { id: 6, name: '周八', gender: '男', age: 38, prisonerNumber: 'P2024006', prisonArea: 'A区', cellNumber: 'A-103', crimeType: '走私罪', sentence: '7年', entryDate: '2024-05-22', releaseDate: '2031-05-21', status: '在押' },
  { id: 7, name: '吴九', gender: '女', age: 26, prisonerNumber: 'P2024007', prisonArea: 'C区', cellNumber: 'C-302', crimeType: '贩毒罪', sentence: '15年', entryDate: '2024-06-08', releaseDate: '2039-06-07', status: '在押' },
  { id: 8, name: '郑十', gender: '男', age: 33, prisonerNumber: 'P2024008', prisonArea: 'B区', cellNumber: 'B-203', crimeType: '非法经营', sentence: '4年', entryDate: '2024-07-12', releaseDate: '2028-07-11', status: '在押' }
]

const tableData = ref<Prisoner[]>(mockData)
const searchForm = reactive({ keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<Prisoner>({
  id: 0, name: '', gender: '男', age: 0, prisonerNumber: '',
  prisonArea: '', cellNumber: '', crimeType: '', sentence: '',
  entryDate: '', releaseDate: '', status: '在押'
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  prisonerNumber: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  prisonArea: [{ required: true, message: '请选择监区', trigger: 'change' }],
  cellNumber: [{ required: true, message: '请输入监舍号', trigger: 'blur' }]
}

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchForm.keyword) return tableData.value
  return tableData.value.filter((item) =>
    item.name.includes(searchForm.keyword) ||
    item.prisonerNumber.includes(searchForm.keyword) ||
    item.prisonArea.includes(searchForm.keyword)
  )
})

const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

function handleSearch() {
  currentPage.value = 1
}

function handleAdd() {
  dialogTitle.value = '新增服刑人员'
  Object.assign(form, { id: 0, name: '', gender: '男', age: 0, prisonerNumber: '', prisonArea: '', cellNumber: '', crimeType: '', sentence: '', entryDate: '', releaseDate: '', status: '在押' })
  dialogVisible.value = true
}

function handleEdit(row: Prisoner) {
  dialogTitle.value = '编辑服刑人员'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

function handleDelete(row: Prisoner) {
  ElMessageBox.confirm(`确定要删除服刑人员 "${row.name}" 吗？`, '确认删除', { type: 'warning' })
    .then(() => {
      tableData.value = tableData.value.filter((item) => item.id !== row.id)
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
    <div class="page-header">
      <h2>服刑人员管理</h2>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索姓名 / 编号 / 监区" clearable class="search-input" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增服刑人员</el-button>
    </div>

    <el-table :data="pagedData" border stripe style="width: 100%">
      <el-table-column prop="prisonerNumber" label="编号" width="110" />
      <el-table-column prop="name" label="姓名" width="80" />
      <el-table-column prop="gender" label="性别" width="60" />
      <el-table-column prop="age" label="年龄" width="60" />
      <el-table-column prop="prisonArea" label="监区" width="80" />
      <el-table-column prop="cellNumber" label="监舍号" width="90" />
      <el-table-column prop="crimeType" label="罪名" width="100" />
      <el-table-column prop="sentence" label="刑期" width="80" />
      <el-table-column prop="entryDate" label="入狱日期" width="110" />
      <el-table-column prop="releaseDate" label="释放日期" width="110" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === '在押' ? 'danger' : 'success'" size="small">{{ row.status }}</el-tag>
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
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20]"
        :total="filteredData.length"
        layout="total, sizes, prev, pager, next"
        background
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="编号" prop="prisonerNumber">
              <el-input v-model="form.prisonerNumber" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="18" :max="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="监区" prop="prisonArea">
              <el-select v-model="form.prisonArea">
                <el-option label="A区" value="A区" />
                <el-option label="B区" value="B区" />
                <el-option label="C区" value="C区" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="监舍号" prop="cellNumber">
              <el-input v-model="form.cellNumber" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="罪名">
              <el-input v-model="form.crimeType" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="刑期">
              <el-input v-model="form.sentence" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="入狱日期">
              <el-date-picker v-model="form.entryDate" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="释放日期">
              <el-date-picker v-model="form.releaseDate" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" />
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
.list-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.page-header h2 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 16px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input {
  width: 260px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>