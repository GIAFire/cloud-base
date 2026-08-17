<script lang="ts" setup>
import { computed, onMounted, reactive, ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  ArrowRight,
  Briefcase,
  Clock,
  DataLine,
  Lightning,
  MagicStick,
  Monitor,
  More,
  Odometer,
  Search,
  TrendCharts,
  VideoPlay
} from '@element-plus/icons-vue'
import { getInteractionHistory, getRunOverview } from '@/api/agent/overview'
import { getUser } from '@/utils/auth'
import { useI18n } from 'vue-i18n'
import { ContentWrap } from '@/components/ContentWrap'

defineOptions({ name: 'AiOverview' })

const { t } = useI18n()
const router = useRouter()

// 类型定义
interface Metrics {
  totalAgents?: number
  newToday?: number
  todayRuns?: number
  runChangePercent?: number
  successRate?: number
  successRateChange?: number
  averageDurationMs?: number
  averageDurationChangeMs?: number
}

interface TrendPoint {
  date: string
  runCount?: number
  successCount?: number
}

interface TrendSummary {
  totalRuns?: number
  totalRunsChangePercent?: number
  successRuns?: number
  successRunsChangePercent?: number
  successRate?: number
  successRateChange?: number
  averageDurationMs?: number
  averageDurationChangeMs?: number
}

interface Trend {
  points: TrendPoint[]
  summary: TrendSummary
  startDate?: string
}

interface Interaction {
  runId: string
  agentId?: number
  agentName?: string
  agentCode?: string
  agentAvailable?: boolean
  sessionId?: string
  status?: string
  userMessage?: string
  assistantMessage?: string
  startedAt?: string
  modelId?: number
}

interface QuickAgent {
  id: number
  agentName: string
  agentCode?: string
  description?: string
  runCount30Days?: number
  modelId?: number
}

interface OverviewData {
  metrics: Metrics
  trend: Trend
  recentInteractions: Interaction[]
  quickAgents: QuickAgent[]
}

// 状态管理
const loading = ref(false)
const loadError = ref('')
const rangeMode = ref('近7天')
const customRange = ref<string[]>([])
let requestSequence = 0
let historyRequestSequence = 0

const emptyOverview = (): OverviewData => ({
  metrics: {},
  trend: { points: [], summary: {} },
  recentInteractions: [],
  quickAgents: []
})

const overview = ref<OverviewData>(emptyOverview())
const rangeOptions = ['近7天', '近30天', '自定义']
const quickIcons = [DataLine, Monitor, Briefcase, Odometer, MagicStick]
const quickTones = ['blue', 'indigo', 'violet', 'orange', 'teal']
const currentUser = getUser()
const userName = currentUser?.userName || '用户'

// 图表引用
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

// 工具函数
const pad = (value: number) => String(value).padStart(2, '0')

const toDateText = (date: Date) =>
  `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`

const relativeDate = (days: number) => {
  const date = new Date()
  date.setHours(0, 0, 0, 0)
  date.setDate(date.getDate() - days)
  return toDateText(date)
}

const todayText = relativeDate(0)
const weekdayText = new Intl.DateTimeFormat('zh-CN', { weekday: 'long' }).format(new Date())
const calendarDateText = new Intl.DateTimeFormat('zh-CN', {
  month: 'long',
  day: 'numeric'
}).format(new Date())
const heroDate = `${weekdayText} · ${calendarDateText}`

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const formatNumber = (value?: number) => Number(value || 0).toLocaleString('zh-CN')

const formatDuration = (value?: number) => {
  if (value == null) return '--'
  const milliseconds = Number(value)
  if (milliseconds >= 60000) return `${(milliseconds / 60000).toFixed(1)}min`
  if (milliseconds >= 1000) return `${(milliseconds / 1000).toFixed(2)}s`
  return `${Math.round(milliseconds)}ms`
}

const formatPercentChange = (value?: number, prefix = '较昨日同期') => {
  if (value == null) return `${prefix}暂无数据`
  const number = Number(value)
  const arrow = number > 0 ? '↑' : number < 0 ? '↓' : '→'
  return `${prefix} ${arrow} ${Math.abs(number).toFixed(1)}%`
}

const formatPointChange = (value?: number, prefix = '较昨日同期') => {
  if (value == null) return `${prefix}暂无数据`
  const number = Number(value)
  const arrow = number > 0 ? '↑' : number < 0 ? '↓' : '→'
  return `${prefix} ${arrow} ${Math.abs(number).toFixed(1)} 个百分点`
}

const formatDurationChange = (value?: number, prefix = '较昨日同期') => {
  if (value == null) return `${prefix}暂无数据`
  const number = Number(value)
  const arrow = number > 0 ? '↑' : number < 0 ? '↓' : '→'
  return `${prefix} ${arrow} ${formatDuration(Math.abs(number))}`
}

// 统计卡片数据
const statCards = computed(() => {
  const metrics = overview.value.metrics || {}
  return [
    {
      label: '智能体总数',
      value: formatNumber(metrics.totalAgents),
      delta: `今日新增 ${formatNumber(metrics.newToday)}`,
      icon: Briefcase,
      tone: 'blue'
    },
    {
      label: '今日运行',
      value: formatNumber(metrics.todayRuns),
      delta: formatPercentChange(metrics.runChangePercent),
      icon: TrendCharts,
      tone: 'cyan'
    },
    {
      label: '今日成功率',
      value: metrics.successRate == null ? '--' : `${Number(metrics.successRate).toFixed(1)}%`,
      delta: formatPointChange(metrics.successRateChange),
      icon: Monitor,
      tone: 'indigo'
    },
    {
      label: '今日平均耗时',
      value: formatDuration(metrics.averageDurationMs),
      delta: formatDurationChange(metrics.averageDurationChangeMs),
      icon: Clock,
      tone: 'teal'
    }
  ]
})

const healthText = computed(() => {
  const rate = overview.value.metrics?.successRate
  if (rate == null) return '今日暂无已结束运行。'
  if (Number(rate) >= 95) return '当前系统运行平稳，智能体表现良好。'
  if (Number(rate) >= 80) return '当前系统运行存在波动，请持续关注。'
  return '当前系统运行需要关注，请检查近期失败记录。'
})

const trendPoints = computed(() => overview.value.trend?.points || [])
const trendSummary = computed(() => overview.value.trend?.summary || {})

// 图表配置
const buildChartOption = (points: TrendPoint[]): echarts.EChartsCoreOption => {
  const dates = points.map((p) => p.date)
  const runCounts = points.map((p) => Number(p.runCount || 0))
  const successCounts = points.map((p) => Number(p.successCount || 0))

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['运行次数', '成功次数'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 40,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        formatter: (v: string) => v.slice(5)
      }
    },
    yAxis: {
      type: 'value',
      name: '次数'
    },
    series: [
      {
        name: '运行次数',
        type: 'line',
        smooth: true,
        data: runCounts,
        itemStyle: { color: '#5b8cff' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(91,140,255,0.24)' },
              { offset: 1, color: 'rgba(91,140,255,0.02)' }
            ]
          }
        }
      },
      {
        name: '成功次数',
        type: 'line',
        smooth: true,
        data: successCounts,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
}

const updateChart = () => {
  const points = trendPoints.value
  if (chart && points.length > 0) {
    chart.setOption(buildChartOption(points))
  }
}

// 交互历史处理
const normalizeInteraction = (interaction: Interaction) => {
  const status = String(interaction.status || '').toUpperCase()
  const userMessage = String(interaction.userMessage || '').trim() || '（空消息）'
  const assistantMessage = String(interaction.assistantMessage || '').trim()
  const fallbackReplies: Record<string, string> = {
    FAILED: '回复失败',
    CANCELLED: '回复已取消',
    RUNNING: '正在回复…',
    WAITING_USER_CONFIRM: '等待用户确认',
    WAITING_EXTERNAL_EXECUTION: '等待外部执行'
  }
  const replyTone = status.startsWith('WAITING_') ? 'running' : status.toLowerCase()

  return {
    ...interaction,
    userMessage,
    assistantPreview: assistantMessage || fallbackReplies[status] || '暂无回复',
    replyTone: assistantMessage ? '' : replyTone,
    agentInitial: String(interaction.agentName || '智').slice(0, 1)
  }
}

const recentInteractions = computed(() =>
  (overview.value.recentInteractions || []).map(normalizeInteraction)
)

const interactionHistoryVisible = ref(false)
const interactionHistoryKeyword = ref('')
const interactionHistory = reactive({
  loading: false,
  records: [] as Interaction[],
  total: 0,
  current: 1,
  size: 10
})
const interactionHistoryRows = computed(() =>
  interactionHistory.records.map(normalizeInteraction)
)

const quickAgents = computed(() =>
  (overview.value.quickAgents || []).map((agent, index) => ({
    ...agent,
    icon: quickIcons[index % quickIcons.length],
    tone: quickTones[index % quickTones.length]
  }))
)

const formatInteractionTime = (value?: string) => {
  if (!value) return '--'
  const normalized = String(value).replace(' ', 'T')
  const date = new Date(normalized)
  if (Number.isNaN(date.getTime())) return String(value).replace('T', ' ')
  const sameDay = toDateText(date) === todayText
  return new Intl.DateTimeFormat(
    'zh-CN',
    sameDay
      ? { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }
      : { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false }
  ).format(date)
}

const selectedRange = () => {
  if (rangeMode.value === '近30天') {
    return [relativeDate(29), todayText]
  }
  if (rangeMode.value === '自定义') {
    return customRange.value?.length === 2 ? customRange.value : null
  }
  return [relativeDate(6), todayText]
}

const loadOverview = async () => {
  const range = selectedRange()
  if (!range) return
  const sequence = ++requestSequence
  loading.value = true
  loadError.value = ''
  try {
    const data = await getRunOverview({ startDate: range[0], endDate: range[1] })
    if (sequence === requestSequence) {
      overview.value = data || emptyOverview()
      // 更新图表
      await nextTick()
      updateChart()
    }
  } catch (error: any) {
    if (sequence === requestSequence) {
      loadError.value = error?.message || '总览数据加载失败'
    }
  } finally {
    if (sequence === requestSequence) loading.value = false
  }
}

const handleRangeModeChange = (value: string) => {
  rangeMode.value = value
  if (value === '自定义') {
    if (customRange.value?.length !== 2) {
      customRange.value = [overview.value.trend?.startDate || relativeDate(6), todayText]
    }
    return
  }
  loadOverview()
}

const handleCustomRangeChange = (value: string[]) => {
  customRange.value = value || []
  if (customRange.value.length !== 2) return
  const start = new Date(`${customRange.value[0]}T00:00:00`)
  const end = new Date(`${customRange.value[1]}T00:00:00`)
  const days = Math.round((end.getTime() - start.getTime()) / 86400000) + 1
  if (days > 90) {
    ElMessage.warning('自定义日期范围最多为 90 天')
    return
  }
  loadOverview()
}

const disableFutureDate = (time: Date) => time.getTime() > new Date().setHours(23, 59, 59, 999)

const handleCreateAgent = () => {
  router.push({ path: '/agent/manage', query: { create: '1' } })
}

const openAgentManagement = () => router.push('/agent/manage')

const openRecentInteraction = (interaction: any) => {
  if (!interaction.agentAvailable || !interaction.agentId) {
    ElMessage.info('该智能体已删除，无法打开原会话')
    return
  }
  if (!interaction.sessionId) {
    ElMessage.info('原会话不存在，无法打开')
    return
  }
  router.push({
    name: 'AgentChat',
    params: { agentId: String(interaction.agentId) },
    query: {
      agentName: interaction.agentName,
      agentKey: interaction.agentCode || '',
      sessionId: String(interaction.sessionId)
    }
  })
}

const loadInteractionHistory = async () => {
  const sequence = ++historyRequestSequence
  interactionHistory.loading = true
  try {
    const page = await getInteractionHistory({
      current: interactionHistory.current,
      size: interactionHistory.size,
      keyword: interactionHistoryKeyword.value.trim() || undefined
    })
    if (sequence !== historyRequestSequence) return
    interactionHistory.records = Array.isArray(page?.records) ? page.records : []
    interactionHistory.total = Number(page?.total || 0)
  } catch (error: any) {
    if (sequence !== historyRequestSequence) return
    interactionHistory.records = []
    interactionHistory.total = 0
    ElMessage.error(error?.message || '历史对话加载失败')
  } finally {
    if (sequence === historyRequestSequence) interactionHistory.loading = false
  }
}

const openInteractionHistory = () => {
  interactionHistoryKeyword.value = ''
  interactionHistory.current = 1
  interactionHistoryVisible.value = true
  loadInteractionHistory()
}

const searchInteractionHistory = () => {
  interactionHistory.current = 1
  loadInteractionHistory()
}

const handleInteractionHistorySizeChange = () => {
  interactionHistory.current = 1
  loadInteractionHistory()
}

const startQuickAgent = (agent: QuickAgent) => {
  if (!agent.modelId) {
    ElMessage.warning('该智能体未绑定可用模型配置，请先完成配置')
    router.push({ path: '/agent/manage', query: { editAgentId: String(agent.id) } })
    return
  }
  router.push({
    name: 'AgentChat',
    params: { agentId: String(agent.id) },
    query: { agentName: agent.agentName, agentKey: agent.agentCode }
  })
}

// 初始化图表
const initChart = async () => {
  await nextTick()
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    updateChart()
  }
}

onMounted(async () => {
  await initChart()
  loadOverview()
})
</script>

<template>
  <div v-loading="loading" class="ai-overview">
    <!-- 错误提示 -->
    <el-alert
      v-if="loadError"
      class="mb-16px"
      type="error"
      :title="loadError"
      show-icon
      :closable="false"
    >
      <template #default>
        <el-button link type="primary" @click="loadOverview">重新加载</el-button>
      </template>
    </el-alert>

    <!-- 欢迎区域 -->
    <ContentWrap class="mb-16px">
      <div class="flex justify-between items-center">
        <div>
          <span class="text-gray-500 text-14px">{{ heroDate }}</span>
          <h2 class="text-20px font-600 mt-8px">
            {{ greeting }}，{{ userName }}
            <el-icon class="ml-8px"><MagicStick /></el-icon>
          </h2>
          <p class="text-gray-500 text-14px mt-8px">{{ healthText }}</p>
        </div>
        <el-button type="primary" size="large" :icon="Lightning" @click="handleCreateAgent">
          创建智能体
        </el-button>
      </div>
    </ContentWrap>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="mb-16px">
      <el-col v-for="card in statCards" :key="card.label" :xs="24" :sm="12" :md="6">
        <ContentWrap>
          <div class="flex items-center">
            <div
              class="w-48px h-48px rounded-8px flex items-center justify-center mr-16px"
              :class="`bg-${card.tone}-100`"
            >
              <el-icon :size="24" :class="`text-${card.tone}-600`">
                <component :is="card.icon" />
              </el-icon>
            </div>
            <div>
              <span class="text-gray-500 text-14px">{{ card.label }}</span>
              <strong class="block text-20px font-600">{{ card.value }}</strong>
              <small class="text-gray-400 text-12px">{{ card.delta }}</small>
            </div>
          </div>
        </ContentWrap>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="16">
      <!-- 运行趋势 -->
      <el-col :xs="24" :lg="16">
        <ContentWrap>
          <template #header>
            <div class="flex justify-between items-center w-full">
              <div>
                <h3 class="text-16px font-600">运行趋势</h3>
                <p class="text-gray-500 text-12px">运行次数与成功次数</p>
              </div>
              <div class="flex items-center gap-8px">
                <el-segmented
                  :model-value="rangeMode"
                  :options="rangeOptions"
                  @change="handleRangeModeChange"
                />
                <el-date-picker
                  v-if="rangeMode === '自定义'"
                  :model-value="customRange"
                  type="daterange"
                  value-format="YYYY-MM-DD"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  :disabled-date="disableFutureDate"
                  :clearable="false"
                  @update:model-value="handleCustomRangeChange"
                />
              </div>
            </div>
          </template>

          <div ref="chartRef" style="width: 100%; height: 320px"></div>

          <div class="grid grid-cols-4 gap-16px mt-16px pt-16px border-t border-gray-200">
            <div>
              <span class="text-gray-500 text-14px">总运行次数</span>
              <strong class="block text-18px font-600">{{ formatNumber(trendSummary.totalRuns) }}</strong>
              <small class="text-gray-400 text-12px">{{
                formatPercentChange(trendSummary.totalRunsChangePercent, '较上期')
              }}</small>
            </div>
            <div>
              <span class="text-gray-500 text-14px">总成功次数</span>
              <strong class="block text-18px font-600">{{
                formatNumber(trendSummary.successRuns)
              }}</strong>
              <small class="text-gray-400 text-12px">{{
                formatPercentChange(trendSummary.successRunsChangePercent, '较上期')
              }}</small>
            </div>
            <div>
              <span class="text-gray-500 text-14px">成功率</span>
              <strong class="block text-18px font-600">{{
                trendSummary.successRate == null ? '--' : `${Number(trendSummary.successRate).toFixed(1)}%`
              }}</strong>
              <small class="text-gray-400 text-12px">{{
                formatPointChange(trendSummary.successRateChange, '较上期')
              }}</small>
            </div>
            <div>
              <span class="text-gray-500 text-14px">平均耗时</span>
              <strong class="block text-18px font-600">{{
                formatDuration(trendSummary.averageDurationMs)
              }}</strong>
              <small class="text-gray-400 text-12px">{{
                formatDurationChange(trendSummary.averageDurationChangeMs, '较上期')
              }}</small>
            </div>
          </div>
        </ContentWrap>
      </el-col>

      <!-- 近期对话 -->
      <el-col :xs="24" :lg="8">
        <ContentWrap>
          <template #header>
            <div>
              <h3 class="text-16px font-600">近期对话</h3>
              <p class="text-gray-500 text-12px">最近 7 次与智能体的交互</p>
            </div>
          </template>

          <div v-if="recentInteractions.length" class="space-y-12px">
            <button
              v-for="interaction in recentInteractions"
              :key="interaction.runId"
              type="button"
              class="w-full p-12px rounded-8px bg-gray-50 hover:bg-gray-100 transition-colors text-left"
              :disabled="!interaction.agentAvailable"
              @click="openRecentInteraction(interaction)"
            >
              <div class="flex items-start">
                <span
                  class="w-32px h-32px rounded-full bg-blue-500 text-white flex items-center justify-center text-14px font-600 mr-12px flex-shrink-0"
                >
                  {{ interaction.agentInitial }}
                </span>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between mb-4px">
                    <strong class="text-14px">{{ interaction.agentName }}</strong>
                    <time class="text-gray-400 text-12px">{{
                      formatInteractionTime(interaction.startedAt)
                    }}</time>
                  </div>
                  <div class="text-12px text-gray-600 truncate">
                    <b>用户：</b>{{ interaction.userMessage }}
                  </div>
                  <div
                    class="text-12px mt-4px truncate"
                    :class="interaction.replyTone ? 'text-gray-400' : 'text-gray-600'"
                  >
                    <b>智能体：</b>{{ interaction.assistantPreview }}
                  </div>
                </div>
              </div>
            </button>
          </div>
          <el-empty v-else description="暂无近期对话" :image-size="64" />

          <el-button
            class="w-full mt-12px"
            text
            bg
            type="primary"
            @click="openInteractionHistory"
          >
            查看更多
            <el-icon class="ml-4px"><ArrowRight /></el-icon>
          </el-button>
        </ContentWrap>
      </el-col>
    </el-row>

    <!-- 快捷启动智能体 -->
    <ContentWrap class="mt-16px">
      <template #header>
        <div class="flex justify-between items-center w-full">
          <div>
            <h3 class="text-16px font-600">快捷启动智能体</h3>
            <p class="text-gray-500 text-12px">近 30 天常用智能体</p>
          </div>
          <el-button link type="primary" @click="openAgentManagement">管理</el-button>
        </div>
      </template>

      <div v-if="quickAgents.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-12px">
        <div
          v-for="agent in quickAgents"
          :key="agent.id"
          class="flex items-center p-12px rounded-8px bg-gray-50 hover:bg-gray-100 transition-colors"
        >
          <span
            class="w-40px h-40px rounded-8px flex items-center justify-center mr-12px"
            :class="`bg-${agent.tone}-100`"
          >
            <el-icon :size="20" :class="`text-${agent.tone}-600`">
              <component :is="agent.icon" />
            </el-icon>
          </span>
          <div class="flex-1 min-w-0">
            <strong class="text-14px block truncate">{{ agent.agentName }}</strong>
            <small class="text-gray-400 text-12px block truncate">
              {{ agent.description || '暂无描述' }} · {{ formatNumber(agent.runCount30Days) }} 次运行
            </small>
          </div>
          <div class="flex gap-8px ml-12px">
            <el-button
              circle
              size="small"
              :aria-label="`启动${agent.agentName}`"
              @click="startQuickAgent(agent)"
            >
              <el-icon><VideoPlay /></el-icon>
            </el-button>
            <el-button
              circle
              size="small"
              :aria-label="`管理${agent.agentName}`"
              @click="openAgentManagement"
            >
              <el-icon><More /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无可用智能体" :image-size="64" />

      <el-button class="w-full mt-12px" text bg type="primary" @click="openAgentManagement">
        查看更多智能体
        <el-icon class="ml-4px"><ArrowRight /></el-icon>
      </el-button>
    </ContentWrap>

    <!-- 历史对话弹窗 -->
    <el-dialog
      v-model="interactionHistoryVisible"
      title="历史对话"
      width="min(920px, calc(100vw - 32px))"
      align-center
      append-to-body
      destroy-on-close
    >
      <div class="flex gap-8px mb-16px">
        <el-input
          v-model="interactionHistoryKeyword"
          :prefix-icon="Search"
          clearable
          placeholder="查询用户消息、智能体回复或智能体名称"
          class="flex-1"
          @keyup.enter="searchInteractionHistory"
          @clear="searchInteractionHistory"
        />
        <el-button type="primary" :icon="Search" @click="searchInteractionHistory">
          查询
        </el-button>
      </div>

      <div v-loading="interactionHistory.loading" class="min-h-200px">
        <div v-if="interactionHistoryRows.length" class="space-y-12px">
          <button
            v-for="interaction in interactionHistoryRows"
            :key="interaction.runId"
            type="button"
            class="w-full p-12px rounded-8px bg-gray-50 hover:bg-gray-100 transition-colors text-left"
            :disabled="!interaction.agentAvailable"
            @click="openRecentInteraction(interaction)"
          >
            <div class="flex items-start">
              <span
                class="w-32px h-32px rounded-full bg-blue-500 text-white flex items-center justify-center text-14px font-600 mr-12px flex-shrink-0"
              >
                {{ interaction.agentInitial }}
              </span>
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-4px">
                  <strong class="text-14px">{{ interaction.agentName }}</strong>
                  <time class="text-gray-400 text-12px">{{
                    formatInteractionTime(interaction.startedAt)
                  }}</time>
                </div>
                <div class="text-12px text-gray-600 truncate">
                  <b>用户：</b>{{ interaction.userMessage }}
                </div>
                <div
                  class="text-12px mt-4px truncate"
                  :class="interaction.replyTone ? 'text-gray-400' : 'text-gray-600'"
                >
                  <b>智能体：</b>{{ interaction.assistantPreview }}
                </div>
              </div>
            </div>
          </button>
        </div>
        <el-empty
          v-else-if="!interactionHistory.loading"
          description="暂无符合条件的历史对话"
          :image-size="72"
        />
      </div>

      <el-pagination
        v-model:current-page="interactionHistory.current"
        v-model:page-size="interactionHistory.size"
        class="mt-16px"
        :page-sizes="[10, 20, 50]"
        :total="interactionHistory.total"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="loadInteractionHistory"
        @size-change="handleInteractionHistorySizeChange"
      />
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.ai-overview {
  :deep(.el-card__header) {
    padding: 12px 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(.el-card__body) {
    padding: 16px;
  }
}
</style>
