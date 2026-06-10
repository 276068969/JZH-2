<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { get } from '@/utils/request'

interface Prisoner {
  id: number
  prisonerNumber: string
  name: string
  gender: string
  crimeType: string
  entryDate: string
  releaseDate: string
  nativePlace: string
  healthStatus: string
  dangerLevel: string
  status: string
  remark: string
}

const mockPrisoners: Prisoner[] = [
  { id: 1, prisonerNumber: 'P20240006', name: '吴某', gender: '男', crimeType: '盗窃罪', entryDate: '2025-01-10', releaseDate: '2026-06-25', nativePlace: '广东省中山市', healthStatus: '良好', dangerLevel: 'LOW', status: 'INCARCERATED', remark: '表现良好' },
  { id: 2, prisonerNumber: 'P20240011', name: '杨某', gender: '男', crimeType: '盗窃罪', entryDate: '2025-09-01', releaseDate: '2026-06-15', nativePlace: '广东省湛江市', healthStatus: '良好', dangerLevel: 'LOW', status: 'TRANSFERRED', remark: '已办理转监手续' },
  { id: 3, prisonerNumber: 'P20240012', name: '刘某', gender: '男', crimeType: '诈骗罪', entryDate: '2024-03-15', releaseDate: '2026-07-05', nativePlace: '广东省韶关市', healthStatus: '良好', dangerLevel: 'MEDIUM', status: 'INCARCERATED', remark: '' },
  { id: 4, prisonerNumber: 'P20240008', name: '王某', gender: '男', crimeType: '故意伤害罪', entryDate: '2025-03-01', releaseDate: '2026-07-20', nativePlace: '广东省江门市', healthStatus: '一般', dangerLevel: 'HIGH', status: 'INCARCERATED', remark: '需重点关注' },
  { id: 5, prisonerNumber: 'P20240013', name: '黄某', gender: '女', crimeType: '贪污罪', entryDate: '2024-01-20', releaseDate: '2026-07-28', nativePlace: '广东省清远市', healthStatus: '良好', dangerLevel: 'LOW', status: 'INCARCERATED', remark: '' },
  { id: 6, prisonerNumber: 'P20240007', name: '郑某', gender: '男', crimeType: '诈骗罪', entryDate: '2024-08-20', releaseDate: '2026-08-10', nativePlace: '广东省惠州市', healthStatus: '良好', dangerLevel: 'MEDIUM', status: 'INCARCERATED', remark: '' },
  { id: 7, prisonerNumber: 'P20240014', name: '许某', gender: '男', crimeType: '抢劫罪', entryDate: '2023-12-01', releaseDate: '2026-08-20', nativePlace: '广东省梅州市', healthStatus: '较差', dangerLevel: 'HIGH', status: 'INCARCERATED', remark: '有慢性病' },
  { id: 8, prisonerNumber: 'P20240009', name: '冯某', gender: '女', crimeType: '贪污罪', entryDate: '2024-06-01', releaseDate: '2026-09-01', nativePlace: '广东省肇庆市', healthStatus: '一般', dangerLevel: 'LOW', status: 'MEDICAL_PAROLE', remark: '保外就医中' },
  { id: 9, prisonerNumber: 'P20240015', name: '何某', gender: '男', crimeType: '贩毒罪', entryDate: '2022-05-10', releaseDate: '2026-08-30', nativePlace: '广东省揭阳市', healthStatus: '良好', dangerLevel: 'EXTREME', status: 'INCARCERATED', remark: '高度戒备区' },
  { id: 10, prisonerNumber: 'P20240016', name: '罗某', gender: '男', crimeType: '盗窃罪', entryDate: '2023-06-15', releaseDate: '2026-07-15', nativePlace: '广东省河源市', healthStatus: '良好', dangerLevel: 'LOW', status: 'INCARCERATED', remark: '' },
  { id: 11, prisonerNumber: 'P20240017', name: '梁某', gender: '男', crimeType: '故意伤害罪', entryDate: '2021-09-20', releaseDate: '2026-09-05', nativePlace: '广东省阳江市', healthStatus: '一般', dangerLevel: 'MEDIUM', status: 'INCARCERATED', remark: '' },
  { id: 12, prisonerNumber: 'P20240018', name: '宋某', gender: '女', crimeType: '诈骗罪', entryDate: '2023-04-10', releaseDate: '2026-06-30', nativePlace: '广东省云浮市', healthStatus: '良好', dangerLevel: 'LOW', status: 'INCARCERATED', remark: '' },
  { id: 13, prisonerNumber: 'P20240019', name: '唐某', gender: '男', crimeType: '抢劫罪', entryDate: '2020-11-05', releaseDate: '2026-08-25', nativePlace: '广东省潮州市', healthStatus: '良好', dangerLevel: 'MEDIUM', status: 'TRANSFERRED', remark: '转至低戒备区' }
]

const allPrisoners = ref<Prisoner[]>([])
const loading = ref(false)
const activeDays = ref<number | null>(null)
const filterForm = reactive({
  status: '',
  dangerLevel: ''
})
const useMockData = ref(false)

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

function parseLocalDate(dateStr: string): Date {
  if (!dateStr) return new Date(NaN)
  const parts = dateStr.split('-')
  if (parts.length === 3) {
    return new Date(
      parseInt(parts[0], 10),
      parseInt(parts[1], 10) - 1,
      parseInt(parts[2], 10)
    )
  }
  return new Date(dateStr)
}

function daysUntilRelease(releaseDate: string): number {
  try {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const release = parseLocalDate(releaseDate)
    release.setHours(0, 0, 0, 0)
    if (isNaN(release.getTime())) return -1
    return Math.ceil((release.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  } catch (e) {
    return -1
  }
}

function getEarliestDays(prisoner: Prisoner): number | null {
  const d = daysUntilRelease(prisoner.releaseDate)
  if (d < 0) return null
  if (d <= 30) return 30
  if (d <= 60) return 60
  if (d <= 90) return 90
  return null
}

const filteredPrisoners = computed(() => {
  try {
    return allPrisoners.value.filter(p => {
      const days = daysUntilRelease(p.releaseDate)
      if (days < 0 || days > 90) return false
      if (filterForm.status && p.status !== filterForm.status) return false
      if (filterForm.dangerLevel && p.dangerLevel !== filterForm.dangerLevel) return false
      return true
    })
  } catch (e) {
    console.error('filteredPrisoners error', e)
    return []
  }
})

const warning30 = computed(() => filteredPrisoners.value.filter(p => {
  const d = daysUntilRelease(p.releaseDate)
  return d >= 0 && d <= 30
}))
const warning60 = computed(() => filteredPrisoners.value.filter(p => {
  const d = daysUntilRelease(p.releaseDate)
  return d > 30 && d <= 60
}))
const warning90 = computed(() => filteredPrisoners.value.filter(p => {
  const d = daysUntilRelease(p.releaseDate)
  return d > 60 && d <= 90
}))

const summaryCards = computed(() => [
  { days: 30, label: '30天内', color: '#f56c6c', count: warning30.value.length },
  { days: 60, label: '60天内', color: '#e6a23c', count: warning60.value.length },
  { days: 90, label: '90天内', color: '#409eff', count: warning90.value.length }
])

const currentPrisoners = computed(() => {
  try {
    if (activeDays.value === null) return filteredPrisoners.value
    if (activeDays.value === 30) return warning30.value
    if (activeDays.value === 60) return warning60.value
    return warning90.value
  } catch (e) {
    console.error('currentPrisoners error', e)
    return []
  }
})

const daysTagMap: Record<number, { text: string; type: string }> = {
  30: { text: '30天内', type: 'danger' },
  60: { text: '60天内', type: 'warning' },
  90: { text: '90天内', type: 'primary' }
}

function getPrisonerDaysTag(prisoner: Prisoner) {
  const earliest = getEarliestDays(prisoner)
  if (earliest && daysTagMap[earliest]) return daysTagMap[earliest]
  return { text: '', type: 'info' }
}

function handleCardClick(days: number) {
  activeDays.value = activeDays.value === days ? null : days
}

function handleFilter() {
  activeDays.value = null
}

function handleReset() {
  filterForm.status = ''
  filterForm.dangerLevel = ''
  activeDays.value = null
}

function loadMockData() {
  useMockData.value = true
  allPrisoners.value = [...mockPrisoners]
}

async function fetchFromAPI() {
  loading.value = true
  try {
    const params: Record<string, string> = {}
    if (filterForm.status) params.status = filterForm.status
    if (filterForm.dangerLevel) params.dangerLevel = filterForm.dangerLevel

    const res = await get('/prisoners/release-warnings', params)
    if (res && res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
      const groups = res.data.data as any[]
      const merged: Prisoner[] = []
      const seen = new Set<number>()
      groups.forEach(g => {
        if (Array.isArray(g.prisoners)) {
          g.prisoners.forEach((p: any) => {
            if (!seen.has(p.id)) {
              seen.add(p.id)
              merged.push({
                id: p.id,
                prisonerNumber: p.prisonerNumber || '',
                name: p.name || '',
                gender: p.gender || '',
                crimeType: p.crimeType || '',
                entryDate: p.entryDate || '',
                releaseDate: p.releaseDate || '',
                nativePlace: p.nativePlace || '',
                healthStatus: p.healthStatus || '',
                dangerLevel: p.dangerLevel || '',
                status: p.status || '',
                remark: p.remark || ''
              })
            }
          })
        }
      })
      if (merged.length > 0) {
        useMockData.value = false
        allPrisoners.value = merged
      } else {
        loadMockData()
      }
    } else {
      loadMockData()
    }
  } catch (e) {
    console.warn('API 请求失败，使用模拟数据', e)
    loadMockData()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFromAPI().catch(err => {
    console.error('onMounted error', err)
    loadMockData()
  })
})
</script>

<template>
  <div class="warning-page">
    <div class="page-header">
      <h2>临释人员预警查询</h2>
      <el-tag v-if="useMockData" type="info" size="small" style="margin-left: 12px">当前使用模拟数据</el-tag>
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
          <el-icon :size="28" color="#fff"><WarningFilled /></el-icon>
        </div>
        <div class="summary-info">
          <span class="summary-label">{{ card.label }}临释</span>
          <span class="summary-count" :style="{ color: card.color }">{{ card.count }}</span>
          <span class="summary-unit">人</span>
        </div>
      </div>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterForm.status" placeholder="人员状态" style="width: 200px" clearable @change="handleFilter">
        <el-option
          v-for="opt in statusOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-select v-model="filterForm.dangerLevel" placeholder="危险等级" style="width: 200px" clearable @change="handleFilter">
        <el-option
          v-for="opt in dangerLevelOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-button type="primary" :loading="loading" @click="fetchFromAPI">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="currentPrisoners"
      border
      stripe
      style="width: 100%"
      empty-text="暂无符合条件的临释人员"
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
              urgent: daysUntilRelease(row.releaseDate) >= 0 && daysUntilRelease(row.releaseDate) <= 30,
              warning: daysUntilRelease(row.releaseDate) > 30 && daysUntilRelease(row.releaseDate) <= 60
            }"
          >
            {{ daysUntilRelease(row.releaseDate) >= 0 ? daysUntilRelease(row.releaseDate) + ' 天' : '已过期' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="预警等级" width="100">
        <template #default="{ row }">
          <el-tag v-if="getPrisonerDaysTag(row).text" :type="getPrisonerDaysTag(row).type" size="small">
            {{ getPrisonerDaysTag(row).text }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 'INCARCERATED' ? 'primary' : row.status === 'TRANSFERRED' ? 'warning' : 'success'"
            size="small"
          >
            {{ statusMap[row.status] || row.status || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="危险等级" width="90">
        <template #default="{ row }">
          <el-tag :type="dangerLevelColor[row.dangerLevel] || 'info'" size="small" effect="dark">
            {{ dangerLevelMap[row.dangerLevel] || row.dangerLevel || '-' }}
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

.page-header {
  display: flex;
  align-items: center;
}

.page-header h2 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 16px 0;
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
