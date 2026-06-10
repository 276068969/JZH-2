<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { get } from '@/utils/request'

interface Prisoner {
  id: number
  prisonerNumber: string
  name: string
  gender: string
  idCard: string
  birthDate: string
  nativePlace: string
  crimeType: string
  sentenceTerm: number
  entryDate: string
  releaseDate: string
  areaId: number
  cellId: number
  educationLevel: string
  maritalStatus: string
  occupation: string
  healthStatus: string
  dangerLevel: string
  status: string
  photoUrl: string
  remark: string
}

interface WarningGroup {
  days: number
  label: string
  count: number
  prisoners: Prisoner[]
}

const loading = ref(false)
const warningGroups = ref<WarningGroup[]>([])
const activeDays = ref<number | null>(null)
const filterForm = reactive({
  status: '',
  dangerLevel: ''
})

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '在押（普通释放）', value: 'INCARCERATED' },
  { label: '转监', value: 'TRANSFERRED' },
  { label: '保外就医', value: 'MEDICAL_PAROLE' }
]

const dangerLevelOptions = [
  { label: '全部等级', value: '' },
  { label: '低危', value: 'LOW' },
  { label: '中危', value: 'MEDIUM' },
  { label: '高危', value: 'HIGH' },
  { label: '极高危', value: 'EXTREME' }
]

const statusMap: Record<string, string> = {
  INCARCERATED: '在押',
  RELEASED: '已释放',
  TRANSFERRED: '转监',
  MEDICAL_PAROLE: '保外就医'
}

const dangerLevelMap: Record<string, string> = {
  LOW: '低危',
  MEDIUM: '中危',
  HIGH: '高危',
  EXTREME: '极高危'
}

const dangerLevelColor: Record<string, string> = {
  LOW: 'success',
  MEDIUM: 'warning',
  HIGH: 'danger',
  EXTREME: 'danger'
}

const summaryCards = computed(() => {
  return [
    { days: 30, label: '30天内', color: '#f56c6c', icon: 'AlarmClock' },
    { days: 60, label: '60天内', color: '#e6a23c', icon: 'Timer' },
    { days: 90, label: '90天内', color: '#409eff', icon: 'Clock' }
  ].map(card => {
    const group = warningGroups.value.find(g => g.days === card.days)
    return { ...card, count: group?.count || 0 }
  })
})

const currentPrisoners = computed(() => {
  if (activeDays.value === null) {
    return warningGroups.value.flatMap(g => g.prisoners)
  }
  const group = warningGroups.value.find(g => g.days === activeDays.value)
  return group?.prisoners || []
})

const daysTagMap: Record<number, { text: string; type: string }> = {
  30: { text: '30天内', type: 'danger' },
  60: { text: '60天内', type: 'warning' },
  90: { text: '90天内', type: '' }
}

function getPrisonerDaysTag(prisoner: Prisoner) {
  for (const group of warningGroups.value) {
    if (group.prisoners.some(p => p.id === prisoner.id)) {
      return daysTagMap[group.days]
    }
  }
  return { text: '', type: 'info' }
}

function daysUntilRelease(releaseDate: string) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const release = new Date(releaseDate)
  release.setHours(0, 0, 0, 0)
  const diff = release.getTime() - today.getTime()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

async function fetchWarnings() {
  loading.value = true
  try {
    const params: Record<string, string | number> = {}
    if (filterForm.status) params.status = filterForm.status
    if (filterForm.dangerLevel) params.dangerLevel = filterForm.dangerLevel

    const res = await get('/prisoners/release-warnings', params)
    if (res.data.code === 200) {
      warningGroups.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取预警数据失败', e)
  } finally {
    loading.value = false
  }
}

function handleCardClick(days: number) {
  activeDays.value = activeDays.value === days ? null : days
}

function handleFilter() {
  fetchWarnings()
}

function handleReset() {
  filterForm.status = ''
  filterForm.dangerLevel = ''
  activeDays.value = null
  fetchWarnings()
}

onMounted(() => {
  fetchWarnings()
})
</script>

<template>
  <div class="warning-page">
    <div class="page-header">
      <h2>临释人员预警查询</h2>
    </div>

    <div class="summary-row">
      <div
        v-for="card in summaryCards"
        :key="card.days"
        class="summary-card"
        :class="{ active: activeDays === card.days }"
        @click="handleCardClick(card.days)"
      >
        <div class="summary-icon" :style="{ background: card.color }">
          <el-icon :size="28" color="#fff">
            <component :is="card.icon" />
          </el-icon>
        </div>
        <div class="summary-info">
          <span class="summary-label">{{ card.label }}临释</span>
          <span class="summary-count" :style="{ color: card.color }">{{ card.count }}</span>
          <span class="summary-unit">人</span>
        </div>
      </div>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterForm.status" placeholder="人员状态" style="width: 200px" clearable>
        <el-option
          v-for="opt in statusOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-select v-model="filterForm.dangerLevel" placeholder="危险等级" style="width: 200px" clearable>
        <el-option
          v-for="opt in dangerLevelOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-button type="primary" @click="handleFilter">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="currentPrisoners"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="prisonerNumber" label="编号" width="120" />
      <el-table-column prop="name" label="姓名" width="80" />
      <el-table-column prop="gender" label="性别" width="60" />
      <el-table-column prop="crimeType" label="罪名" width="100" />
      <el-table-column prop="entryDate" label="入狱日期" width="110" />
      <el-table-column prop="releaseDate" label="释放日期" width="110" />
      <el-table-column label="剩余天数" width="100">
        <template #default="{ row }">
          <span
            class="days-remaining"
            :class="{
              urgent: daysUntilRelease(row.releaseDate) <= 30,
              warning: daysUntilRelease(row.releaseDate) > 30 && daysUntilRelease(row.releaseDate) <= 60
            }"
          >
            {{ daysUntilRelease(row.releaseDate) }} 天
          </span>
        </template>
      </el-table-column>
      <el-table-column label="预警等级" width="100">
        <template #default="{ row }">
          <el-tag
            :type="getPrisonerDaysTag(row).type"
            size="small"
          >
            {{ getPrisonerDaysTag(row).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 'INCARCERATED' ? '' : row.status === 'TRANSFERRED' ? 'warning' : 'success'"
            size="small"
          >
            {{ statusMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="危险等级" width="90">
        <template #default="{ row }">
          <el-tag
            :type="dangerLevelColor[row.dangerLevel] || 'info'"
            size="small"
            effect="dark"
          >
            {{ dangerLevelMap[row.dangerLevel] || row.dangerLevel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="nativePlace" label="籍贯" min-width="120" show-overflow-tooltip />
      <el-table-column prop="healthStatus" label="健康状况" width="90" />
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
    </el-table>
  </div>
</template>

<style scoped>
.warning-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.page-header h2 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 16px;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.summary-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  border: 2px solid transparent;
  background: #fafbfc;
  cursor: pointer;
  transition: all 0.3s ease;
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.summary-card.active {
  border-color: #409eff;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8f4fd 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.summary-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
  opacity: 0.85;
}

.summary-info {
  display: flex;
  flex-direction: column;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.summary-count {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
}

.summary-unit {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 2px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.days-remaining {
  font-weight: 600;
  color: #409eff;
}

.days-remaining.urgent {
  color: #f56c6c;
}

.days-remaining.warning {
  color: #e6a23c;
}
</style>
