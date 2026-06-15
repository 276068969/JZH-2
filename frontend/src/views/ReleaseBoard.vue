<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, reactive } from 'vue'
import * as echarts from 'echarts'
import { get } from '@/utils/request'

interface RecentIncident {
  id: number
  incidentTitle: string
  incidentType: string
  severity: string
  status: string
  occurTime: string
}

interface BoardPrisoner {
  id: number
  prisonerNumber: string
  name: string
  gender: string
  crimeType: string
  releaseDate: string
  remainingDays: number
  warningLevel: string
  areaId: number
  areaName: string
  cellId: number | null
  dangerLevel: string
  status: string
  healthStatus: string
  remark: string
  recentIncidents: RecentIncident[]
}

interface Stats {
  total: number
  urgent30: number
  warning60: number
  notice90: number
  highDangerCount: number
  extremeDangerCount: number
}

interface AreaDistribution {
  areaName: string
  count: number
}

interface DangerDistribution {
  dangerLevel: string
  count: number
}

interface BoardData {
  stats: Stats
  areaDistribution: AreaDistribution[]
  dangerDistribution: DangerDistribution[]
  prisoners: BoardPrisoner[]
}

const mockBoardData: BoardData = {
  stats: { total: 13, urgent30: 3, warning60: 5, notice90: 5, highDangerCount: 2, extremeDangerCount: 1 },
  areaDistribution: [
    { areaName: 'A区监区', count: 4 },
    { areaName: 'B区监区', count: 3 },
    { areaName: 'C区监区', count: 3 },
    { areaName: 'D区隔离区', count: 3 }
  ],
  dangerDistribution: [
    { dangerLevel: 'EXTREME', count: 1 },
    { dangerLevel: 'HIGH', count: 2 },
    { dangerLevel: 'MEDIUM', count: 4 },
    { dangerLevel: 'LOW', count: 6 }
  ],
  prisoners: [
    { id: 1, prisonerNumber: 'P20240011', name: '杨某', gender: '男', crimeType: '盗窃罪', releaseDate: '2026-06-15', remainingDays: 0, warningLevel: 'EXTREME', areaId: 2, areaName: 'B区监区', cellId: null, dangerLevel: 'LOW', status: 'TRANSFERRED', healthStatus: '良好', remark: '已办理转监手续', recentIncidents: [] },
    { id: 2, prisonerNumber: 'P20240006', name: '吴某', gender: '男', crimeType: '盗窃罪', releaseDate: '2026-06-25', remainingDays: 10, warningLevel: 'EXTREME', areaId: 1, areaName: 'A区监区', cellId: null, dangerLevel: 'LOW', status: 'INCARCERATED', healthStatus: '良好', remark: '表现良好', recentIncidents: [] },
    { id: 3, prisonerNumber: 'P20240018', name: '宋某', gender: '女', crimeType: '诈骗罪', releaseDate: '2026-06-30', remainingDays: 15, warningLevel: 'HIGH', areaId: 3, areaName: 'C区监区', cellId: null, dangerLevel: 'LOW', status: 'INCARCERATED', healthStatus: '良好', remark: '', recentIncidents: [{ id: 1, incidentTitle: '违反纪律', incidentType: 'DISCIPLINE', severity: 'LOW', status: 'RESOLVED', occurTime: '2026-06-01 14:00' }] },
    { id: 4, prisonerNumber: 'P20240012', name: '刘某', gender: '男', crimeType: '诈骗罪', releaseDate: '2026-07-05', remainingDays: 20, warningLevel: 'MEDIUM', areaId: 3, areaName: 'C区监区', cellId: null, dangerLevel: 'MEDIUM', status: 'INCARCERATED', healthStatus: '良好', remark: '', recentIncidents: [] },
    { id: 5, prisonerNumber: 'P20240016', name: '罗某', gender: '男', crimeType: '盗窃罪', releaseDate: '2026-07-15', remainingDays: 30, warningLevel: 'MEDIUM', areaId: 1, areaName: 'A区监区', cellId: null, dangerLevel: 'LOW', status: 'INCARCERATED', healthStatus: '良好', remark: '', recentIncidents: [] },
    { id: 6, prisonerNumber: 'P20240008', name: '王某', gender: '男', crimeType: '故意伤害罪', releaseDate: '2026-07-20', remainingDays: 35, warningLevel: 'LOW', areaId: 1, areaName: 'A区监区', cellId: null, dangerLevel: 'HIGH', status: 'INCARCERATED', healthStatus: '一般', remark: '需重点关注', recentIncidents: [{ id: 2, incidentTitle: '打架事件', incidentType: 'FIGHT', severity: 'HIGH', status: 'PROCESSING', occurTime: '2026-06-10 09:30' }] },
    { id: 7, prisonerNumber: 'P20240013', name: '黄某', gender: '女', crimeType: '贪污罪', releaseDate: '2026-07-28', remainingDays: 43, warningLevel: 'LOW', areaId: 4, areaName: 'D区隔离区', cellId: null, dangerLevel: 'LOW', status: 'INCARCERATED', healthStatus: '良好', remark: '', recentIncidents: [] },
    { id: 8, prisonerNumber: 'P20240007', name: '郑某', gender: '男', crimeType: '诈骗罪', releaseDate: '2026-08-10', remainingDays: 56, warningLevel: 'LOW', areaId: 2, areaName: 'B区监区', cellId: null, dangerLevel: 'MEDIUM', status: 'INCARCERATED', healthStatus: '良好', remark: '', recentIncidents: [] },
    { id: 9, prisonerNumber: 'P20240015', name: '何某', gender: '男', crimeType: '贩毒罪', releaseDate: '2026-08-30', remainingDays: 76, warningLevel: 'LOW', areaId: 4, areaName: 'D区隔离区', cellId: null, dangerLevel: 'EXTREME', status: 'INCARCERATED', healthStatus: '良好', remark: '高度戒备区', recentIncidents: [{ id: 3, incidentTitle: '发现违规物品', incidentType: 'CONTRABAND', severity: 'CRITICAL', status: 'PENDING', occurTime: '2026-06-12 16:45' }] },
    { id: 10, prisonerNumber: 'P20240014', name: '许某', gender: '男', crimeType: '抢劫罪', releaseDate: '2026-08-20', remainingDays: 66, warningLevel: 'LOW', areaId: 4, areaName: 'D区隔离区', cellId: null, dangerLevel: 'HIGH', status: 'INCARCERATED', healthStatus: '较差', remark: '有慢性病', recentIncidents: [{ id: 4, incidentTitle: '医疗急救', incidentType: 'MEDICAL', severity: 'HIGH', status: 'RESOLVED', occurTime: '2026-06-05 08:20' }] },
    { id: 11, prisonerNumber: 'P20240019', name: '唐某', gender: '男', crimeType: '抢劫罪', releaseDate: '2026-08-25', remainingDays: 71, warningLevel: 'LOW', areaId: 2, areaName: 'B区监区', cellId: null, dangerLevel: 'MEDIUM', status: 'TRANSFERRED', healthStatus: '良好', remark: '转至低戒备区', recentIncidents: [] },
    { id: 12, prisonerNumber: 'P20240017', name: '梁某', gender: '男', crimeType: '故意伤害罪', releaseDate: '2026-09-05', remainingDays: 82, warningLevel: 'LOW', areaId: 3, areaName: 'C区监区', cellId: null, dangerLevel: 'MEDIUM', status: 'INCARCERATED', healthStatus: '一般', remark: '', recentIncidents: [] },
    { id: 13, prisonerNumber: 'P20240009', name: '冯某', gender: '女', crimeType: '贪污罪', releaseDate: '2026-09-01', remainingDays: 78, warningLevel: 'LOW', areaId: 1, areaName: 'A区监区', cellId: null, dangerLevel: 'LOW', status: 'MEDICAL_PAROLE', healthStatus: '一般', remark: '保外就医中', recentIncidents: [] }
  ]
}

const boardData = ref<BoardData>(mockBoardData)
const loading = ref(false)
const useMockData = ref(false)

const filterForm = reactive({
  status: '',
  dangerLevel: '',
  areaId: null as number | null,
  days: null as number | null
})

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '在押', value: 'INCARCERATED' },
  { label: '转监', value: 'TRANSFERRED' },
  { label: '保外就医', value: 'MEDICAL_PAROLE' }
]

const dangerLevelOptions = [
  { label: '全部等级', value: '' },
  { label: '极高危', value: 'EXTREME' },
  { label: '高危', value: 'HIGH' },
  { label: '中危', value: 'MEDIUM' },
  { label: '低危', value: 'LOW' }
]

const daysOptions = [
  { label: '90天内', value: 90 },
  { label: '60天内', value: 60 },
  { label: '30天内', value: 30 }
]

const areaOptions = ref<{ label: string; value: number }[]>([])

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

const warningLevelMap: Record<string, { text: string; color: string; bgColor: string }> = {
  EXTREME: { text: '极紧急', color: '#fff', bgColor: '#c45656' },
  HIGH: { text: '紧急', color: '#f56c6c', bgColor: '#fef0f0' },
  MEDIUM: { text: '关注', color: '#e6a23c', bgColor: '#fdf6ec' },
  LOW: { text: '一般', color: '#909399', bgColor: '#f4f4f5' }
}

const incidentTypeMap: Record<string, string> = {
  FIGHT: '打架',
  MEDICAL: '医疗',
  ESCAPE_ATTEMPT: '逃跑',
  DISCIPLINE: '违纪',
  CONTRABAND: '违规物品',
  OTHER: '其他'
}

const severityMap: Record<string, { text: string; type: string }> = {
  LOW: { text: '低', type: 'info' },
  MEDIUM: { text: '中', type: 'warning' },
  HIGH: { text: '高', type: 'danger' },
  CRITICAL: { text: '严重', type: 'danger' }
}

const incidentStatusMap: Record<string, { text: string; type: string }> = {
  PENDING: { text: '待处理', type: 'danger' },
  PROCESSING: { text: '处理中', type: 'warning' },
  RESOLVED: { text: '已解决', type: 'success' },
  CLOSED: { text: '已关闭', type: 'info' }
}

const activeFilter = ref<string>('all')

const statsCards = computed(() => {
  const s = boardData.value.stats
  return [
    { key: 'total', label: '临释总人数', value: s.total, icon: 'UserFilled', color: '#409eff', bg: '#ecf5ff', borderColor: '#409eff' },
    { key: 'urgent30', label: '30天内释放', value: s.urgent30, icon: 'WarningFilled', color: '#f56c6c', bg: '#fef0f0', borderColor: '#f56c6c' },
    { key: 'warning60', label: '60天内释放', value: s.warning60, icon: 'Warning', color: '#e6a23c', bg: '#fdf6ec', borderColor: '#e6a23c' },
    { key: 'notice90', label: '90天内释放', value: s.notice90, icon: 'Bell', color: '#409eff', bg: '#ecf5ff', borderColor: '#409eff' },
    { key: 'highDanger', label: '高危人员', value: s.highDangerCount, icon: 'StarFilled', color: '#e6a23c', bg: '#fdf6ec', borderColor: '#e6a23c' },
    { key: 'extremeDanger', label: '极高危人员', value: s.extremeDangerCount, icon: 'Place', color: '#f56c6c', bg: '#fef0f0', borderColor: '#f56c6c' }
  ]
})

const filteredPrisoners = computed(() => {
  let list = [...boardData.value.prisoners]

  if (activeFilter.value === 'urgent30') {
    list = list.filter(p => p.remainingDays <= 30)
  } else if (activeFilter.value === 'warning60') {
    list = list.filter(p => p.remainingDays > 30 && p.remainingDays <= 60)
  } else if (activeFilter.value === 'notice90') {
    list = list.filter(p => p.remainingDays > 60 && p.remainingDays <= 90)
  } else if (activeFilter.value === 'highDanger') {
    list = list.filter(p => p.dangerLevel === 'HIGH' || p.dangerLevel === 'EXTREME')
  }

  return list.sort((a, b) => a.remainingDays - b.remainingDays)
})

const areaChartRef = ref<HTMLDivElement>()
const dangerChartRef = ref<HTMLDivElement>()
let charts: echarts.ECharts[] = []

function initAreaChart() {
  if (!areaChartRef.value) return
  const chart = echarts.init(areaChartRef.value)
  const data = boardData.value.areaDistribution
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b59b6']

  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { fontSize: 13 } },
    series: [{
      type: 'pie',
      radius: ['40%', '68%'],
      center: ['38%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' }
      },
      data: data.map((item, idx) => ({
        name: item.areaName,
        value: item.count,
        itemStyle: { color: colors[idx % colors.length] }
      }))
    }]
  })
  charts.push(chart)
}

function initDangerChart() {
  if (!dangerChartRef.value) return
  const chart = echarts.init(dangerChartRef.value)
  const data = boardData.value.dangerDistribution
  const colorMap: Record<string, string> = {
    EXTREME: '#f56c6c',
    HIGH: '#e6a23c',
    MEDIUM: '#409eff',
    LOW: '#67c23a',
    UNKNOWN: '#909399'
  }

  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '20px', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => dangerLevelMap[d.dangerLevel] || d.dangerLevel),
      axisLabel: { fontSize: 13 }
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      barWidth: 48,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: (params: any) => {
          const dl = data[params.dataIndex]?.dangerLevel
          return colorMap[dl] || '#409eff'
        }
      },
      data: data.map(d => d.count),
      label: { show: true, position: 'top', fontSize: 16, fontWeight: 'bold' }
    }]
  })
  charts.push(chart)
}

function handleFilterClick(key: string) {
  activeFilter.value = activeFilter.value === key ? 'all' : key
}

function handleReset() {
  filterForm.status = ''
  filterForm.dangerLevel = ''
  filterForm.areaId = null
  filterForm.days = null
  activeFilter.value = 'all'
}

async function fetchFromAPI() {
  loading.value = true
  try {
    const params: Record<string, string | number> = {}
    if (filterForm.status) params.status = filterForm.status
    if (filterForm.dangerLevel) params.dangerLevel = filterForm.dangerLevel
    if (filterForm.areaId) params.areaId = filterForm.areaId
    if (filterForm.days) params.days = filterForm.days

    const res = await get('/prisoners/release-board', params)
    if (res.data.code === 200 && res.data.data) {
      boardData.value = res.data.data
      useMockData.value = false
      await nextTick()
      charts.forEach(c => c.dispose())
      charts = []
      initAreaChart()
      initDangerChart()
    } else {
      boardData.value = mockBoardData
      useMockData.value = true
    }
  } catch (e) {
    console.warn('API请求失败，使用模拟数据', e)
    boardData.value = mockBoardData
    useMockData.value = true
  } finally {
    loading.value = false
  }
}

async function fetchAreas() {
  try {
    const res = await get('/prison-areas/all')
    if (res.data.code === 200 && Array.isArray(res.data.data)) {
      areaOptions.value = res.data.data.map((a: any) => ({
        label: a.areaName,
        value: a.id
      }))
    }
  } catch (e) {
    console.warn('获取监区列表失败', e)
  }
}

function getRemainingDaysStyle(days: number) {
  if (days <= 7) return { color: '#fff', bg: '#c45656', fontWeight: 700 }
  if (days <= 15) return { color: '#f56c6c', bg: '#fef0f0', fontWeight: 700 }
  if (days <= 30) return { color: '#e6a23c', bg: '#fdf6ec', fontWeight: 600 }
  return { color: '#409eff', bg: '#ecf5ff', fontWeight: 500 }
}

function getDangerTagType(level: string) {
  if (level === 'EXTREME') return 'danger'
  if (level === 'HIGH') return 'warning'
  if (level === 'MEDIUM') return ''
  return 'info'
}

function handleResize() {
  charts.forEach(c => c.resize())
}

onMounted(async () => {
  await fetchAreas()
  await fetchFromAPI()
  await nextTick()
  initAreaChart()
  initDangerChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach(c => c.dispose())
  charts = []
})
</script>

<template>
  <div class="release-board">
    <div class="board-header">
      <div class="header-left">
        <h2>临释名单运营看板</h2>
        <span class="board-subtitle">值班与管理研判专用 · 快速核对与提前准备</span>
      </div>
      <div class="header-right">
        <el-tag v-if="useMockData" type="info" size="small">模拟数据</el-tag>
      </div>
    </div>

    <div class="stats-row">
      <div
        v-for="card in statsCards"
        :key="card.key"
        class="stat-card"
        :class="{ active: activeFilter === card.key }"
        :style="{ borderLeftColor: card.borderColor }"
        @click="handleFilterClick(card.key)"
      >
        <div class="stat-icon-box" :style="{ background: card.bg }">
          <el-icon :size="26" :color="card.color"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value" :style="{ color: card.color }">{{ card.value }}</span>
          <span class="stat-label">{{ card.label }}</span>
        </div>
        <div v-if="card.key === 'urgent30' && boardData.stats.urgent30 > 0" class="stat-badge pulse">需核对</div>
        <div v-if="card.key === 'extremeDanger' && boardData.stats.extremeDangerCount > 0" class="stat-badge pulse">重点</div>
      </div>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterForm.status" placeholder="人员状态" style="width: 150px" clearable @change="fetchFromAPI">
        <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-select v-model="filterForm.dangerLevel" placeholder="危险等级" style="width: 150px" clearable @change="fetchFromAPI">
        <el-option v-for="opt in dangerLevelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-select v-model="filterForm.areaId" placeholder="所属监区" style="width: 150px" clearable @change="fetchFromAPI">
        <el-option v-for="opt in areaOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-select v-model="filterForm.days" placeholder="时间范围" style="width: 150px" clearable @change="fetchFromAPI">
        <el-option v-for="opt in daysOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <h3 class="chart-title">
          <el-icon><OfficeBuilding /></el-icon> 监区分布
        </h3>
        <div ref="areaChartRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <h3 class="chart-title">
          <el-icon><Warning /></el-icon> 危险等级分布
        </h3>
        <div ref="dangerChartRef" class="chart-box"></div>
      </div>
    </div>

    <div class="cards-section">
      <h3 class="section-title">
        <el-icon><List /></el-icon> 临释人员卡片
        <el-tag type="info" size="small" class="title-badge">{{ filteredPrisoners.length }}人</el-tag>
      </h3>
      <div class="prisoner-cards" v-loading="loading">
        <div
          v-for="p in filteredPrisoners"
          :key="p.id"
          class="prisoner-card"
          :class="{
            'card-extreme': p.remainingDays <= 7,
            'card-urgent': p.remainingDays > 7 && p.remainingDays <= 15,
            'card-warning': p.remainingDays > 15 && p.remainingDays <= 30,
            'card-has-incident': p.recentIncidents && p.recentIncidents.length > 0
          }"
        >
          <div class="card-top">
            <div class="card-identity">
              <span class="card-name">{{ p.name }}</span>
              <span class="card-number">{{ p.prisonerNumber }}</span>
            </div>
            <div class="card-days-badge" :style="{ background: getRemainingDaysStyle(p.remainingDays).bg, color: getRemainingDaysStyle(p.remainingDays).color }">
              <span class="days-number" :style="{ fontWeight: getRemainingDaysStyle(p.remainingDays).fontWeight }">{{ p.remainingDays }}</span>
              <span class="days-label">天</span>
            </div>
          </div>

          <div class="card-info-row">
            <div class="info-item">
              <span class="info-label">释放日期</span>
              <span class="info-value">{{ p.releaseDate }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">罪名</span>
              <span class="info-value">{{ p.crimeType }}</span>
            </div>
          </div>

          <div class="card-tags-row">
            <el-tag :type="warningLevelMap[p.warningLevel]?.bgColor === '#c45656' ? 'danger' : (p.warningLevel === 'HIGH' ? 'danger' : (p.warningLevel === 'MEDIUM' ? 'warning' : 'info'))" size="small" effect="dark">
              {{ warningLevelMap[p.warningLevel]?.text || '一般' }}
            </el-tag>
            <el-tag :type="getDangerTagType(p.dangerLevel)" size="small" effect="dark">
              {{ dangerLevelMap[p.dangerLevel] || p.dangerLevel }}
            </el-tag>
            <el-tag
              :type="p.status === 'INCARCERATED' ? '' : (p.status === 'TRANSFERRED' ? 'warning' : 'success')"
              size="small"
            >
              {{ statusMap[p.status] || p.status }}
            </el-tag>
          </div>

          <div class="card-area-row">
            <el-icon :size="14" color="#909399"><OfficeBuilding /></el-icon>
            <span class="area-text">{{ p.areaName || '未分配监区' }}</span>
            <span v-if="p.healthStatus && p.healthStatus !== '良好'" class="health-warn">
              <el-icon :size="12" color="#e6a23c"><WarningFilled /></el-icon>
              {{ p.healthStatus }}
            </span>
          </div>

          <div v-if="p.remark" class="card-remark">
            <el-icon :size="12" color="#909399"><Document /></el-icon>
            <span>{{ p.remark }}</span>
          </div>

          <div v-if="p.recentIncidents && p.recentIncidents.length > 0" class="card-incidents">
            <div class="incidents-header">
              <el-icon :size="14" color="#f56c6c"><WarningFilled /></el-icon>
              <span>近期事件 ({{ p.recentIncidents.length }})</span>
            </div>
            <div v-for="inc in p.recentIncidents.slice(0, 3)" :key="inc.id" class="incident-row">
              <el-tag
                :type="severityMap[inc.severity]?.type || 'info'"
                size="small"
              >
                {{ severityMap[inc.severity]?.text || inc.severity }}
              </el-tag>
              <span class="incident-title">{{ inc.incidentTitle || incidentTypeMap[inc.incidentType] || inc.incidentType }}</span>
              <el-tag
                :type="incidentStatusMap[inc.status]?.type || 'info'"
                size="small"
                effect="plain"
              >
                {{ incidentStatusMap[inc.status]?.text || inc.status }}
              </el-tag>
            </div>
            <div v-if="p.recentIncidents.length > 3" class="incident-more">
              还有 {{ p.recentIncidents.length - 3 }} 条事件...
            </div>
          </div>
        </div>

        <el-empty v-if="filteredPrisoners.length === 0" description="暂无符合条件的临释人员" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.release-board {
  padding: 4px;
}

.board-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.board-header h2 {
  font-size: 20px;
  color: #303133;
  font-weight: 700;
  margin: 0;
}

.board-subtitle {
  font-size: 13px;
  color: #909399;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 14px;
  position: relative;
  transition: transform 0.2s, box-shadow 0.2s;
  border-left: 4px solid transparent;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.stat-card.active {
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.2);
  background: linear-gradient(135deg, #f0f7ff 0%, #fff 100%);
}

.stat-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-body {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.stat-badge {
  position: absolute;
  top: 8px;
  right: 10px;
  background: #fef0f0;
  color: #f56c6c;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 8px;
  font-weight: 600;
}

.pulse {
  animation: pulse-anim 2s infinite;
}

@keyframes pulse-anim {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.chart-box {
  width: 100%;
  height: 280px;
}

.cards-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-badge {
  margin-left: 8px;
}

.prisoner-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}

.prisoner-card {
  border-radius: 10px;
  padding: 18px 20px;
  border: 1px solid #ebeef5;
  background: #fff;
  transition: all 0.25s ease;
  position: relative;
}

.prisoner-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.prisoner-card.card-extreme {
  background: linear-gradient(135deg, #fff5f5 0%, #fff 60%);
  border-left: 4px solid #c45656;
}

.prisoner-card.card-urgent {
  background: linear-gradient(135deg, #fff5f5 0%, #fff 60%);
  border-left: 4px solid #f56c6c;
}

.prisoner-card.card-warning {
  background: linear-gradient(135deg, #fffbf0 0%, #fff 60%);
  border-left: 4px solid #e6a23c;
}

.prisoner-card.card-has-incident {
  border-right: 3px solid #f56c6c;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-identity {
  display: flex;
  flex-direction: column;
}

.card-name {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.card-number {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.card-days-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 14px;
  border-radius: 10px;
  min-width: 64px;
}

.days-number {
  font-size: 28px;
  line-height: 1;
}

.days-label {
  font-size: 11px;
  opacity: 0.8;
  margin-top: 2px;
}

.card-info-row {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 11px;
  color: #c0c4cc;
  margin-bottom: 2px;
}

.info-value {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.card-tags-row {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.card-area-row {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
}

.area-text {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.health-warn {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-left: auto;
  font-size: 12px;
  color: #e6a23c;
}

.card-remark {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  padding: 4px 8px;
  background: #f9f9f9;
  border-radius: 4px;
}

.card-incidents {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
}

.incidents-header {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #f56c6c;
  font-weight: 600;
  margin-bottom: 8px;
}

.incident-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 0;
  font-size: 12px;
}

.incident-title {
  flex: 1;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.incident-more {
  font-size: 11px;
  color: #c0c4cc;
  text-align: center;
  padding-top: 4px;
}

@media (max-width: 1400px) {
  .stats-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-row {
    grid-template-columns: 1fr;
  }
  .prisoner-cards {
    grid-template-columns: 1fr;
  }
}
</style>
