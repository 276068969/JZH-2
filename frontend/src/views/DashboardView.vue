<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const prisonerChart = ref<HTMLDivElement>()
const cellChart = ref<HTMLDivElement>()
const incidentChart = ref<HTMLDivElement>()
const patrolChart = ref<HTMLDivElement>()

let charts: echarts.ECharts[] = []

const statsCards = [
  { label: '在押总人数', value: 1248, icon: 'UserFilled', color: '#409eff', unit: '人' },
  { label: '在职警员', value: 86, icon: 'Avatar', color: '#67c23a', unit: '人' },
  { label: '监舍总数', value: 48, icon: 'HomeFilled', color: '#e6a23c', unit: '间' },
  { label: '本月事件', value: 15, icon: 'WarningFilled', color: '#f56c6c', unit: '起' }
]

function initPrisonerChart() {
  if (!prisonerChart.value) return
  const chart = echarts.init(prisonerChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['在押人数', '新入监', '释放'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '40px', top: '20px', containLabel: true },
    xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'] },
    yAxis: { type: 'value' },
    series: [
      {
        name: '在押人数',
        type: 'bar',
        barWidth: 14,
        itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
        data: [1180, 1195, 1210, 1225, 1200, 1230, 1245, 1260, 1248, 1235, 1220, 1248]
      },
      {
        name: '新入监',
        type: 'line',
        smooth: true,
        itemStyle: { color: '#67c23a' },
        data: [30, 35, 28, 42, 25, 38, 32, 45, 22, 18, 20, 28]
      },
      {
        name: '释放',
        type: 'line',
        smooth: true,
        itemStyle: { color: '#e6a23c' },
        data: [25, 20, 32, 27, 45, 20, 28, 33, 35, 36, 38, 15]
      }
    ]
  })
  charts.push(chart)
}

function initCellChart() {
  if (!cellChart.value) return
  const chart = echarts.init(cellChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: '5%', top: 'center' },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: [
        { value: 35, name: '已满', itemStyle: { color: '#f56c6c' } },
        { value: 10, name: '空闲', itemStyle: { color: '#67c23a' } },
        { value: 3, name: '维护中', itemStyle: { color: '#e6a23c' } }
      ]
    }]
  })
  charts.push(chart)
}

function initIncidentChart() {
  if (!incidentChart.value) return
  const chart = echarts.init(incidentChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '20px', containLabel: true },
    xAxis: { type: 'category', data: ['违纪', '医疗', '逃跑', '打架', '违规物品', '其他'] },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      barWidth: 24,
      itemStyle: { borderRadius: [4, 4, 0, 0] },
      data: [
        { value: 8, itemStyle: { color: '#f56c6c' } },
        { value: 5, itemStyle: { color: '#409eff' } },
        { value: 1, itemStyle: { color: '#e6a23c' } },
        { value: 3, itemStyle: { color: '#f56c6c' } },
        { value: 4, itemStyle: { color: '#909399' } },
        { value: 2, itemStyle: { color: '#67c23a' } }
      ]
    }]
  })
  charts.push(chart)
}

function initPatrolChart() {
  if (!patrolChart.value) return
  const chart = echarts.init(patrolChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['计划巡查', '已完成'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '40px', top: '20px', containLabel: true },
    xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
    yAxis: { type: 'value' },
    series: [
      {
        name: '计划巡查',
        type: 'bar',
        barWidth: 12,
        itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
        data: [24, 24, 24, 24, 24, 20, 20]
      },
      {
        name: '已完成',
        type: 'bar',
        barWidth: 12,
        itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] },
        data: [23, 22, 24, 23, 21, 19, 18]
      }
    ]
  })
  charts.push(chart)
}

onMounted(async () => {
  await nextTick()
  initPrisonerChart()
  initCellChart()
  initIncidentChart()
  initPatrolChart()
})

onUnmounted(() => {
  charts.forEach((c) => c.dispose())
})

function handleResize() {
  charts.forEach((c) => c.resize())
}

window.addEventListener('resize', handleResize)

import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <div class="dashboard">
    <div class="stats-row">
      <div
        v-for="card in statsCards"
        :key="card.label"
        class="stat-card"
      >
        <div class="stat-content">
          <div class="stat-info">
            <span class="stat-label">{{ card.label }}</span>
            <span class="stat-value" :style="{ color: card.color }">{{ card.value }}</span>
            <span class="stat-unit">{{ card.unit }}</span>
          </div>
          <div class="stat-icon" :style="{ background: card.color }">
            <el-icon :size="28" color="#fff">
              <component :is="card.icon" />
            </el-icon>
          </div>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <h3 class="chart-title">在押人员趋势</h3>
        <div ref="prisonerChart" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <h3 class="chart-title">监舍使用分布</h3>
        <div ref="cellChart" class="chart-box"></div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <h3 class="chart-title">本月事件分类统计</h3>
        <div ref="incidentChart" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <h3 class="chart-title">本周巡查完成情况</h3>
        <div ref="patrolChart" class="chart-box"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 4px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.stat-unit {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.85;
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
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-box {
  width: 100%;
  height: 320px;
}

@media (max-width: 1200px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>