<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

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
  updatedAt: string
}

const mockData: Incident[] = [
  { id: 1, incidentDate: '2024-08-12', areaName: 'A区监区', category: '自残倾向', level: '严重', description: 'A-102监舍服刑人员表现出自残倾向，言语消极', handler: '刘建国', status: '处理中', result: '已隔离并安排心理疏导', updatedAt: '2024-08-12 16:30' },
  { id: 2, incidentDate: '2024-08-11', areaName: 'B区监区', category: '医疗急救', level: '紧急', description: 'B-203监舍服刑人员突发心脏病，需紧急送医', handler: '陈卫东', status: '处理中', result: '送医后脱离危险，住院观察中', updatedAt: '2024-08-12 10:15' },
  { id: 3, incidentDate: '2024-08-10', areaName: 'C区监区', category: '违反纪律', level: '轻微', description: 'C-301监舍女犯拒绝参加劳动改造', handler: '赵敏', status: '处理中', result: '进行思想教育中', updatedAt: '2024-08-11 09:00' },
  { id: 4, incidentDate: '2024-08-09', areaName: 'A区监区', category: '打架斗殴', level: '一般', description: 'A-101监舍两名服刑人员因琐事发生口角并动手，及时被制止', handler: '杨志强', status: '已处理', result: '对涉事人员分别禁闭3天', updatedAt: '2024-08-09 18:00' },
  { id: 5, incidentDate: '2024-08-08', areaName: 'D区隔离区', category: '违规物品', level: '一般', description: '例行检查中发现D-401室藏有自制锐器', handler: '张伟民', status: '已处理', result: '没收违禁品，相关责任人延长禁闭期限', updatedAt: '2024-08-08 20:30' },
  { id: 6, incidentDate: '2024-08-07', areaName: 'B区监区', category: '打架斗殴', level: '严重', description: 'B-205监舍发生群体性冲突，多人参与', handler: '陈卫东', status: '处理中', result: '现场已控制，涉事人员隔离审查', updatedAt: '2024-08-12 14:00' },
  { id: 7, incidentDate: '2024-08-06', areaName: 'A区监区', category: '逃跑未遂', level: '紧急', description: 'A区围墙发现攀爬痕迹，疑似有人企图越狱', handler: '杨志强', status: '已处理', result: '加强巡逻，增派岗哨，排查人员', updatedAt: '2024-08-07 08:00' },
  { id: 8, incidentDate: '2024-08-05', areaName: 'C区监区', category: '医疗急救', level: '一般', description: 'C-302监舍服刑人员跌倒受伤', handler: '赵敏', status: '已处理', result: '医务室处理后已回监舍', updatedAt: '2024-08-05 15:00' },
  { id: 9, incidentDate: '2024-08-04', areaName: 'D区隔离区', category: '自残倾向', level: '严重', description: 'D-403监舍服刑人员用头撞墙', handler: '张伟民', status: '处理中', result: '已约束保护，安排24小时看护', updatedAt: '2024-08-12 11:00' },
  { id: 10, incidentDate: '2024-08-03', areaName: 'B区监区', category: '违规物品', level: '轻微', description: 'B-201监舍发现藏有香烟', handler: '陈卫东', status: '已处理', result: '没收并警告', updatedAt: '2024-08-03 17:00' },
  { id: 11, incidentDate: '2024-08-02', areaName: 'A区监区', category: '违反纪律', level: '一般', description: 'A-103监舍服刑人员私自交换物品', handler: '刘建国', status: '已处理', result: '批评教育并记录', updatedAt: '2024-08-02 14:00' },
  { id: 12, incidentDate: '2024-08-01', areaName: 'C区监区', category: '打架斗殴', level: '紧急', description: 'C-303监舍发生持物伤人事件', handler: '赵敏', status: '处理中', result: '伤者已送医，肇事者已控制', updatedAt: '2024-08-12 09:30' }
]

const incidents = ref<Incident[]>(mockData)

const severityLevelMap: Record<string, number> = { '严重': 4, '紧急': 3, '一般': 2, '轻微': 1 }
const severityColorMap: Record<string, string> = { '严重': '#f56c6c', '紧急': '#e6a23c', '一般': '#409eff', '轻微': '#909399' }
const statusColorMap: Record<string, string> = { '处理中': '#e6a23c', '已处理': '#67c23a' }

const highSeverityCount = computed(() => incidents.value.filter(i => i.level === '严重' || i.level === '紧急').length)
const processingCount = computed(() => incidents.value.filter(i => i.status === '处理中').length)
const resolvedCount = computed(() => incidents.value.filter(i => i.status === '已处理').length)
const totalCount = computed(() => incidents.value.length)

const severityStats = computed(() => {
  const map: Record<string, number> = {}
  incidents.value.forEach(i => { map[i.level] = (map[i.level] || 0) + 1 })
  return Object.entries(map).sort((a, b) => (severityLevelMap[b[0]] || 0) - (severityLevelMap[a[0]] || 0))
})

const statusStats = computed(() => {
  const map: Record<string, number> = {}
  incidents.value.forEach(i => { map[i.status] = (map[i.status] || 0) + 1 })
  return Object.entries(map)
})

const areaStats = computed(() => {
  const map: Record<string, number> = {}
  incidents.value.forEach(i => { map[i.areaName] = (map[i.areaName] || 0) + 1 })
  return Object.entries(map).sort((a, b) => b[1] - a[1])
})

const categoryStats = computed(() => {
  const map: Record<string, number> = {}
  incidents.value.forEach(i => { map[i.category] = (map[i.category] || 0) + 1 })
  return Object.entries(map).sort((a, b) => b[1] - a[1])
})

const highSeverityIncidents = computed(() =>
  incidents.value
    .filter(i => i.level === '严重' || i.level === '紧急')
    .sort((a, b) => (severityLevelMap[b.level] || 0) - (severityLevelMap[a.level] || 0))
)

const processingIncidents = computed(() =>
  incidents.value
    .filter(i => i.status === '处理中')
    .sort((a, b) => (severityLevelMap[b.level] || 0) - (severityLevelMap[a.level] || 0))
)

const recentUpdates = computed(() =>
  [...incidents.value]
    .sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
    .slice(0, 8)
)

const severityChartRef = ref<HTMLDivElement>()
const statusChartRef = ref<HTMLDivElement>()
const areaChartRef = ref<HTMLDivElement>()
const categoryChartRef = ref<HTMLDivElement>()
let charts: echarts.ECharts[] = []

function initSeverityChart() {
  if (!severityChartRef.value) return
  const chart = echarts.init(severityChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}起 ({d}%)' },
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
      data: severityStats.value.map(([name, value]) => ({
        name, value,
        itemStyle: { color: severityColorMap[name] }
      }))
    }]
  })
  charts.push(chart)
}

function initStatusChart() {
  if (!statusChartRef.value) return
  const chart = echarts.init(statusChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '20px', containLabel: true },
    xAxis: { type: 'category', data: statusStats.value.map(([name]) => name), axisLabel: { fontSize: 13 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      barWidth: 48,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: (params: any) => statusColorMap[params.name] || '#409eff'
      },
      data: statusStats.value.map(([, value]) => value),
      label: { show: true, position: 'top', fontSize: 16, fontWeight: 'bold' }
    }]
  })
  charts.push(chart)
}

function initAreaChart() {
  if (!areaChartRef.value) return
  const chart = echarts.init(areaChartRef.value)
  const areaColorMap: Record<string, string> = { 'A区监区': '#f56c6c', 'B区监区': '#e6a23c', 'C区监区': '#409eff', 'D区隔离区': '#909399' }
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '20px', containLabel: true },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: areaStats.value.map(([name]) => name).reverse(), axisLabel: { fontSize: 13 } },
    series: [{
      type: 'bar',
      barWidth: 24,
      itemStyle: {
        borderRadius: [0, 6, 6, 0],
        color: (params: any) => {
          const name = areaStats.value.map(([n]) => n).reverse()[params.dataIndex]
          return areaColorMap[name] || '#409eff'
        }
      },
      data: areaStats.value.map(([, value]) => value).reverse(),
      label: { show: true, position: 'right', fontWeight: 'bold' }
    }]
  })
  charts.push(chart)
}

function initCategoryChart() {
  if (!categoryChartRef.value) return
  const chart = echarts.init(categoryChartRef.value)
  const catColors = ['#f56c6c', '#e6a23c', '#409eff', '#67c23a', '#909399', '#9b59b6', '#1abc9c']
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '20px', containLabel: true },
    xAxis: { type: 'category', data: categoryStats.value.map(([name]) => name), axisLabel: { fontSize: 12, rotate: 15 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      barWidth: 28,
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: (params: any) => catColors[params.dataIndex % catColors.length]
      },
      data: categoryStats.value.map(([, value]) => value),
      label: { show: true, position: 'top', fontWeight: 'bold' }
    }]
  })
  charts.push(chart)
}

function handleResize() {
  charts.forEach(c => c.resize())
}

function getLevelTagType(level: string) {
  if (level === '严重') return 'danger'
  if (level === '紧急') return 'warning'
  if (level === '一般') return ''
  return 'info'
}

function getStatusTagType(status: string) {
  if (status === '已处理') return 'success'
  return 'warning'
}

onMounted(async () => {
  await nextTick()
  initSeverityChart()
  initStatusChart()
  initAreaChart()
  initCategoryChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach(c => c.dispose())
  charts = []
})
</script>

<template>
  <div class="command-board">
    <div class="board-header">
      <h2>事件指挥看板</h2>
      <span class="board-subtitle">实时态势总览 · 值班领导研判专用</span>
    </div>

    <div class="stats-row">
      <div class="stat-card stat-danger">
        <div class="stat-icon-box" style="background: #fef0f0">
          <el-icon :size="28" color="#f56c6c"><WarningFilled /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value" style="color: #f56c6c">{{ highSeverityCount }}</span>
          <span class="stat-label">高危事件</span>
        </div>
        <div class="stat-badge" v-if="highSeverityCount > 0">需关注</div>
      </div>
      <div class="stat-card stat-processing">
        <div class="stat-icon-box" style="background: #fdf6ec">
          <el-icon :size="28" color="#e6a23c"><Clock /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value" style="color: #e6a23c">{{ processingCount }}</span>
          <span class="stat-label">处理中</span>
        </div>
      </div>
      <div class="stat-card stat-resolved">
        <div class="stat-icon-box" style="background: #f0f9eb">
          <el-icon :size="28" color="#67c23a"><CircleCheckFilled /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value" style="color: #67c23a">{{ resolvedCount }}</span>
          <span class="stat-label">已解决</span>
        </div>
      </div>
      <div class="stat-card stat-total">
        <div class="stat-icon-box" style="background: #ecf5ff">
          <el-icon :size="28" color="#409eff"><DataAnalysis /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value" style="color: #409eff">{{ totalCount }}</span>
          <span class="stat-label">事件总数</span>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <h3 class="chart-title">
          <el-icon><PieChart /></el-icon> 严重等级分布
        </h3>
        <div ref="severityChartRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <h3 class="chart-title">
          <el-icon><Histogram /></el-icon> 处置状态分布
        </h3>
        <div ref="statusChartRef" class="chart-box"></div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <h3 class="chart-title">
          <el-icon><OfficeBuilding /></el-icon> 发生区域分布
        </h3>
        <div ref="areaChartRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <h3 class="chart-title">
          <el-icon><TrendCharts /></el-icon> 事件类别统计
        </h3>
        <div ref="categoryChartRef" class="chart-box"></div>
      </div>
    </div>

    <div class="detail-row">
      <div class="detail-card">
        <h3 class="chart-title">
          <el-icon><Warning /></el-icon> 高危事件列表
          <el-tag type="danger" size="small" class="title-badge">{{ highSeverityCount }}</el-tag>
        </h3>
        <div class="incident-list">
          <div v-for="item in highSeverityIncidents" :key="item.id" class="incident-item severity-high">
            <div class="incident-item-header">
              <el-tag :type="getLevelTagType(item.level)" size="small" effect="dark">{{ item.level }}</el-tag>
              <el-tag :type="getStatusTagType(item.status)" size="small">{{ item.status }}</el-tag>
              <span class="incident-area">{{ item.areaName }}</span>
              <span class="incident-time">{{ item.incidentDate }}</span>
            </div>
            <div class="incident-item-desc">{{ item.description }}</div>
            <div class="incident-item-footer">
              <span class="incident-handler">处理人：{{ item.handler }}</span>
              <span class="incident-result">{{ item.result }}</span>
            </div>
          </div>
          <el-empty v-if="highSeverityIncidents.length === 0" description="暂无高危事件" :image-size="60" />
        </div>
      </div>

      <div class="detail-card">
        <h3 class="chart-title">
          <el-icon><Clock /></el-icon> 处理中事件
          <el-tag type="warning" size="small" class="title-badge">{{ processingCount }}</el-tag>
        </h3>
        <div class="incident-list">
          <div v-for="item in processingIncidents" :key="item.id" class="incident-item status-processing">
            <div class="incident-item-header">
              <el-tag :type="getLevelTagType(item.level)" size="small">{{ item.level }}</el-tag>
              <el-tag type="warning" size="small" effect="dark">{{ item.status }}</el-tag>
              <span class="incident-area">{{ item.areaName }}</span>
              <span class="incident-time">{{ item.incidentDate }}</span>
            </div>
            <div class="incident-item-desc">{{ item.description }}</div>
            <div class="incident-item-footer">
              <span class="incident-handler">处理人：{{ item.handler }}</span>
              <span class="incident-result">{{ item.result }}</span>
            </div>
          </div>
          <el-empty v-if="processingIncidents.length === 0" description="暂无处理中事件" :image-size="60" />
        </div>
      </div>
    </div>

    <div class="timeline-section">
      <h3 class="chart-title">
        <el-icon><Timer /></el-icon> 最近处置进展
      </h3>
      <div class="timeline-container">
        <el-timeline>
          <el-timeline-item
            v-for="item in recentUpdates"
            :key="item.id"
            :timestamp="item.updatedAt"
            placement="top"
            :type="item.status === '已处理' ? 'success' : (item.level === '严重' || item.level === '紧急' ? 'danger' : 'warning')"
            :hollow="item.status === '已处理'"
          >
            <div class="timeline-card">
              <div class="timeline-card-header">
                <el-tag :type="getLevelTagType(item.level)" size="small">{{ item.level }}</el-tag>
                <el-tag :type="getStatusTagType(item.status)" size="small">{{ item.status }}</el-tag>
                <span class="timeline-area">{{ item.areaName }}</span>
                <span class="timeline-category">{{ item.category }}</span>
              </div>
              <div class="timeline-desc">{{ item.description }}</div>
              <div class="timeline-result">处置进展：{{ item.result }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<style scoped>
.command-board {
  padding: 4px;
}

.board-header {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 20px;
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
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  transition: transform 0.2s, box-shadow 0.2s;
  border-left: 4px solid transparent;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.stat-danger {
  border-left-color: #f56c6c;
}

.stat-processing {
  border-left-color: #e6a23c;
}

.stat-resolved {
  border-left-color: #67c23a;
}

.stat-total {
  border-left-color: #409eff;
}

.stat-icon-box {
  width: 56px;
  height: 56px;
  border-radius: 12px;
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
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
}

.stat-badge {
  position: absolute;
  top: 12px;
  right: 16px;
  background: #fef0f0;
  color: #f56c6c;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: 600;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
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

.title-badge {
  margin-left: auto;
}

.chart-box {
  width: 100%;
  height: 280px;
}

.detail-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.detail-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.incident-list {
  max-height: 380px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.incident-item {
  border-radius: 8px;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  transition: all 0.2s;
}

.incident-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.incident-item.severity-high {
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
  border-left: 3px solid #f56c6c;
}

.incident-item.status-processing {
  background: linear-gradient(135deg, #fffbf0 0%, #fff 100%);
  border-left: 3px solid #e6a23c;
}

.incident-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.incident-area {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.incident-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-left: auto;
}

.incident-item-desc {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  margin-bottom: 8px;
}

.incident-item-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
}

.incident-handler {
  color: #909399;
}

.incident-result {
  color: #67c23a;
  font-weight: 500;
}

.timeline-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.timeline-container {
  max-height: 420px;
  overflow-y: auto;
  padding-right: 12px;
}

.timeline-card {
  background: #fafbfc;
  border-radius: 6px;
  padding: 12px 16px;
  border: 1px solid #f0f2f5;
}

.timeline-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.timeline-area {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.timeline-category {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.timeline-desc {
  font-size: 13px;
  color: #303133;
  line-height: 1.5;
  margin-bottom: 4px;
}

.timeline-result {
  font-size: 12px;
  color: #67c23a;
  font-weight: 500;
}

@media (max-width: 1200px) {
  .charts-row, .detail-row {
    grid-template-columns: 1fr;
  }
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
