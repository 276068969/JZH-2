<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { get } from '@/utils/request'
import { ElMessage } from 'element-plus'
import {
  UserFilled, Calendar, FirstAidKit, WarningFilled, OfficeBuilding,
  ChatDotRound, Stethoscope, Clock, CircleCheck, StarFilled,
  Management, Date, Place, User, Monitor
} from '@element-plus/icons-vue'

interface Prisoner {
  id: number
  prisonerNumber: string
  name: string
  gender: string
  areaName?: string
  cellNumber?: string
  dangerLevel?: string
}

interface TimelineNode {
  recordId: number
  eventDate: string
  nodeType: string
  medicalType: string
  medicalTypeLabel: string
  diagnosis: string
  treatment: string
  hospital: string
  doctorName: string
  result: string
  resultLabel: string
  resultTagType: string
  medicine: string
  followUpDate: string
  followUpStatus: string
  followUpStatusLabel: string
  followUpTagType: string
  actualFollowUpDate: string
  followUpResult: string
  followUpRemark: string
  color: string
  icon: string
}

interface TimelineData {
  prisonerId: number
  prisonerNumber: string
  prisonerName: string
  gender: string
  birthDate: string
  idCard: string
  nativePlace: string
  areaId: number
  areaName: string
  cellId: number
  cellNumber: string
  dangerLevel: string
  healthStatus: string
  prisonerStatus: string
  totalRecords: number
  treatingCount: number
  recoveredCount: number
  followUpPendingCount: number
  followUpMissedCount: number
  nodes: TimelineNode[]
}

const prisoners = ref<Prisoner[]>([])
const selectedPrisonerId = ref<number | null>(null)
const timelineData = ref<TimelineData | null>(null)
const loading = ref(false)
const prisonerLoading = ref(false)
const filterType = ref<string>('')
const filterResult = ref<string>('')

const medicalTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '体检', value: 'PHYSICAL' },
  { label: '门诊', value: 'OUTPATIENT' },
  { label: '急诊', value: 'EMERGENCY' },
  { label: '住院', value: 'HOSPITALIZATION' },
  { label: '心理咨询', value: 'PSYCHOLOGICAL' },
  { label: '复诊', value: 'FOLLOW_UP' }
]

const resultOptions = [
  { label: '全部结果', value: '' },
  { label: '已治愈', value: 'RECOVERED' },
  { label: '治疗中', value: 'TREATING' },
  { label: '已转院', value: 'TRANSFERRED' },
  { label: '已完成复诊', value: 'COMPLETED' }
]

const dangerLevelMap: Record<string, { text: string; type: string }> = {
  LOW: { text: '低危', type: 'success' },
  MEDIUM: { text: '中危', type: 'warning' },
  HIGH: { text: '高危', type: 'danger' },
  EXTREME: { text: '极高危', type: 'danger' }
}

const prisonerStatusMap: Record<string, string> = {
  INCARCERATED: '在押',
  RELEASED: '已释放',
  TRANSFERRED: '已调动',
  MEDICAL_PAROLE: '保外就医'
}

const summaryCards = computed(() => {
  const d = timelineData.value
  if (!d) return []
  return [
    { label: '就诊记录总数', color: '#409eff', icon: 'FirstAidKit', count: d.totalRecords, desc: '累计就诊' },
    { label: '治疗中', color: '#e6a23c', icon: 'Stethoscope', count: d.treatingCount, desc: '需持续跟进' },
    { label: '已治愈', color: '#67c23a', icon: 'CircleCheck', count: d.recoveredCount, desc: '治疗完成' },
    { label: '待复诊', color: '#f56c6c', icon: 'Calendar', count: d.followUpPendingCount, desc: '近期需复诊' },
    { label: '逾期未复诊', color: '#909399', icon: 'WarningFilled', count: d.followUpMissedCount, desc: '需重点关注' }
  ]
})

const filteredNodes = computed(() => {
  const nodes = timelineData.value?.nodes || []
  return nodes.filter(n => {
    if (filterType.value && n.medicalType !== filterType.value) return false
    if (filterResult.value) {
      if (filterResult.value === 'COMPLETED' && n.nodeType !== 'FOLLOW_UP') return false
      if (filterResult.value !== 'COMPLETED' && n.result !== filterResult.value) return false
    }
    return true
  })
})

function getIconComponent(iconName: string) {
  const map: Record<string, any> = {
    FirstAidKit, Stethoscope, WarningFilled, OfficeBuilding,
    ChatDotRound, Calendar, CircleCheck, StarFilled, Clock, Date,
    Management, Place, User, Monitor, UserFilled
  }
  return map[iconName] || FirstAidKit
}

function formatDate(d: string) {
  if (!d) return '-'
  return d
}

async function fetchPrisoners() {
  prisonerLoading.value = true
  try {
    const res = await get<any>('/prisoners?size=999')
    const records = res.data?.data?.records || res.data?.data || []
    prisoners.value = records.map((r: any) => ({
      id: r.id,
      prisonerNumber: r.prisonerNumber,
      name: r.name,
      gender: r.gender,
      areaName: r.areaName,
      cellNumber: r.cellNumber,
      dangerLevel: r.dangerLevel
    }))
    if (prisoners.value.length > 0 && !selectedPrisonerId.value) {
      selectedPrisonerId.value = prisoners.value[0].id
      await fetchTimeline()
    }
  } catch (e) {
    prisoners.value = []
  } finally {
    prisonerLoading.value = false
  }
}

async function fetchTimeline() {
  if (!selectedPrisonerId.value) {
    timelineData.value = null
    return
  }
  loading.value = true
  try {
    const res = await get<any>(`/medical-records/timeline/${selectedPrisonerId.value}`)
    timelineData.value = res.data?.data || null
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '加载时间轴失败')
    timelineData.value = null
  } finally {
    loading.value = false
  }
}

function handlePrisonerChange() {
  fetchTimeline()
}

function handleFilterChange() {
}

function maskIdCard(id: string) {
  if (!id || id.length < 10) return id || '-'
  return id.slice(0, 6) + '********' + id.slice(-4)
}

onMounted(() => {
  fetchPrisoners()
})
</script>

<template>
  <div class="timeline-page">
    <div class="page-header">
      <h2>医疗记录时间轴档案</h2>
      <p class="desc">按时间顺序查看服刑人员的诊断、治疗、结果、用药与复诊脉络</p>
    </div>

    <div class="control-bar">
      <div class="selector-wrap">
        <span class="label">选择服刑人员：</span>
        <el-select
          v-model="selectedPrisonerId"
          placeholder="请选择服刑人员"
          filterable
          style="width: 320px"
          :loading="prisonerLoading"
          @change="handlePrisonerChange"
        >
          <el-option
            v-for="p in prisoners"
            :key="p.id"
            :label="`${p.name} (${p.prisonerNumber})`"
            :value="p.id"
          >
            <span style="float: left">{{ p.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">{{ p.prisonerNumber }} · {{ p.gender }}</span>
          </el-option>
        </el-select>
      </div>

      <div class="filters-wrap" v-if="timelineData">
        <el-select v-model="filterType" placeholder="医疗类型" style="width: 140px" clearable @change="handleFilterChange">
          <el-option v-for="opt in medicalTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-select v-model="filterResult" placeholder="治疗结果" style="width: 140px" clearable @change="handleFilterChange">
          <el-option v-for="opt in resultOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </div>
    </div>

    <el-skeleton :loading="loading" animated :rows="8" v-if="loading || !timelineData">
    </el-skeleton>

    <template v-else>
      <div class="prisoner-card">
        <div class="avatar-wrap">
          <el-avatar :size="64" style="background: #409eff">
            <el-icon :size="32"><UserFilled /></el-icon>
          </el-avatar>
        </div>
        <div class="info-wrap">
          <div class="name-row">
            <span class="name">{{ timelineData.prisonerName }}</span>
            <el-tag size="small" type="info">{{ timelineData.gender }}</el-tag>
            <el-tag
              size="small"
              :type="dangerLevelMap[timelineData.dangerLevel]?.type || 'info'"
              effect="light"
              v-if="timelineData.dangerLevel"
            >{{ dangerLevelMap[timelineData.dangerLevel]?.text || timelineData.dangerLevel }}</el-tag>
            <el-tag
              size="small"
              :type="timelineData.prisonerStatus === 'INCARCERATED' ? 'success' : 'warning'"
              effect="plain"
            >{{ prisonerStatusMap[timelineData.prisonerStatus] || timelineData.prisonerStatus }}</el-tag>
          </div>
          <div class="info-row">
            <div class="info-item"><el-icon><Monitor /></el-icon> 编号：{{ timelineData.prisonerNumber }}</div>
            <div class="info-item" v-if="timelineData.birthDate"><el-icon><Calendar /></el-icon> 出生日期：{{ formatDate(timelineData.birthDate) }}</div>
            <div class="info-item"><el-icon><User /></el-icon> 身份证：{{ maskIdCard(timelineData.idCard) }}</div>
            <div class="info-item" v-if="timelineData.nativePlace"><el-icon><Place /></el-icon> 籍贯：{{ timelineData.nativePlace }}</div>
          </div>
          <div class="info-row">
            <div class="info-item" v-if="timelineData.areaName"><el-icon><OfficeBuilding /></el-icon> 监区：{{ timelineData.areaName }}</div>
            <div class="info-item" v-if="timelineData.cellNumber"><el-icon><Management /></el-icon> 监舍：{{ timelineData.cellNumber }}</div>
            <div class="info-item" v-if="timelineData.healthStatus"><el-icon><Stethoscope /></el-icon> 健康状况：{{ timelineData.healthStatus }}</div>
          </div>
        </div>
      </div>

      <div class="stats-row">
        <div
          v-for="card in summaryCards"
          :key="card.label"
          class="stat-card"
          :style="{ borderLeftColor: card.color }"
        >
          <div class="stat-icon" :style="{ background: card.color + '15', color: card.color }">
            <el-icon :size="22"><component :is="getIconComponent(card.icon)" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-count" :style="{ color: card.color }">{{ card.count }}</div>
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-desc">{{ card.desc }}</div>
          </div>
        </div>
      </div>

      <div class="timeline-wrap">
        <div class="timeline-header">
          <h3>诊疗时间轴</h3>
          <span class="tip">共 {{ filteredNodes.length }} 条记录</span>
        </div>

        <el-empty v-if="filteredNodes.length === 0" description="暂无符合条件的诊疗记录" />

        <el-timeline v-else>
          <el-timeline-item
            v-for="(node, idx) in filteredNodes"
            :key="`${node.recordId}-${node.nodeType}-${idx}`"
            :timestamp="formatDate(node.eventDate)"
            placement="top"
            :color="node.color"
            :hollow="node.nodeType === 'FOLLOW_UP'"
          >
            <div class="timeline-card" :class="{ 'follow-up': node.nodeType === 'FOLLOW_UP' }">
              <div class="card-header">
                <div class="type-row">
                  <el-tag
                    :color="node.color + '15'"
                    effect="plain"
                    size="small"
                    round
                    style="color: #333; border-color: transparent; font-weight: 600"
                  >
                    <el-icon style="vertical-align: -2px; margin-right: 4px; color: #333">
                      <component :is="getIconComponent(node.icon)" />
                    </el-icon>
                    {{ node.medicalTypeLabel }}
                  </el-tag>
                  <el-tag
                    v-if="node.resultLabel"
                    :type="node.resultTagType as any"
                    size="small"
                    effect="light"
                  >{{ node.resultLabel }}</el-tag>
                </div>
                <div class="date-row">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(node.eventDate) }}</span>
                </div>
              </div>

              <div class="card-body">
                <div class="row-item" v-if="node.diagnosis">
                  <span class="label">诊断</span>
                  <span class="value diagnosis">{{ node.diagnosis }}</span>
                </div>
                <div class="row-item" v-if="node.treatment">
                  <span class="label">治疗方案</span>
                  <span class="value">{{ node.treatment }}</span>
                </div>
                <div class="row-item" v-if="node.medicine">
                  <span class="label">用药</span>
                  <span class="value medicine">{{ node.medicine }}</span>
                </div>

                <div class="meta-row" v-if="node.hospital || node.doctorName">
                  <span v-if="node.hospital" class="meta-item">
                    <el-icon><Place /></el-icon>{{ node.hospital }}
                  </span>
                  <span v-if="node.doctorName" class="meta-item">
                    <el-icon><User /></el-icon>{{ node.doctorName }}
                  </span>
                </div>
              </div>

              <div class="card-footer" v-if="node.followUpDate || node.followUpRemark">
                <template v-if="node.nodeType === 'FOLLOW_UP'">
                  <div class="follow-up-box completed" v-if="node.followUpRemark">
                    <div class="fu-label">复诊备注</div>
                    <div class="fu-content">{{ node.followUpRemark }}</div>
                  </div>
                  <div class="follow-up-box completed">
                    <div class="fu-label">复诊信息</div>
                    <div class="fu-content">
                      计划复诊：{{ formatDate(node.followUpDate) }}
                      <span class="sep">·</span>
                      实际复诊：{{ formatDate(node.actualFollowUpDate) }}
                    </div>
                  </div>
                </template>
                <template v-else>
                  <div
                    class="follow-up-box"
                    :class="{
                      pending: node.followUpStatus === 'PENDING',
                      overdue: node.followUpStatus === 'OVERDUE' || node.followUpStatus === 'MISSED',
                      completed: node.followUpStatus === 'COMPLETED'
                    }"
                  >
                    <div class="fu-row">
                      <el-icon><Calendar /></el-icon>
                      <span class="fu-label">复诊安排</span>
                      <span class="fu-date">{{ formatDate(node.followUpDate) }}</span>
                      <el-tag
                        size="small"
                        :type="node.followUpTagType as any"
                        effect="light"
                      >{{ node.followUpStatusLabel }}</el-tag>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </template>
  </div>
</template>

<style scoped>
.timeline-page {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 16px;
}
.page-header h2 {
  font-size: 20px;
  color: #303133;
  margin: 0 0 6px 0;
}
.page-header .desc {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.control-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
  padding: 14px 18px;
  background: #f5f7fa;
  border-radius: 8px;
}
.selector-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}
.selector-wrap .label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}
.filters-wrap {
  display: flex;
  gap: 12px;
}

.prisoner-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f0f9ff 0%, #ecfdf5 100%);
  border-radius: 10px;
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}
.avatar-wrap {
  flex-shrink: 0;
}
.info-wrap {
  flex: 1;
  min-width: 0;
}
.name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
.info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-bottom: 6px;
}
.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}
.info-item .el-icon {
  color: #909399;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
  margin-bottom: 24px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-left: 4px solid #409eff;
  border-radius: 8px;
  transition: box-shadow 0.2s;
}
.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info {
  min-width: 0;
  flex: 1;
}
.stat-count {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 2px;
}
.stat-label {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}
.stat-desc {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

.timeline-wrap {
  background: #fafbfc;
  border-radius: 10px;
  padding: 20px 24px 24px 24px;
}
.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}
.timeline-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}
.timeline-header .tip {
  font-size: 12px;
  color: #909399;
}

.timeline-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 18px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  margin-bottom: 4px;
  transition: box-shadow 0.2s;
}
.timeline-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}
.timeline-card.follow-up {
  background: linear-gradient(135deg, #faf5ff 0%, #fff 100%);
  border-color: #e9d5ff;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}
.type-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.date-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.card-body {
  margin-bottom: 12px;
}
.row-item {
  display: flex;
  gap: 12px;
  padding: 6px 0;
  border-bottom: 1px dashed #f0f0f0;
}
.row-item:last-of-type {
  border-bottom: none;
}
.row-item .label {
  flex-shrink: 0;
  width: 70px;
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}
.row-item .value {
  flex: 1;
  font-size: 13px;
  color: #303133;
  line-height: 1.6;
}
.row-item .value.diagnosis {
  color: #f56c6c;
  font-weight: 500;
}
.row-item .value.medicine {
  color: #409eff;
}

.meta-row {
  display: flex;
  gap: 18px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #f0f0f0;
  flex-wrap: wrap;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
  background: #f5f7fa;
  padding: 4px 10px;
  border-radius: 14px;
}
.meta-item .el-icon {
  color: #909399;
}

.card-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
.follow-up-box {
  border-radius: 8px;
  padding: 10px 14px;
  background: #fff7e6;
  border: 1px solid #ffe7ba;
}
.follow-up-box.pending {
  background: #ecf5ff;
  border-color: #d9ecff;
}
.follow-up-box.overdue {
  background: #fef0f0;
  border-color: #fbc4c4;
}
.follow-up-box.completed {
  background: #f0f9eb;
  border-color: #e1f3d8;
}
.fu-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #606266;
}
.fu-row .el-icon {
  color: #909399;
}
.fu-label {
  font-weight: 500;
  color: #303133;
}
.fu-date {
  font-weight: 600;
  color: #303133;
}
.fu-content {
  font-size: 13px;
  color: #606266;
  margin-top: 4px;
}
.fu-content .sep {
  margin: 0 6px;
  color: #c0c4cc;
}
</style>
