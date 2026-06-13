<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { get } from '@/utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()

interface VisitorCalendarItem {
  id: number
  visitorName: string
  relation: string
  visitType: string
  prisonerId: number
  prisonerName: string
  prisonerNumber: string
  visitDate: string
  visitTimeSlot: string
  status: string
  visitorCount: number
  purpose: string
}

const statusMap: Record<string, { label: string; type: string; color: string; bgColor: string }> = {
  PENDING: { label: '待审核', type: 'warning', color: '#e6a23c', bgColor: '#fdf6ec' },
  APPROVED: { label: '已通过', type: 'success', color: '#67c23a', bgColor: '#f0f9eb' },
  REJECTED: { label: '已驳回', type: 'danger', color: '#f56c6c', bgColor: '#fef0f0' },
  IN_PROGRESS: { label: '会见中', type: 'primary', color: '#409eff', bgColor: '#ecf5ff' },
  COMPLETED: { label: '已完成', type: 'info', color: '#909399', bgColor: '#f4f4f5' },
  CANCELLED: { label: '已取消', type: 'info', color: '#909399', bgColor: '#f4f4f5' }
}

const visitTypeMap: Record<string, { label: string; tag: string }> = {
  FAMILY: { label: '家属', tag: 'success' },
  LAWYER: { label: '律师', tag: 'primary' },
  OTHER: { label: '其他', tag: 'info' }
}

const relationMap: Record<string, string> = {
  PARENT: '父母',
  SPOUSE: '配偶',
  SIBLING: '兄弟姐妹',
  CHILD: '子女',
  FRIEND: '朋友',
  LAWYER: '律师',
  OTHER: '其他'
}

const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const viewMode = ref<'month' | 'week'>('month')
const currentDate = ref(new Date())
const calendarData = ref<VisitorCalendarItem[]>([])
const loading = ref(false)

const filterForm = ref({
  status: '',
  visitType: ''
})

const detailVisible = ref(false)
const selectedItem = ref<VisitorCalendarItem | null>(null)

const currentYear = computed(() => currentDate.value.getFullYear())
const currentMonth = computed(() => currentDate.value.getMonth() + 1)

function formatDate(date: Date): string {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function getMonthStartEnd(year: number, month: number) {
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month, 0)
  return {
    start: new Date(firstDay.getFullYear(), firstDay.getMonth(), firstDay.getDate() - firstDay.getDay()),
    end: new Date(lastDay.getFullYear(), lastDay.getMonth(), lastDay.getDate() + (6 - lastDay.getDay()))
  }
}

function getWeekStartEnd(date: Date) {
  const start = new Date(date)
  start.setDate(date.getDate() - date.getDay())
  const end = new Date(start)
  end.setDate(start.getDate() + 6)
  return { start, end }
}

const calendarDays = computed(() => {
  const days: {
    date: Date
    dateStr: string
    isCurrentMonth: boolean
    isToday: boolean
    items: VisitorCalendarItem[]
  }[] = []

  let start: Date, end: Date
  if (viewMode.value === 'month') {
    const range = getMonthStartEnd(currentYear.value, currentMonth.value)
    start = range.start
    end = range.end
  } else {
    const range = getWeekStartEnd(currentDate.value)
    start = range.start
    end = range.end
  }

  const today = formatDate(new Date())

  let cur = new Date(start)
  while (cur <= end) {
    const dateStr = formatDate(cur)
    const items = calendarData.value.filter(item => item.visitDate === dateStr)
    days.push({
      date: new Date(cur),
      dateStr,
      isCurrentMonth: cur.getMonth() + 1 === currentMonth.value,
      isToday: dateStr === today,
      items
    })
    cur.setDate(cur.getDate() + 1)
  }
  return days
})

const monthStats = computed(() => {
  const stats = {
    total: calendarData.value.length,
    pending: 0,
    approved: 0,
    inProgress: 0,
    completed: 0,
    rejected: 0
  }
  calendarData.value.forEach(item => {
    if (item.status === 'PENDING') stats.pending++
    else if (item.status === 'APPROVED') stats.approved++
    else if (item.status === 'IN_PROGRESS') stats.inProgress++
    else if (item.status === 'COMPLETED') stats.completed++
    else if (item.status === 'REJECTED') stats.rejected++
  })
  return stats
})

async function fetchCalendarData() {
  loading.value = true
  try {
    let startDate: string, endDate: string
    if (viewMode.value === 'month') {
      const range = getMonthStartEnd(currentYear.value, currentMonth.value)
      startDate = formatDate(range.start)
      endDate = formatDate(range.end)
    } else {
      const range = getWeekStartEnd(currentDate.value)
      startDate = formatDate(range.start)
      endDate = formatDate(range.end)
    }
    const res = await get('/visitors/calendar', {
      startDate,
      endDate,
      status: filterForm.value.status || undefined,
      visitType: filterForm.value.visitType || undefined
    })
    if (res.data.code === 200) {
      calendarData.value = res.data.data || []
    }
  } catch (e) {
    ElMessage.error('获取日历数据失败')
  } finally {
    loading.value = false
  }
}

function goPrev() {
  if (viewMode.value === 'month') {
    currentDate.value = new Date(currentYear.value, currentMonth.value - 2, 1)
  } else {
    currentDate.value = new Date(currentDate.value.getTime() - 7 * 24 * 60 * 60 * 1000)
  }
}

function goNext() {
  if (viewMode.value === 'month') {
    currentDate.value = new Date(currentYear.value, currentMonth.value, 1)
  } else {
    currentDate.value = new Date(currentDate.value.getTime() + 7 * 24 * 60 * 60 * 1000)
  }
}

function goToday() {
  currentDate.value = new Date()
}

function selectDay(date: Date) {
  currentDate.value = date
  viewMode.value = 'week'
}

function showDetail(item: VisitorCalendarItem) {
  selectedItem.value = item
  detailVisible.value = true
}

function getStatusInfo(status: string) {
  return statusMap[status] || { label: status, type: 'info', color: '#909399', bgColor: '#f4f4f5' }
}

function getVisitTypeInfo(type: string) {
  return visitTypeMap[type] || { label: '其他', tag: 'info' }
}

function getRelationLabel(relation: string) {
  return relationMap[relation] || relation
}

function groupItemsByTimeSlot(items: VisitorCalendarItem[]) {
  const am = items.filter(i => i.visitTimeSlot === 'AM')
  const pm = items.filter(i => i.visitTimeSlot === 'PM')
  return { am, pm }
}

function goToList() {
  router.push('/visitors')
}

watch([viewMode, currentDate, filterForm], fetchCalendarData, { deep: true })

onMounted(() => {
  fetchCalendarData()
})
</script>

<template>
  <div class="calendar-page" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>会见日历</h2>
        <p class="subtitle">按日期和时段查看会见安排，排班视角一目了然</p>
      </div>
      <div class="header-actions">
        <el-button @click="goToList">
          <el-icon><List /></el-icon>
          列表视图
        </el-button>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-chip total">
        <span class="chip-label">本月总计</span>
        <span class="chip-value">{{ monthStats.total }}</span>
      </div>
      <div class="stat-chip pending">
        <span class="chip-label">待审核</span>
        <span class="chip-value">{{ monthStats.pending }}</span>
      </div>
      <div class="stat-chip approved">
        <span class="chip-label">已通过</span>
        <span class="chip-value">{{ monthStats.approved }}</span>
      </div>
      <div class="stat-chip in-progress">
        <span class="chip-label">会见中</span>
        <span class="chip-value">{{ monthStats.inProgress }}</span>
      </div>
      <div class="stat-chip completed">
        <span class="chip-label">已完成</span>
        <span class="chip-value">{{ monthStats.completed }}</span>
      </div>
      <div class="stat-chip rejected">
        <span class="chip-label">已驳回</span>
        <span class="chip-value">{{ monthStats.rejected }}</span>
      </div>
    </div>

    <div class="filter-bar">
      <div class="filter-left">
        <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="会见中" value="IN_PROGRESS" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-select v-model="filterForm.visitType" placeholder="全部类型" clearable style="width: 140px">
          <el-option label="家属会见" value="FAMILY" />
          <el-option label="律师会见" value="LAWYER" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </div>
      <div class="calendar-nav">
        <el-radio-group v-model="viewMode" size="default">
          <el-radio-button value="month">月视图</el-radio-button>
          <el-radio-button value="week">周视图</el-radio-button>
        </el-radio-group>
        <el-button @click="goToday" style="margin-left: 8px">今天</el-button>
        <el-button-group>
          <el-button @click="goPrev">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <el-button @click="goNext">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </el-button-group>
        <span class="nav-title">{{ currentYear }}年{{ currentMonth }}月</span>
      </div>
    </div>

    <div class="calendar-container">
      <div class="weekday-header">
        <div v-for="day in weekDays" :key="day" class="weekday-cell" :class="{ weekend: day === '日' || day === '六' }">
          {{ day }}
        </div>
      </div>

      <div class="calendar-grid" :class="{ 'week-view': viewMode === 'week' }">
        <div
          v-for="(cell, idx) in calendarDays"
          :key="idx"
          class="calendar-cell"
          :class="{
            'not-current': !cell.isCurrentMonth && viewMode === 'month',
            'today': cell.isToday,
            weekend: cell.date.getDay() === 0 || cell.date.getDay() === 6
          }"
          @dblclick="selectDay(cell.date)"
        >
          <div class="cell-header">
            <span class="cell-date" :class="{ highlight: cell.isToday }">
              {{ cell.date.getDate() }}
            </span>
            <span v-if="cell.items.length" class="cell-count">
              {{ cell.items.length }}场
            </span>
          </div>

          <div v-if="cell.items.length" class="cell-content">
            <div v-if="groupItemsByTimeSlot(cell.items).am.length" class="time-slot-group">
              <div class="time-slot-label am">
                <el-icon><Sunny /></el-icon>
                上午 {{ groupItemsByTimeSlot(cell.items).am.length }}场
              </div>
              <div class="slot-items">
                <div
                  v-for="item in groupItemsByTimeSlot(cell.items).am.slice(0, 3)"
                  :key="item.id"
                  class="calendar-event"
                  :style="{
                    borderLeftColor: getStatusInfo(item.status).color,
                    background: getStatusInfo(item.status).bgColor
                  }"
                  @click.stop="showDetail(item)"
                >
                  <div class="event-top">
                    <el-tag size="small" :type="getVisitTypeInfo(item.visitType).tag" effect="plain">
                      {{ getVisitTypeInfo(item.visitType).label }}
                    </el-tag>
                    <span class="event-status" :style="{ color: getStatusInfo(item.status).color }">
                      {{ getStatusInfo(item.status).label }}
                    </span>
                  </div>
                  <div class="event-main">
                    <span class="prisoner-name">{{ item.prisonerName || '服刑人员#' + item.prisonerId }}</span>
                    <span class="divider">←</span>
                    <span class="visitor-name">{{ item.visitorName }}</span>
                  </div>
                  <div class="event-sub">
                    <span v-if="item.relation">{{ getRelationLabel(item.relation) }}</span>
                    <span v-if="item.visitorCount" class="visitor-count">{{ item.visitorCount }}人</span>
                  </div>
                </div>
                <div
                  v-if="groupItemsByTimeSlot(cell.items).am.length > 3"
                  class="more-items"
                  @click.stop="selectDay(cell.date)"
                >
                  还有 {{ groupItemsByTimeSlot(cell.items).am.length - 3 }} 场...
                </div>
              </div>
            </div>

            <div v-if="groupItemsByTimeSlot(cell.items).pm.length" class="time-slot-group">
              <div class="time-slot-label pm">
                <el-icon><Moon /></el-icon>
                下午 {{ groupItemsByTimeSlot(cell.items).pm.length }}场
              </div>
              <div class="slot-items">
                <div
                  v-for="item in groupItemsByTimeSlot(cell.items).pm.slice(0, 3)"
                  :key="item.id"
                  class="calendar-event"
                  :style="{
                    borderLeftColor: getStatusInfo(item.status).color,
                    background: getStatusInfo(item.status).bgColor
                  }"
                  @click.stop="showDetail(item)"
                >
                  <div class="event-top">
                    <el-tag size="small" :type="getVisitTypeInfo(item.visitType).tag" effect="plain">
                      {{ getVisitTypeInfo(item.visitType).label }}
                    </el-tag>
                    <span class="event-status" :style="{ color: getStatusInfo(item.status).color }">
                      {{ getStatusInfo(item.status).label }}
                    </span>
                  </div>
                  <div class="event-main">
                    <span class="prisoner-name">{{ item.prisonerName || '服刑人员#' + item.prisonerId }}</span>
                    <span class="divider">←</span>
                    <span class="visitor-name">{{ item.visitorName }}</span>
                  </div>
                  <div class="event-sub">
                    <span v-if="item.relation">{{ getRelationLabel(item.relation) }}</span>
                    <span v-if="item.visitorCount" class="visitor-count">{{ item.visitorCount }}人</span>
                  </div>
                </div>
                <div
                  v-if="groupItemsByTimeSlot(cell.items).pm.length > 3"
                  class="more-items"
                  @click.stop="selectDay(cell.date)"
                >
                  还有 {{ groupItemsByTimeSlot(cell.items).pm.length - 3 }} 场...
                </div>
              </div>
            </div>
          </div>

          <div v-else class="cell-empty">
            <span>暂无安排</span>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="会见详情" width="520px" destroy-on-close>
      <div v-if="selectedItem" class="detail-content">
        <div class="detail-header">
          <el-tag :type="getStatusInfo(selectedItem.status).type" size="large" effect="light">
            {{ getStatusInfo(selectedItem.status).label }}
          </el-tag>
          <el-tag :type="getVisitTypeInfo(selectedItem.visitType).tag" size="large">
            {{ getVisitTypeInfo(selectedItem.visitType).label }}会见
          </el-tag>
        </div>

        <div class="detail-section">
          <h4 class="section-title">
            <el-icon><Calendar /></el-icon>
            会见安排
          </h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">会见日期</span>
              <span class="value">{{ selectedItem.visitDate }}</span>
            </div>
            <div class="detail-item">
              <span class="label">会见时段</span>
              <span class="value">{{ selectedItem.visitTimeSlot === 'AM' ? '上午' : '下午' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">来访人数</span>
              <span class="value">{{ selectedItem.visitorCount || 1 }} 人</span>
            </div>
            <div class="detail-item">
              <span class="label">会见目的</span>
              <span class="value">{{ selectedItem.purpose || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">
            <el-icon><UserFilled /></el-icon>
            被探视人信息
          </h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">姓名</span>
              <span class="value">{{ selectedItem.prisonerName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">编号</span>
              <span class="value">{{ selectedItem.prisonerNumber || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">
            <el-icon><User /></el-icon>
            访客信息
          </h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">姓名</span>
              <span class="value">{{ selectedItem.visitorName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">与服刑人员关系</span>
              <span class="value">{{ getRelationLabel(selectedItem.relation) }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="detailVisible = false; goToList()">查看完整列表</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.calendar-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  min-height: calc(100vh - 140px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 4px 0;
}

.subtitle {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.stats-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.stat-chip {
  display: flex;
  flex-direction: column;
  padding: 10px 18px;
  border-radius: 8px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  min-width: 96px;
}

.stat-chip .chip-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-chip .chip-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-chip.total {
  background: linear-gradient(135deg, #e6f2ff 0%, #f0f7ff 100%);
  border-color: #b3d8ff;
}
.stat-chip.total .chip-value {
  color: #409eff;
}
.stat-chip.pending {
  background: linear-gradient(135deg, #fef6e4 0%, #fff7ed 100%);
  border-color: #faecd8;
}
.stat-chip.pending .chip-value {
  color: #e6a23c;
}
.stat-chip.approved {
  background: linear-gradient(135deg, #e8f7e8 0%, #f0fff0 100%);
  border-color: #c2e7b0;
}
.stat-chip.approved .chip-value {
  color: #67c23a;
}
.stat-chip.in-progress {
  background: linear-gradient(135deg, #e6f2ff 0%, #f0f7ff 100%);
  border-color: #b3d8ff;
}
.stat-chip.in-progress .chip-value {
  color: #409eff;
}
.stat-chip.completed {
  background: linear-gradient(135deg, #f0f2f5 0%, #f5f7fa 100%);
  border-color: #e4e7ed;
}
.stat-chip.completed .chip-value {
  color: #909399;
}
.stat-chip.rejected {
  background: linear-gradient(135deg, #ffecec 0%, #fff0f0 100%);
  border-color: #fbc4c4;
}
.stat-chip.rejected .chip-value {
  color: #f56c6c;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.calendar-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-left: 8px;
  min-width: 120px;
}

.calendar-container {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.weekday-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.weekday-cell {
  padding: 12px 8px;
  text-align: center;
  font-weight: 600;
  font-size: 13px;
  color: #606266;
}

.weekday-cell.weekend {
  color: #f56c6c;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.calendar-grid.week-view .calendar-cell {
  min-height: 480px;
}

.calendar-cell {
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  min-height: 160px;
  display: flex;
  flex-direction: column;
  padding: 8px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}

.calendar-cell:nth-child(7n) {
  border-right: none;
}

.calendar-cell:hover {
  background: #fafcff;
}

.calendar-cell.not-current {
  background: #fafafa;
}

.calendar-cell.not-current .cell-date {
  color: #c0c4cc;
}

.calendar-cell.today {
  background: #f0f9ff;
}

.calendar-cell.weekend {
  background: #fafbfc;
}

.calendar-cell.weekend.today {
  background: #f0f9ff;
}

.cell-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.cell-date {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.cell-date.highlight {
  background: #409eff;
  color: #fff;
}

.cell-count {
  font-size: 11px;
  color: #909399;
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 10px;
}

.cell-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  overflow-y: auto;
}

.cell-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 12px;
}

.time-slot-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-slot-label {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 3px;
}

.time-slot-label.am {
  background: #fdf6ec;
  color: #e6a23c;
}

.time-slot-label.pm {
  background: #ecf5ff;
  color: #409eff;
}

.slot-items {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.calendar-event {
  border-left: 3px solid;
  border-radius: 4px;
  padding: 5px 6px;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  background: #f5f7fa;
}

.calendar-event:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.event-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3px;
  gap: 4px;
}

.event-status {
  font-size: 11px;
  font-weight: 600;
}

.event-main {
  font-size: 12px;
  color: #303133;
  line-height: 1.4;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.prisoner-name {
  font-weight: 600;
  color: #1f2d3d;
}

.divider {
  color: #c0c4cc;
  font-size: 11px;
}

.visitor-name {
  color: #606266;
}

.event-sub {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
  display: flex;
  gap: 8px;
}

.more-items {
  font-size: 11px;
  color: #409eff;
  text-align: center;
  padding: 2px 0;
  cursor: pointer;
  border-radius: 4px;
}
.more-items:hover {
  background: rgba(64, 158, 255, 0.1);
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-header {
  display: flex;
  gap: 8px;
}

.detail-section {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item .label {
  font-size: 12px;
  color: #909399;
}

.detail-item .value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.visitor-count {
  background: #e8f4fd;
  color: #409eff;
  padding: 0 4px;
  border-radius: 3px;
  font-size: 10px;
}
</style>
