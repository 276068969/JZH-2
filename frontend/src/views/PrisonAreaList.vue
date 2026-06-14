<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { get, post, put, del } from '@/utils/request'

interface CellDetail {
  id: number
  cellNumber: string
  cellType: string
  capacity: number
  currentOccupancy: number
  status: string
  occupancyRate: number
}

interface PrisonAreaStats {
  id: number
  areaName: string
  areaCode: string
  areaType: string
  capacity: number
  currentPopulation: number
  status: string
  description: string
  cellCount: number
  fullCellCount: number
  availableCellCount: number
  maintenanceCellCount: number
  totalCellCapacity: number
  totalCellOccupancy: number
  occupancyRate: number
  cells?: CellDetail[]
}

const areaList = ref<PrisonAreaStats[]>([])
const selectedAreaId = ref<number | null>(null)
const selectedArea = ref<PrisonAreaStats | null>(null)
const cellList = ref<CellDetail[]>([])
const loading = ref(false)
const cellLoading = ref(false)

const searchForm = reactive({ keyword: '' })
const cellSearchForm = reactive({ keyword: '', statusFilter: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const formLoading = ref(false)

const form = reactive({
  id: 0,
  areaName: '',
  areaCode: '',
  areaType: '',
  capacity: 0,
  description: '',
  status: 'ACTIVE'
})

const cellDialogVisible = ref(false)
const cellDialogTitle = ref('')
const cellFormRef = ref<FormInstance>()
const cellFormLoading = ref(false)

const cellForm = reactive({
  id: 0,
  cellNumber: '',
  areaId: 0,
  cellType: '',
  capacity: 1,
  status: 'AVAILABLE'
})

const statusMap: Record<string, { label: string; type: string; color: string }> = {
  AVAILABLE: { label: '使用中', type: 'success', color: '#67c23a' },
  FULL: { label: '已满', type: 'danger', color: '#f56c6c' },
  MAINTENANCE: { label: '维护中', type: 'warning', color: '#e6a23c' }
}

const areaTypeMap: Record<string, string> = {
  MALE: '男监',
  FEMALE: '女监',
  JUVENILE: '少管所',
  HIGH_SECURITY: '高度戒备'
}

const cellTypeMap: Record<string, string> = {
  SINGLE: '单人',
  DOUBLE: '双人',
  MULTI: '多人',
  ISOLATION: '禁闭'
}

const filteredAreaList = computed(() => {
  if (!searchForm.keyword) return areaList.value
  const keyword = searchForm.keyword.toLowerCase()
  return areaList.value.filter(
    (i) =>
      i.areaName.toLowerCase().includes(keyword) ||
      i.areaCode.toLowerCase().includes(keyword)
  )
})

const filteredCellList = computed(() => {
  let list = cellList.value
  if (cellSearchForm.keyword) {
    const keyword = cellSearchForm.keyword.toLowerCase()
    list = list.filter(
      (i) =>
        i.cellNumber.toLowerCase().includes(keyword) ||
        cellTypeMap[i.cellType]?.toLowerCase().includes(keyword)
    )
  }
  if (cellSearchForm.statusFilter) {
    list = list.filter((i) => i.status === cellSearchForm.statusFilter)
  }
  return list
})

async function fetchAreaStats() {
  loading.value = true
  try {
    const res = await get('/prison-areas/stats')
    if (res.data.code === 200) {
      areaList.value = res.data.data
      if (areaList.value.length > 0 && !selectedAreaId.value) {
        selectArea(areaList.value[0].id)
      }
    }
  } catch (e) {
    console.error('获取监区统计失败', e)
    ElMessage.error('获取监区数据失败')
  } finally {
    loading.value = false
  }
}

async function fetchAreaDetail(id: number) {
  cellLoading.value = true
  try {
    const res = await get(`/prison-areas/${id}/stats`)
    if (res.data.code === 200) {
      selectedArea.value = res.data.data
      cellList.value = res.data.data.cells || []
    }
  } catch (e) {
    console.error('获取监区详情失败', e)
    ElMessage.error('获取监舍数据失败')
  } finally {
    cellLoading.value = false
  }
}

function selectArea(id: number) {
  selectedAreaId.value = id
  const area = areaList.value.find((a) => a.id === id)
  if (area) {
    selectedArea.value = area
  }
  fetchAreaDetail(id)
}

function handleSearch() {
  // 搜索已通过 computed 实现
}

function handleCellSearch() {
  // 搜索已通过 computed 实现
}

function handleAddArea() {
  dialogTitle.value = '新增监区'
  Object.assign(form, {
    id: 0,
    areaName: '',
    areaCode: '',
    areaType: 'MALE',
    capacity: 0,
    description: '',
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

function handleEditArea(row: PrisonAreaStats) {
  dialogTitle.value = '编辑监区'
  Object.assign(form, {
    id: row.id,
    areaName: row.areaName,
    areaCode: row.areaCode,
    areaType: row.areaType,
    capacity: row.capacity,
    description: row.description || '',
    status: row.status
  })
  dialogVisible.value = true
}

function handleDeleteArea(row: PrisonAreaStats) {
  ElMessageBox.confirm(`确定要删除监区 "${row.areaName}" 吗？`, '确认删除', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await del(`/prison-areas/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        if (selectedAreaId.value === row.id) {
          selectedAreaId.value = null
          selectedArea.value = null
          cellList.value = []
        }
        fetchAreaStats()
      }
    } catch (e) {
      console.error('删除失败', e)
    }
  })
}

async function handleAreaSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  formLoading.value = true
  try {
    let res
    if (form.id === 0) {
      res = await post('/prison-areas', form)
    } else {
      res = await put(`/prison-areas/${form.id}`, form)
    }
    if (res.data.code === 200) {
      ElMessage.success(form.id === 0 ? '新增成功' : '更新成功')
      dialogVisible.value = false
      fetchAreaStats()
    }
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    formLoading.value = false
  }
}

function handleAddCell() {
  if (!selectedAreaId.value) {
    ElMessage.warning('请先选择一个监区')
    return
  }
  cellDialogTitle.value = '新增监舍'
  Object.assign(cellForm, {
    id: 0,
    cellNumber: '',
    areaId: selectedAreaId.value,
    cellType: 'MULTI',
    capacity: 8,
    status: 'AVAILABLE'
  })
  cellDialogVisible.value = true
}

function handleEditCell(row: CellDetail) {
  cellDialogTitle.value = '编辑监舍'
  Object.assign(cellForm, {
    id: row.id,
    cellNumber: row.cellNumber,
    areaId: selectedAreaId.value,
    cellType: row.cellType,
    capacity: row.capacity,
    status: row.status
  })
  cellDialogVisible.value = true
}

function handleDeleteCell(row: CellDetail) {
  ElMessageBox.confirm(`确定要删除监舍 "${row.cellNumber}" 吗？`, '确认删除', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await del(`/cells/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        if (selectedAreaId.value) {
          fetchAreaDetail(selectedAreaId.value)
          fetchAreaStats()
        }
      }
    } catch (e) {
      console.error('删除失败', e)
    }
  })
}

async function handleCellSubmit() {
  const valid = await cellFormRef.value?.validate().catch(() => false)
  if (!valid) return
  cellFormLoading.value = true
  try {
    let res
    if (cellForm.id === 0) {
      res = await post('/cells', cellForm)
    } else {
      res = await put(`/cells/${cellForm.id}`, cellForm)
    }
    if (res.data.code === 200) {
      ElMessage.success(cellForm.id === 0 ? '新增成功' : '更新成功')
      cellDialogVisible.value = false
      if (selectedAreaId.value) {
        fetchAreaDetail(selectedAreaId.value)
        fetchAreaStats()
      }
    }
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    cellFormLoading.value = false
  }
}

function getProgressColor(rate: number): string {
  if (rate >= 100) return '#f56c6c'
  if (rate >= 80) return '#e6a23c'
  return '#67c23a'
}

onMounted(() => {
  fetchAreaStats()
})
</script>

<template>
  <div class="linkage-view">
    <div class="page-header">
      <h2>监区与监舍联动占用视图</h2>
      <p class="page-desc">从监区维度直观查看下属监舍的容量分布、满员情况与维护状态</p>
    </div>

    <div class="view-container">
      <div class="area-panel">
        <div class="panel-header">
          <h3>监区列表</h3>
          <span class="area-count">共 {{ areaList.length }} 个监区</span>
        </div>

        <div class="search-bar">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索监区名称/编号"
            clearable
            size="default"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleAddArea">
            <el-icon><Plus /></el-icon>
            新增监区
          </el-button>
        </div>

        <div class="area-card-list" v-loading="loading">
          <div
            v-for="area in filteredAreaList"
            :key="area.id"
            class="area-card"
            :class="{ active: selectedAreaId === area.id }"
            @click="selectArea(area.id)"
          >
            <div class="area-card-header">
              <div class="area-name">
                <span class="area-badge">{{ areaTypeMap[area.areaType] || area.areaType }}</span>
                <h4>{{ area.areaName }}</h4>
              </div>
              <el-tag size="small" :type="area.status === 'ACTIVE' ? 'success' : 'info'">
                {{ area.status === 'ACTIVE' ? '运行中' : '已停用' }}
              </el-tag>
            </div>

            <div class="area-code">编号：{{ area.areaCode }}</div>

            <div class="capacity-section">
              <div class="capacity-label">
                <span>人员容量</span>
                <span class="capacity-num">
                  <b>{{ area.currentPopulation || 0 }}</b> / {{ area.capacity || 0 }} 人
                </span>
              </div>
              <el-progress
                :percentage="Math.min(area.capacity ? Math.round((area.currentPopulation || 0) / area.capacity * 100) : 0, 100)"
                :stroke-width="8"
                :show-text="false"
                :color="getProgressColor(area.capacity ? (area.currentPopulation || 0) / area.capacity * 100 : 0)"
              />
            </div>

            <div class="cell-stats">
              <div class="stat-item">
                <div class="stat-value">{{ area.cellCount }}</div>
                <div class="stat-label">监舍总数</div>
              </div>
              <div class="stat-item available">
                <div class="stat-value">{{ area.availableCellCount }}</div>
                <div class="stat-label">空闲</div>
              </div>
              <div class="stat-item full">
                <div class="stat-value">{{ area.fullCellCount }}</div>
                <div class="stat-label">满员</div>
              </div>
              <div class="stat-item maintenance">
                <div class="stat-value">{{ area.maintenanceCellCount }}</div>
                <div class="stat-label">维护</div>
              </div>
            </div>

            <div class="occupancy-rate">
              <span>监舍使用率</span>
              <span class="rate-value" :style="{ color: getProgressColor(area.occupancyRate || 0) }">
                {{ area.occupancyRate?.toFixed(1) || '0.0' }}%
              </span>
            </div>

            <div class="card-actions">
              <el-button size="small" type="primary" link @click.stop="handleEditArea(area)">编辑</el-button>
              <el-button size="small" type="danger" link @click.stop="handleDeleteArea(area)">删除</el-button>
            </div>
          </div>

          <div v-if="filteredAreaList.length === 0 && !loading" class="empty-tip">
            <el-empty description="暂无监区数据" :image-size="80" />
          </div>
        </div>
      </div>

      <div class="cell-panel">
        <div class="panel-header" v-if="selectedArea">
          <div>
            <h3>{{ selectedArea.areaName }} - 监舍详情</h3>
            <div class="panel-subtitle">
              <span>编号：{{ selectedArea.areaCode }}</span>
              <span class="divider">|</span>
              <span>共 {{ cellList.length }} 间监舍</span>
              <span class="divider">|</span>
              <span>总容量 {{ selectedArea.totalCellCapacity }} 人</span>
              <span class="divider">|</span>
              <span>当前在押 {{ selectedArea.totalCellOccupancy }} 人</span>
            </div>
          </div>
        </div>

        <div class="panel-header empty-header" v-else>
          <h3>请选择左侧监区查看详情</h3>
        </div>

        <div class="cell-toolbar" v-if="selectedArea">
          <div class="toolbar-left">
            <el-input
              v-model="cellSearchForm.keyword"
              placeholder="搜索监舍号/类型"
              clearable
              size="default"
              style="width: 200px"
              @keyup.enter="handleCellSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="cellSearchForm.statusFilter"
              placeholder="全部状态"
              size="default"
              style="width: 120px"
              clearable
            >
              <el-option label="使用中" value="AVAILABLE" />
              <el-option label="已满" value="FULL" />
              <el-option label="维护中" value="MAINTENANCE" />
            </el-select>
          </div>
          <div class="toolbar-right">
            <el-button type="primary" @click="handleAddCell">
              <el-icon><Plus /></el-icon>
              新增监舍
            </el-button>
          </div>
        </div>

        <div class="cell-grid" v-loading="cellLoading" v-if="selectedArea">
          <div
            v-for="cell in filteredCellList"
            :key="cell.id"
            class="cell-card"
            :class="cell.status.toLowerCase()"
          >
            <div class="cell-card-header">
              <span class="cell-number">{{ cell.cellNumber }}</span>
              <el-tag size="small" :type="statusMap[cell.status]?.type || 'info'">
                {{ statusMap[cell.status]?.label || cell.status }}
              </el-tag>
            </div>

            <div class="cell-type">{{ cellTypeMap[cell.cellType] || cell.cellType }}监舍</div>

            <div class="cell-capacity">
              <div class="capacity-row">
                <span class="label">在押</span>
                <span class="value">
                  <b :style="{ color: getProgressColor(cell.occupancyRate || 0) }">
                    {{ cell.currentOccupancy || 0 }}
                  </b>
                  / {{ cell.capacity }} 人
                </span>
              </div>
              <el-progress
                :percentage="Math.min(cell.occupancyRate || 0, 100)"
                :stroke-width="6"
                :show-text="false"
                :color="getProgressColor(cell.occupancyRate || 0)"
              />
            </div>

            <div class="cell-occupancy">
              使用率：
              <span :style="{ color: getProgressColor(cell.occupancyRate || 0) }">
                {{ cell.occupancyRate?.toFixed(1) || '0.0' }}%
              </span>
            </div>

            <div class="cell-actions">
              <el-button size="small" type="primary" link @click="handleEditCell(cell)">编辑</el-button>
              <el-button size="small" type="danger" link @click="handleDeleteCell(cell)">删除</el-button>
            </div>
          </div>

          <div v-if="filteredCellList.length === 0 && !cellLoading" class="empty-tip cell-empty">
            <el-empty description="暂无符合条件的监舍" :image-size="80" />
          </div>
        </div>

        <div class="empty-panel" v-else>
          <el-icon :size="64" color="#c0c4cc"><OfficeBuilding /></el-icon>
          <p>从左侧选择一个监区，查看下属监舍的详细占用情况</p>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="监区名称" prop="areaName" :rules="[{ required: true, message: '请输入监区名称', trigger: 'blur' }]">
              <el-input v-model="form.areaName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="编号" prop="areaCode" :rules="[{ required: true, message: '请输入监区编号', trigger: 'blur' }]">
              <el-input v-model="form.areaCode" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="监区类型" prop="areaType" :rules="[{ required: true, message: '请选择监区类型', trigger: 'change' }]">
              <el-select v-model="form.areaType" style="width: 100%">
                <el-option label="男监" value="MALE" />
                <el-option label="女监" value="FEMALE" />
                <el-option label="少管所" value="JUVENILE" />
                <el-option label="高度戒备" value="HIGH_SECURITY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大容量">
              <el-input-number v-model="form.capacity" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status">
                <el-option label="运行中" value="ACTIVE" />
                <el-option label="已停用" value="INACTIVE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleAreaSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cellDialogVisible" :title="cellDialogTitle" width="500px" destroy-on-close>
      <el-form ref="cellFormRef" :model="cellForm" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="监舍号" prop="cellNumber" :rules="[{ required: true, message: '请输入监舍号', trigger: 'blur' }]">
              <el-input v-model="cellForm.cellNumber" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="监舍类型" prop="cellType" :rules="[{ required: true, message: '请选择监舍类型', trigger: 'change' }]">
              <el-select v-model="cellForm.cellType" style="width: 100%">
                <el-option label="单人" value="SINGLE" />
                <el-option label="双人" value="DOUBLE" />
                <el-option label="多人" value="MULTI" />
                <el-option label="禁闭" value="ISOLATION" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="容量" prop="capacity" :rules="[{ required: true, message: '请输入容量', trigger: 'blur' }]">
              <el-input-number v-model="cellForm.capacity" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="cellForm.status">
                <el-option label="使用中" value="AVAILABLE" />
                <el-option label="已满" value="FULL" />
                <el-option label="维护中" value="MAINTENANCE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="cellDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="cellFormLoading" @click="handleCellSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.linkage-view {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.page-header {
  margin-bottom: 16px;
  flex-shrink: 0;
}

.page-header h2 {
  font-size: 20px;
  color: #303133;
  margin: 0 0 4px 0;
}

.page-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.view-container {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 0;
}

.area-panel {
  width: 360px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.panel-header h3 {
  font-size: 16px;
  color: #303133;
  margin: 0;
}

.panel-subtitle {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.panel-subtitle .divider {
  margin: 0 8px;
  color: #dcdfe6;
}

.empty-header {
  justify-content: center;
  color: #c0c4cc;
}

.area-count {
  font-size: 12px;
  color: #909399;
}

.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.search-bar .el-input {
  flex: 1;
}

.area-card-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

.area-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 10px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.area-card:hover {
  border-color: #b3d8ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.area-card.active {
  border-color: #409eff;
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9ff 100%);
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.area-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.area-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.area-name h4 {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.area-badge {
  display: inline-block;
  padding: 2px 6px;
  font-size: 11px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
}

.area-code {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.capacity-section {
  margin-bottom: 12px;
}

.capacity-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.capacity-num b {
  font-size: 14px;
  color: #303133;
}

.cell-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
  margin-bottom: 10px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 6px;
}

.area-card.active .cell-stats {
  background: rgba(255, 255, 255, 0.6);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

.stat-item.available .stat-value {
  color: #67c23a;
}

.stat-item.full .stat-value {
  color: #f56c6c;
}

.stat-item.maintenance .stat-value {
  color: #e6a23c;
}

.occupancy-rate {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
  font-size: 12px;
  color: #606266;
}

.area-card.active .occupancy-rate {
  border-top-color: #d9ecff;
}

.rate-value {
  font-size: 15px;
  font-weight: 600;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
}

.area-card.active .card-actions {
  border-top-color: #d9ecff;
}

.empty-tip {
  text-align: center;
  padding: 20px;
}

.cell-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.cell-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  gap: 10px;
}

.cell-grid {
  flex: 1;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
  align-content: start;
  padding-right: 4px;
}

.cell-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px;
  transition: all 0.2s ease;
}

.cell-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.cell-card.full {
  border-color: #fbc4c4;
  background: linear-gradient(135deg, #fef0f0 0%, #fff 100%);
}

.cell-card.maintenance {
  border-color: #f5dab1;
  background: linear-gradient(135deg, #fdf6ec 0%, #fff 100%);
}

.cell-card.available {
  border-color: #c2e7b0;
  background: linear-gradient(135deg, #f0f9eb 0%, #fff 100%);
}

.cell-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.cell-number {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.cell-type {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.cell-capacity {
  margin-bottom: 8px;
}

.capacity-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.capacity-row .value b {
  font-size: 14px;
}

.cell-occupancy {
  font-size: 12px;
  color: #606266;
  margin-bottom: 10px;
}

.cell-actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
}

.cell-empty {
  grid-column: 1 / -1;
}

.empty-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}

.empty-panel p {
  margin-top: 16px;
  font-size: 14px;
}

@media (max-width: 1200px) {
  .view-container {
    flex-direction: column;
  }

  .area-panel {
    width: 100%;
    max-height: 300px;
  }

  .area-card-list {
    display: flex;
    gap: 10px;
    overflow-x: auto;
    overflow-y: hidden;
  }

  .area-card {
    min-width: 280px;
    margin-bottom: 0;
  }

  .cell-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
}
</style>
