<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bottom, ChatLineRound, Close, Delete, MoreFilled, Plus, Promotion, Search } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import {
  chatStream,
  createAgentSession,
  deleteAgentSession,
  pageAgentMessages,
  pageAgentSessions
} from '@/api/agent/chat'

defineOptions({ name: 'AiAgentChat' })

const route = useRoute()
const markdownRenderer = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true
})
const defaultLinkOpenRenderer = markdownRenderer.renderer.rules.link_open || ((tokens, index, options, env, self) => self.renderToken(tokens, index, options))

markdownRenderer.renderer.rules.link_open = (tokens, index, options, env, self) => {
  tokens[index].attrSet('target', '_blank')
  tokens[index].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpenRenderer(tokens, index, options, env, self)
}

// ==================== 类型定义 ====================
interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  displayContent?: string
  contentSegments?: ContentSegment[]
  createdAt: string
  streaming?: boolean
  pending?: boolean
  typing?: boolean
  error?: boolean
  // 流式事件相关
  eventStages?: EventStage[]
  streamTiming?: StreamTiming | null
  streamStatus?: 'running' | 'waiting' | 'done' | 'error'
  usageToken?: number
  usageTime?: number
  // 辅助输出
  thinkingContent?: string
  thinkingExpanded?: boolean
  toolResultContent?: string
  toolResultExpanded?: boolean
  toolCalls?: ToolCall[]
  auxiliaryBlocks?: AuxiliaryBlock[]
  toolExpanded?: boolean
  // 计划相关
  planState?: PlanState | null
  planTasks?: PlanTask[]
  planProgress?: PlanProgress
  planExpanded?: boolean
  lastPlanEvent?: PlanEvent | null
  runId?: string | null
  // 辅助块状态
  activeReasoningBlockId?: string | null
  activeToolBlockId?: string | null
  activeToolBlockIds?: Record<string, string>
  activeSubAgentBlockIds?: Record<string, string>
  pendingReasoningStartedAt?: number | null
  pendingIntervention?: Intervention | null
}

interface ContentSegment {
  id: string
  type: 'content' | 'auxiliary'
  content?: string
  blockId?: string
  isLastContent?: boolean
}

interface EventStage {
  key: string
  label: string
  order: number
  startedAt: number
  endedAt: number | null
  durationMs: number
  eventCount: number
  lastEventType: string
  lastEventLabel: string
  gapMs: number
  status: 'running' | 'waiting' | 'done' | 'error'
}

interface StreamTiming {
  startedAt: number
  lastAt: number
  totalMs: number
  eventCount: number
}

interface ToolCall {
  id: string
  name: string
  process: string
  result: string
  rawInput: string
  rawResult: string
  status: 'running' | 'waiting' | 'waiting_external' | 'done' | 'error'
  argumentStarted: boolean
  resultStarted: boolean
  startedAt: number
  updatedAt: number
  pendingToolCall?: any
}

interface AuxiliaryBlock {
  id: string
  kind: 'reasoning' | 'tool' | 'subagent'
  sequence: number
  title: string
  expanded: boolean
  expandedByUser: boolean
  status: 'running' | 'waiting' | 'waiting_external' | 'done' | 'error'
  startedAt: number
  endedAt: number | null
  durationMs: number
  content: string
  toolCall?: ToolCall
  subAgent?: {
    name: string
    sourcePath: string
    status: string
  }
  subAgentName?: string
  sourcePath?: string
}

interface PlanState {
  title?: string
  planNo?: string
  status?: string
  [key: string]: any
}

interface PlanTask {
  id: string
  planId: string
  taskIndex: number
  subject: string
  detail: string
  state: string
  priority: string
  owner: string
  blocks: any[]
  blockedBy: any[]
  startedAt: string
  finishedAt: string
}

interface PlanProgress {
  total: number
  completed: number
  inProgress: number
  pending: number
  percent: number
}

interface PlanEvent {
  type: string
  toolName: string
  occurredAt: string
  toolCallId?: string
}

interface Intervention {
  type: 'userConfirm' | 'externalExecution'
  replyId: string
  runId: string
  toolCalls: any[]
}

interface Session {
  id: string
  backendSessionId?: string
  title: string
  messages: Message[]
  historyRecords?: any[]
  messagePaging?: MessagePaging
  hasMore?: boolean
  updatedAt?: string
}

interface MessagePaging {
  current: number
  size: number
  total: number
  loadedRecords: number
  loading: boolean
  finished: boolean
  initialized: boolean
}

// ==================== 常量定义 ====================
const STREAM_STAGE_META = {
  reasoning: { label: '推理事件', order: 10 },
  tool: { label: '工具调用事件', order: 20 },
  subagent: { label: '子智能体事件', order: 30 },
  message: { label: '正文事件', order: 40 }
}

const SSE_EVENT_LABELS = {
  agent_start: '智能体开始',
  agent_end: '智能体结束',
  model_call_start: '模型调用开始',
  model_call_end: '模型调用结束',
  message_start: '文本块开始',
  message_delta: '文本增量',
  message_end: '文本块结束',
  thinking_start: '思考开始',
  thinking_delta: '思考增量',
  thinking_end: '思考结束',
  tool_call_start: '工具调用开始',
  tool_call_delta: '工具参数增量',
  tool_call_end: '工具调用结束',
  tool_result_start: '工具执行开始',
  tool_result_text_delta: '工具文本结果',
  tool_result_data_delta: '工具数据结果',
  tool_result_end: '工具执行完成',
  require_user_confirm: '等待用户确认',
  require_external_execution: '等待外部执行',
  user_confirm_result: '用户确认结果',
  external_execution_result: '外部执行结果',
  subagent_exposed: '子智能体暴露',
  subagent_agent_start: '子智能体开始',
  subagent_agent_end: '子智能体结束',
  subagent_message_start: '子智能体文本开始',
  subagent_message_delta: '子智能体文本增量',
  subagent_message_end: '子智能体文本结束',
  subagent_thinking_start: '子智能体思考开始',
  subagent_thinking_delta: '子智能体思考增量',
  subagent_thinking_end: '子智能体思考结束',
  request_stop: '请求停止',
  exceed_max_iters: '达到最大迭代',
  error: '执行异常'
}

const PLAN_STATUS_META = {
  DRAFT: { label: '草稿', type: 'info' },
  WAITING_APPROVAL: { label: '待确认', type: 'warning' },
  APPROVED: { label: '已批准', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  EXECUTING: { label: '执行中', type: 'warning' },
  COMPLETED: { label: '已完成', type: 'success' },
  FAILED: { label: '失败', type: 'danger' },
  CANCELLED: { label: '已取消', type: 'info' }
}

const TASK_STATE_META = {
  PENDING: { label: '待执行', type: 'info' },
  IN_PROGRESS: { label: '执行中', type: 'warning' },
  COMPLETED: { label: '已完成', type: 'success' },
  BLOCKED: { label: '阻塞', type: 'danger' },
  FAILED: { label: '失败', type: 'danger' },
  CANCELLED: { label: '已取消', type: 'info' }
}

const SCROLL_BOTTOM_THRESHOLD = 64
const SCROLL_PAGE_THRESHOLD = 80
const SESSION_PAGE_SIZE = 20
const MESSAGE_PAGE_SIZE = 30

// ==================== 响应式状态 ====================
const sessions = ref<Session[]>([])
const activeSessionId = ref('')
const inputMessage = ref('')
const sessionKeyword = ref('')
const planDrawerOpen = ref(false)
const streaming = ref(false)
const sessionCreating = ref(false)
const autoScrollEnabled = ref(true)
const sessionListRef = ref()
const messageListRef = ref()
const abortController = ref<AbortController | null>(null)

let scrollFrame: number | null = null
let pendingScrollForce = false
let sessionSearchTimer: ReturnType<typeof setTimeout> | null = null

const sessionPaging = reactive({
  current: 0,
  size: SESSION_PAGE_SIZE,
  total: 0,
  loading: false,
  finished: false
})

const agentInfo = reactive({
  id: null as string | null,
  name: '',
  key: ''
})

// ==================== 计算属性 ====================
const agentId = computed(() => {
  const value = route.params.agentId
  return Array.isArray(value) ? String(value[0] || '') : String(value || '')
})

const activeSession = computed(() => {
  return sessions.value.find((item) => item.id === activeSessionId.value) || null
})

const activeMessages = computed(() => {
  return activeSession.value?.messages || []
})

const canSend = computed(() => {
  return Boolean(inputMessage.value.trim()) && !streaming.value && !sessionCreating.value && Boolean(agentId.value)
})

const latestAssistantMessage = computed(() => {
  const messages = activeMessages.value
  for (let index = messages.length - 1; index >= 0; index -= 1) {
    if (messages[index]?.role === 'assistant') {
      return messages[index]
    }
  }
  return null
})

const activePlanMessage = computed(() => {
  const message = latestAssistantMessage.value
  return message && hasPlanOutput(message) ? message : null
})

const executionPlanProgress = computed(() => {
  const message = activePlanMessage.value
  return message?.planProgress || buildPlanProgress(message?.planTasks || [])
})

// ==================== 工具函数 ====================
const getNowMs = () => {
  return typeof performance !== 'undefined' ? performance.now() : Date.now()
}

const nowText = () => {
  const date = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const formatDuration = (duration = 0) => {
  const milliseconds = Math.max(0, Math.round(duration || 0))
  if (milliseconds < 1000) {
    return `${milliseconds}ms`
  }
  if (milliseconds < 10000) {
    return `${(milliseconds / 1000).toFixed(1)}s`
  }
  return `${Math.round(milliseconds / 1000)}s`
}

const eventLabel = (eventName = '') => {
  return SSE_EVENT_LABELS[eventName] || eventName || '未知事件'
}

const normalizedSseEvent = (streamEvent: any) => String(streamEvent?.sseEvent || '').toLowerCase()

const getStageKey = (sseEvent = '') => {
  const normalizedEvent = String(sseEvent || '').toLowerCase()

  if (normalizedEvent.startsWith('subagent_')) {
    return 'subagent'
  }
  if (normalizedEvent.startsWith('thinking_')) {
    return 'reasoning'
  }
  if (normalizedEvent.startsWith('tool_call_') || normalizedEvent.startsWith('tool_result_')) {
    return 'tool'
  }
  if (normalizedEvent === 'require_user_confirm' ||
    normalizedEvent === 'require_external_execution' ||
    normalizedEvent === 'user_confirm_result' ||
    normalizedEvent === 'external_execution_result') {
    return 'tool'
  }
  if (normalizedEvent.startsWith('message_')) {
    return 'message'
  }

  return null
}

const isStageEndEvent = (sseEvent = '') => {
  const normalizedEvent = String(sseEvent || '').toLowerCase()
  return normalizedEvent === 'error' ||
    normalizedEvent === 'thinking_end' ||
    normalizedEvent === 'tool_result_end' ||
    normalizedEvent === 'message_end' ||
    normalizedEvent === 'agent_end' ||
    normalizedEvent === 'request_stop' ||
    normalizedEvent === 'exceed_max_iters' ||
    normalizedEvent === 'require_user_confirm' ||
    normalizedEvent === 'require_external_execution'
}

const isReasoningStartEvent = (streamEvent: any) => {
  return normalizedSseEvent(streamEvent) === 'thinking_start'
}

const isReasoningDeltaEvent = (streamEvent: any) => {
  return normalizedSseEvent(streamEvent) === 'thinking_delta'
}

const isReasoningEndEvent = (streamEvent: any) => {
  return normalizedSseEvent(streamEvent) === 'thinking_end'
}

const isReasoningStreamEvent = (streamEvent: any) => {
  return isReasoningStartEvent(streamEvent) ||
    isReasoningDeltaEvent(streamEvent) ||
    isReasoningEndEvent(streamEvent)
}

const isToolStreamEvent = (streamEvent: any) => {
  const sseEvent = normalizedSseEvent(streamEvent)
  return sseEvent.startsWith('tool_call_') ||
    sseEvent.startsWith('tool_result_')
}

const isToolCallStartEvent = (streamEvent: any) => {
  return normalizedSseEvent(streamEvent) === 'tool_call_start'
}

const isToolCallDeltaEvent = (streamEvent: any) => {
  return normalizedSseEvent(streamEvent) === 'tool_call_delta'
}

const isToolCallEndEvent = (streamEvent: any) => {
  return normalizedSseEvent(streamEvent) === 'tool_call_end'
}

const isToolResultStartEvent = (streamEvent: any) => {
  return normalizedSseEvent(streamEvent) === 'tool_result_start'
}

const isToolResultTextDeltaEvent = (streamEvent: any) => {
  return normalizedSseEvent(streamEvent) === 'tool_result_text_delta'
}

const isToolResultDataDeltaEvent = (streamEvent: any) => {
  return normalizedSseEvent(streamEvent) === 'tool_result_data_delta'
}

const isToolResultEndEvent = (streamEvent: any) => {
  return normalizedSseEvent(streamEvent) === 'tool_result_end'
}

const isSubAgentStreamEvent = (streamEvent: any) => {
  const sseEvent = normalizedSseEvent(streamEvent)
  return sseEvent.startsWith('subagent_')
}

const isSubAgentTextDeltaEvent = (streamEvent: any) => {
  return normalizedSseEvent(streamEvent) === 'subagent_message_delta'
}

const isSubAgentThinkingDeltaEvent = (streamEvent: any) => {
  return normalizedSseEvent(streamEvent) === 'subagent_thinking_delta'
}

const isSubAgentStartEvent = (streamEvent: any) => {
  if (!isSubAgentStreamEvent(streamEvent)) {
    return false
  }
  const sseEvent = normalizedSseEvent(streamEvent)
  return sseEvent === 'subagent_agent_start' ||
    sseEvent === 'subagent_message_start'
}

const isSubAgentEndEvent = (streamEvent: any) => {
  if (!isSubAgentStreamEvent(streamEvent)) {
    return false
  }
  const sseEvent = normalizedSseEvent(streamEvent)
  return sseEvent === 'subagent_agent_end' ||
    sseEvent === 'subagent_message_end' ||
    sseEvent === 'subagent_thinking_end'
}

const isRecord = (value: any) => Boolean(value) && typeof value === 'object' && !Array.isArray(value)

const createToolCallId = () => {
  return `tool-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`
}

const createToolCall = (name = '') => {
  const now = getNowMs()
  return {
    id: createToolCallId(),
    name: name || '工具调用',
    process: '',
    result: '',
    rawInput: '',
    rawResult: '',
    status: 'running' as const,
    argumentStarted: false,
    resultStarted: false,
    startedAt: now,
    updatedAt: now
  }
}

const createAuxiliaryBlockId = (kind: string) => {
  return `${kind}-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`
}

const createContentSegmentId = () => {
  return `content-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`
}

const ensureAuxiliaryBlocks = (message: Message) => {
  if (!Array.isArray(message.auxiliaryBlocks)) {
    message.auxiliaryBlocks = []
  }
  return message.auxiliaryBlocks
}

const ensureContentSegments = (message: Message) => {
  if (!Array.isArray(message.contentSegments)) {
    message.contentSegments = []
  }
  return message.contentSegments
}

const appendContentSegment = (message: Message, delta: string) => {
  const text = delta == null ? '' : String(delta)
  if (!text) {
    return
  }

  const segments = ensureContentSegments(message)
  const lastSegment = segments[segments.length - 1]
  if (lastSegment?.type === 'content') {
    lastSegment.content = `${lastSegment.content || ''}${text}`
    return
  }

  segments.push({
    id: createContentSegmentId(),
    type: 'content',
    content: text
  })
}

const createAuxiliaryBlock = (message: Message, kind: 'reasoning' | 'tool' | 'subagent', extra: any = {}) => {
  const now = getNowMs()
  const sequence = ensureAuxiliaryBlocks(message).filter((block) => block.kind === kind).length + 1
  const defaultTitle = kind === 'tool'
    ? `工具调用 ${sequence}`
    : kind === 'subagent'
      ? `子智能体 ${sequence}`
      : `推理过程 ${sequence}`
  return {
    id: createAuxiliaryBlockId(kind),
    kind,
    sequence,
    title: defaultTitle,
    expanded: false,
    expandedByUser: false,
    status: 'running' as const,
    startedAt: now,
    endedAt: null,
    durationMs: 0,
    content: '',
    ...extra
  }
}

const touchAuxiliaryBlock = (block: AuxiliaryBlock, status: 'running' | 'waiting' | 'waiting_external' | 'done' | 'error' = 'running') => {
  const now = getNowMs()
  block.status = status
  block.durationMs = block.startedAt ? now - block.startedAt : 0
  if (status === 'running') {
    block.endedAt = null
    return
  }
  block.endedAt = now
}

const findAuxiliaryBlock = (message: Message, blockId: string) => {
  if (!blockId) {
    return null
  }
  return ensureAuxiliaryBlocks(message).find((block) => block.id === blockId) || null
}

const findLastRunningBlock = (message: Message, kind: 'reasoning' | 'tool' | 'subagent') => {
  const blocks = ensureAuxiliaryBlocks(message)
  for (let index = blocks.length - 1; index >= 0; index -= 1) {
    const block = blocks[index]
    if (block.kind === kind && block.status === 'running') {
      return block
    }
  }
  return null
}

const nextAuxiliaryBlockSequence = (message: Message, kind: 'reasoning' | 'tool' | 'subagent') => {
  return ensureAuxiliaryBlocks(message).filter((block) => block.kind === kind).length + 1
}

const appendAuxiliaryBlockSegment = (message: Message, block: AuxiliaryBlock) => {
  if (!message || !block?.id) {
    return
  }

  const segments = ensureContentSegments(message)
  if (segments.some((segment) => segment.type === 'auxiliary' && segment.blockId === block.id)) {
    return
  }

  segments.push({
    id: `auxiliary-${block.id}`,
    type: 'auxiliary',
    blockId: block.id
  })
}

const getReasoningBlock = (message: Message, shouldCreate = false) => {
  let block = findAuxiliaryBlock(message, message.activeReasoningBlockId || '')

  if (!block && !shouldCreate) {
    block = findLastRunningBlock(message, 'reasoning')
  }

  if (!block && shouldCreate) {
    block = createAuxiliaryBlock(message, 'reasoning', {
      startedAt: message.pendingReasoningStartedAt || getNowMs()
    })
    ensureAuxiliaryBlocks(message).push(block)
    appendAuxiliaryBlockSegment(message, block)
  }

  if (block) {
    message.activeReasoningBlockId = block.id
  }

  return block
}

const ensureToolCalls = (message: Message) => {
  if (!Array.isArray(message.toolCalls)) {
    message.toolCalls = []
  }
  return message.toolCalls
}

const ensureToolBlockIds = (message: Message) => {
  if (!message.activeToolBlockIds || typeof message.activeToolBlockIds !== 'object') {
    message.activeToolBlockIds = {}
  }
  return message.activeToolBlockIds
}

const streamPayload = (streamEvent: any = {}) => {
  return isRecord(streamEvent.payload) ? streamEvent.payload : {}
}

const streamToolCallId = (streamEvent: any = {}) => {
  const payload = streamPayload(streamEvent)
  return String(payload.toolCallId || payload.tool_call_id || payload.id || '')
}

const appendToolLine = (toolCall: ToolCall, key: 'process' | 'result', text: string) => {
  const value = String(text || '').trim()
  if (!value) {
    return
  }
  toolCall[key] = toolCall[key] ? `${toolCall[key]}\n${value}` : value
}

const appendToolText = (toolCall: ToolCall, key: 'process' | 'result', text: string) => {
  const value = text == null ? '' : String(text)
  if (!value) {
    return
  }
  toolCall[key] = `${toolCall[key] || ''}${value}`
}

const getToolBlock = (message: Message, shouldCreate = false, toolName = '', toolCallId = '') => {
  const callId = String(toolCallId || '')
  const activeIds = ensureToolBlockIds(message)
  let block = callId ? findAuxiliaryBlock(message, activeIds[callId]) : null

  if (!block && callId) {
    block = ensureAuxiliaryBlocks(message).find((item) => item.kind === 'tool' && item.toolCall?.id === callId) || null
  }

  if (!block && !callId) {
    block = shouldCreate ? null : findAuxiliaryBlock(message, message.activeToolBlockId || '')
  }

  if (!block && !shouldCreate && !callId) {
    block = findLastRunningBlock(message, 'tool')
  }

  if (!block && (shouldCreate || callId || !message.activeToolBlockId)) {
    block = createAuxiliaryBlock(message, 'tool', { toolCall: createToolCall(toolName) })
    if (callId) {
      block.toolCall!.id = callId
    }
    ensureAuxiliaryBlocks(message).push(block)
    ensureToolCalls(message).push(block.toolCall!)
    appendAuxiliaryBlockSegment(message, block)
  }

  if (block) {
    message.activeToolBlockId = block.id
    if (callId) {
      activeIds[callId] = block.id
      if (block.toolCall) {
        block.toolCall.id = callId
      }
    }
  }

  return block
}

const getToolCallFromBlock = (message: Message, block: AuxiliaryBlock, toolName = '') => {
  if (!block.toolCall) {
    block.toolCall = createToolCall(toolName)
    ensureToolCalls(message).push(block.toolCall)
  }

  if (toolName && (!block.toolCall.name || block.toolCall.name === '工具调用')) {
    block.toolCall.name = toolName
  }

  return block.toolCall
}

const normalizeSourcePath = (sourcePath = '') => String(sourcePath || '').trim()

const subAgentNameFromSource = (sourcePath = '') => {
  const normalizedPath = normalizeSourcePath(sourcePath).replace(/\\/g, '/')
  if (!normalizedPath || normalizedPath.toLowerCase() === 'main') {
    return ''
  }
  const parts = normalizedPath.split('/').filter(Boolean)
  return parts[parts.length - 1] || normalizedPath
}

const getSubAgentName = (streamEvent: any) => {
  return streamEvent?.subAgentName || subAgentNameFromSource(streamEvent?.sourcePath) || ''
}

const subAgentBlockKey = (streamEvent: any) => {
  return normalizeSourcePath(streamEvent?.sourcePath) || getSubAgentName(streamEvent) || 'subagent'
}

const ensureSubAgentBlockIds = (message: Message) => {
  if (!message.activeSubAgentBlockIds || typeof message.activeSubAgentBlockIds !== 'object') {
    message.activeSubAgentBlockIds = {}
  }
  return message.activeSubAgentBlockIds
}

const findRunningSubAgentBlock = (message: Message, key: string, name: string) => {
  const blocks = ensureAuxiliaryBlocks(message)
  for (let index = blocks.length - 1; index >= 0; index -= 1) {
    const block = blocks[index]
    if (block.kind !== 'subagent' || block.status !== 'running') {
      continue
    }
    if (block.subAgent?.sourcePath === key || block.subAgent?.name === name) {
      return block
    }
  }
  return null
}

const getSubAgentBlock = (message: Message, streamEvent: any, shouldCreate = false) => {
  const name = getSubAgentName(streamEvent) || '子智能体'
  const key = subAgentBlockKey(streamEvent)
  const activeIds = ensureSubAgentBlockIds(message)
  let block = findAuxiliaryBlock(message, activeIds[key])

  if (!block && !shouldCreate) {
    block = findRunningSubAgentBlock(message, key, name)
  }

  if (!block && shouldCreate) {
    block = createAuxiliaryBlock(message, 'subagent', {
      title: `子智能体 ${name}`,
      expanded: true,
      subAgentName: name,
      sourcePath: normalizeSourcePath(streamEvent?.sourcePath),
      subAgent: {
        name,
        sourcePath: normalizeSourcePath(streamEvent?.sourcePath),
        status: 'running'
      }
    })
    ensureAuxiliaryBlocks(message).push(block)
    appendAuxiliaryBlockSegment(message, block)
  }

  if (!block) {
    return null
  }

  block.subAgentName = name
  block.sourcePath = normalizeSourcePath(streamEvent?.sourcePath)
  block.subAgent = {
    ...(block.subAgent || {}),
    name,
    sourcePath: block.sourcePath,
    status: block.status || 'running'
  }
  activeIds[key] = block.id
  return block
}

// ==================== 计划相关函数 ====================
const normalizePlanStatus = (status = '') => {
  const normalized = String(status || '').toUpperCase()
  const alias: Record<string, string> = {
    RUNNING: 'EXECUTING',
    IN_PROGRESS: 'EXECUTING',
    WAITING: 'WAITING_APPROVAL',
    WAITING_CONFIRM: 'WAITING_APPROVAL',
    WAITING_USER_CONFIRM: 'WAITING_APPROVAL',
    DONE: 'COMPLETED',
    SUCCESS: 'COMPLETED',
    FINISHED: 'COMPLETED',
    ERROR: 'FAILED'
  }
  return alias[normalized] || normalized
}

const normalizeTaskState = (state = '') => {
  const normalized = String(state || 'PENDING').toUpperCase()
  const alias: Record<string, string> = {
    TODO: 'PENDING',
    WAITING: 'PENDING',
    RUNNING: 'IN_PROGRESS',
    EXECUTING: 'IN_PROGRESS',
    PROCESSING: 'IN_PROGRESS',
    DONE: 'COMPLETED',
    SUCCESS: 'COMPLETED',
    FINISHED: 'COMPLETED',
    ERROR: 'FAILED'
  }
  return alias[normalized] || normalized
}

const planStatusMeta = (status: string) => PLAN_STATUS_META[normalizePlanStatus(status)] || { label: status || '未知', type: 'info' }
const taskStateMeta = (state: string) => TASK_STATE_META[normalizeTaskState(state)] || { label: state || '未知', type: 'info' }
const planStatusText = (status: string) => planStatusMeta(status).label
const planStatusType = (status: string) => planStatusMeta(status).type
const taskStateText = (state: string) => taskStateMeta(state).label
const taskStateType = (state: string) => taskStateMeta(state).type

const normalizePlanTask = (task: any = {}, index = 0): PlanTask => ({
  id: task.id || `${task.planId || 'plan'}-${task.taskIndex || index + 1}`,
  planId: task.planId || '',
  taskIndex: Number(task.taskIndex ?? task.index ?? task.step ?? task.order) || index + 1,
  subject: task.subject || task.title || task.name || task.content || task.task || `任务 ${index + 1}`,
  detail: task.detail || task.description || task.desc || task.summary || '',
  state: normalizeTaskState(task.state || task.status || task.taskState),
  priority: task.priority || task.level || '',
  owner: task.owner || '',
  blocks: Array.isArray(task.blocks) ? task.blocks : [],
  blockedBy: Array.isArray(task.blockedBy) ? task.blockedBy : [],
  startedAt: task.startedAt || '',
  finishedAt: task.finishedAt || ''
})

const buildPlanProgress = (tasks: PlanTask[] = []): PlanProgress => {
  const total = tasks.length
  const completed = tasks.filter((task) => normalizeTaskState(task.state) === 'COMPLETED').length
  const inProgress = tasks.filter((task) => normalizeTaskState(task.state) === 'IN_PROGRESS').length
  const pending = tasks.filter((task) => normalizeTaskState(task.state) === 'PENDING').length
  return {
    total,
    completed,
    inProgress,
    pending,
    percent: total ? Math.round((completed * 100) / total) : 0
  }
}

const hasPlanOutput = (message: Message) => {
  return Boolean(
    message?.planState ||
    message?.lastPlanEvent ||
    (Array.isArray(message?.planTasks) && message.planTasks.length)
  )
}

const planPanelTitle = (message: Message) => {
  return message?.planState?.title || message?.planState?.planNo || '执行计划'
}

const planProgressText = (message: Message) => {
  const progress = message?.planProgress || buildPlanProgress(message?.planTasks || [])
  return progress.total ? `${progress.completed}/${progress.total}` : ''
}

const executionTaskStateClass = (state: string) => normalizeTaskState(state).toLowerCase()

// ==================== 消息处理函数 ====================
const appendReasoningEvent = (message: Message, streamEvent: any) => {
  if (!message || !isReasoningStreamEvent(streamEvent)) {
    return false
  }

  const delta = streamEvent?.delta == null ? '' : String(streamEvent.delta)
  if (isReasoningStartEvent(streamEvent)) {
    const activeBlock = getReasoningBlock(message)
    if (activeBlock && activeBlock.content) {
      touchAuxiliaryBlock(activeBlock, 'done')
    }
    message.activeReasoningBlockId = null
    message.pendingReasoningStartedAt = getNowMs()
    return false
  }

  const block = getReasoningBlock(message, isReasoningDeltaEvent(streamEvent))
  if (!block) {
    message.pendingReasoningStartedAt = null
    return false
  }

  if (isReasoningDeltaEvent(streamEvent)) {
    block.content = `${block.content || ''}${delta}`
    message.thinkingContent = `${message.thinkingContent || ''}${delta}`
  }

  if (isReasoningEndEvent(streamEvent)) {
    touchAuxiliaryBlock(block, 'done')
    message.activeReasoningBlockId = null
    message.pendingReasoningStartedAt = null
  } else {
    touchAuxiliaryBlock(block, 'running')
  }

  return true
}

const appendToolEvent = (message: Message, streamEvent: any) => {
  if (!message || !isToolStreamEvent(streamEvent)) {
    return false
  }

  const delta = streamEvent?.delta == null ? '' : String(streamEvent.delta)
  const isStartEvent = isToolCallStartEvent(streamEvent)
  const toolCallId = streamToolCallId(streamEvent)
  const block = getToolBlock(message, isStartEvent, isStartEvent ? delta : '', toolCallId)
  const toolCall = getToolCallFromBlock(message, block, isStartEvent ? delta : '')
  if (toolCallId) {
    toolCall.id = toolCallId
  }
  toolCall.updatedAt = getNowMs()

  if (isStartEvent) {
    if (delta) {
      toolCall.name = delta
    }
    appendToolLine(toolCall, 'process', '开始调用工具')
    touchAuxiliaryBlock(block, 'running')
    return true
  }

  if (isToolCallDeltaEvent(streamEvent)) {
    if (!toolCall.argumentStarted) {
      appendToolLine(toolCall, 'process', '调用参数：')
      toolCall.argumentStarted = true
    }
    toolCall.rawInput = `${toolCall.rawInput || ''}${delta}`
    appendToolText(toolCall, 'process', delta)
    touchAuxiliaryBlock(block, 'running')
    return true
  }

  if (isToolCallEndEvent(streamEvent)) {
    appendToolLine(toolCall, 'process', '调用参数准备完成')
    touchAuxiliaryBlock(block, 'running')
    return true
  }

  if (isToolResultStartEvent(streamEvent)) {
    if (!toolCall.resultStarted) {
      appendToolLine(toolCall, 'process', '工具开始执行')
      toolCall.resultStarted = true
    }
    touchAuxiliaryBlock(block, 'running')
    return true
  }

  if (isToolResultTextDeltaEvent(streamEvent)) {
    toolCall.rawResult = `${toolCall.rawResult || ''}${delta}`
    appendToolText(toolCall, 'result', delta)
    touchAuxiliaryBlock(block, 'running')
    return true
  }

  if (isToolResultDataDeltaEvent(streamEvent)) {
    toolCall.rawResult = `${toolCall.rawResult || ''}${delta}`
    appendToolText(toolCall, 'result', delta)
    touchAuxiliaryBlock(block, 'running')
    return true
  }

  if (isToolResultEndEvent(streamEvent)) {
    toolCall.status = 'done'
    touchAuxiliaryBlock(block, 'done')
    if (!toolCallId || message.activeToolBlockId === block.id) {
      message.activeToolBlockId = null
    }
    if (toolCallId) {
      delete ensureToolBlockIds(message)[toolCallId]
    }
    return true
  }

  touchAuxiliaryBlock(block, 'running')
  return true
}

const appendSubAgentEvent = (message: Message, streamEvent: any) => {
  if (!message || !isSubAgentStreamEvent(streamEvent)) {
    return false
  }

  const shouldCreate = isSubAgentStartEvent(streamEvent) ||
    isSubAgentTextDeltaEvent(streamEvent) ||
    isSubAgentThinkingDeltaEvent(streamEvent)
  const shouldFinish = isSubAgentEndEvent(streamEvent)
  const shouldShowExposed = normalizedSseEvent(streamEvent) === 'subagent_exposed'
  if (!shouldCreate && !shouldFinish && !shouldShowExposed) {
    return false
  }

  const block = getSubAgentBlock(message, streamEvent, shouldCreate)
  if (!block) {
    return false
  }
  const delta = streamEvent?.delta == null ? '' : String(streamEvent.delta)

  if (shouldShowExposed) {
    const label = streamEvent?.payload?.label || getSubAgentName(streamEvent) || '子智能体'
    block.title = `子智能体 ${label}`
    appendToolLine(block.toolCall || createToolCall(), 'process', `${label} 已准备就绪`)
    touchAuxiliaryBlock(block, 'done')
    return true
  }

  if (isSubAgentTextDeltaEvent(streamEvent)) {
    block.content = `${block.content || ''}${delta}`
    touchAuxiliaryBlock(block, 'running')
    return true
  }

  if (isSubAgentThinkingDeltaEvent(streamEvent)) {
    block.content = `${block.content || ''}${delta}`
    touchAuxiliaryBlock(block, 'running')
    return true
  }

  if (shouldFinish) {
    touchAuxiliaryBlock(block, 'done')
    const activeIds = ensureSubAgentBlockIds(message)
    delete activeIds[subAgentBlockKey(streamEvent)]
    return true
  }

  if (isSubAgentStartEvent(streamEvent)) {
    touchAuxiliaryBlock(block, 'running')
    return true
  }

  return false
}

const ensureStreamTiming = (message: Message) => {
  if (!message.streamTiming) {
    message.streamTiming = {
      startedAt: 0,
      lastAt: 0,
      totalMs: 0,
      eventCount: 0
    }
  }
  if (!Array.isArray(message.eventStages)) {
    message.eventStages = []
  }
  return message.streamTiming
}

const recordStreamEvent = (message: Message, streamEvent: any) => {
  if (!message || !streamEvent) {
    return
  }

  const timing = ensureStreamTiming(message)
  const now = getNowMs()
  const eventName = streamEvent.sseEvent || 'unknown'
  const stageKey = getStageKey(eventName)
  if (!stageKey) {
    return
  }

  const stageMeta = STREAM_STAGE_META[stageKey as keyof typeof STREAM_STAGE_META]

  if (!timing.startedAt) {
    timing.startedAt = now
    timing.lastAt = now
  }

  let stage = message.eventStages!.find((item) => item.key === stageKey)
  if (!stage) {
    stage = {
      key: stageKey,
      label: stageMeta.label,
      order: stageMeta.order,
      startedAt: now,
      endedAt: null,
      durationMs: 0,
      eventCount: 0,
      lastEventType: '',
      lastEventLabel: '',
      gapMs: 0,
      status: 'running'
    }
    message.eventStages!.push(stage)
    message.eventStages!.sort((left, right) => left.order - right.order)
  }

  const gapMs = timing.eventCount > 0 ? now - timing.lastAt : 0
  stage.eventCount += 1
  stage.lastEventType = eventName
  stage.lastEventLabel = eventLabel(eventName)
  stage.gapMs = gapMs
  stage.durationMs = now - stage.startedAt
  stage.status = eventName === 'error' ? 'error' : isStageEndEvent(eventName) ? 'done' : 'running'

  if (stage.status !== 'running') {
    stage.endedAt = now
    stage.durationMs = stage.endedAt - stage.startedAt
  }

  timing.lastAt = now
  timing.eventCount += 1
  timing.totalMs = now - timing.startedAt
  message.streamStatus = eventName === 'error' ? 'error' : 'running'

  scheduleScrollToBottom()
}

const finishStreamEvents = (message: Message, status: 'running' | 'waiting' | 'done' | 'error' = 'done') => {
  if (!message?.streamTiming) {
    return
  }

  const now = getNowMs()
  const isWaiting = status === 'waiting'
  message.streamTiming.totalMs = now - message.streamTiming.startedAt
  message.streamStatus = status

  if (Array.isArray(message.eventStages)) {
    message.eventStages.forEach((stage) => {
      if (stage.status === 'running') {
        stage.status = status === 'error' ? 'error' : isWaiting ? 'waiting' : 'done'
        stage.endedAt = now
        stage.durationMs = stage.endedAt - stage.startedAt
      }
    })
  }

  if (Array.isArray(message.toolCalls)) {
    message.toolCalls.forEach((toolCall) => {
      if (toolCall.status === 'running') {
        toolCall.status = status === 'error' ? 'error' : isWaiting ? 'waiting' : 'done'
        toolCall.updatedAt = now
      }
    })
  }

  if (Array.isArray(message.auxiliaryBlocks)) {
    message.auxiliaryBlocks.forEach((block) => {
      if (block.status === 'running') {
        block.status = status === 'error' ? 'error' : isWaiting ? 'waiting' : 'done'
        block.endedAt = now
        block.durationMs = block.startedAt ? block.endedAt - block.startedAt : block.durationMs || 0
      }
      if (block.toolCall?.status === 'running') {
        block.toolCall.status = status === 'error' ? 'error' : isWaiting ? 'waiting' : 'done'
        block.toolCall.updatedAt = now
      }
    })
  }

  message.activeReasoningBlockId = null
  message.activeToolBlockId = null
  message.activeToolBlockIds = {}
  message.activeSubAgentBlockIds = {}
  message.pendingReasoningStartedAt = null
}

const appendStreamText = (message: Message, session: Session | null, delta: string) => {
  if (!delta) {
    return
  }

  message.content = `${message.content || ''}${delta}`
  message.displayContent = message.content
  appendContentSegment(message, delta)
  message.pending = false
  message.typing = true
  if (session) {
    session.updatedAt = nowText()
  }
  scheduleScrollToBottom()
}

const appendAuxiliaryDelta = (message: Message, session: Session | null, streamEvent: any) => {
  if (!message || !streamEvent) {
    return
  }

  let changed = false

  const subAgentEvent = isSubAgentStreamEvent(streamEvent)

  if (appendSubAgentEvent(message, streamEvent)) {
    changed = true
  }

  if (!subAgentEvent) {
    if (appendReasoningEvent(message, streamEvent)) {
      changed = true
    }

    if (appendToolEvent(message, streamEvent)) {
      changed = true
    }
  }

  if (!changed) {
    return
  }

  if (session) {
    session.updatedAt = nowText()
  }

  scheduleScrollToBottom()
}

// ==================== 消息展示函数 ====================
const assistantMessageItems = (message: Message) => {
  if (!message || message.role !== 'assistant') {
    return []
  }

  const blocks = Array.isArray(message.auxiliaryBlocks) ? message.auxiliaryBlocks : []
  const blocksById = new Map(blocks.map((block) => [block.id, block]))
  const segments = Array.isArray(message.contentSegments) && message.contentSegments.length
    ? message.contentSegments
    : []
  const referencedBlocks = new Set<string>()
  const items: Array<{ id: string; type: 'content' | 'auxiliary'; content?: string; block?: AuxiliaryBlock; isLastContent?: boolean }> = []

  segments.forEach((segment, index) => {
    if (segment?.type === 'content') {
      const content = segment.content == null ? '' : String(segment.content)
      if (content) {
        items.push({
          id: segment.id || `content-${index}`,
          type: 'content',
          content
        })
      }
      return
    }

    const block = blocksById.get(segment?.blockId || '')
    if (block) {
      referencedBlocks.add(block.id)
      items.push({
        id: segment.id || `auxiliary-${block.id}`,
        type: 'auxiliary',
        block
      })
    }
  })

  blocks.forEach((block) => {
    if (block?.id && !referencedBlocks.has(block.id)) {
      items.push({
        id: `auxiliary-${block.id}`,
        type: 'auxiliary',
        block
      })
    }
  })

  if (!items.length && (message.content || message.displayContent)) {
    items.push({
      id: `content-${message.id}`,
      type: 'content',
      content: message.content || message.displayContent || ''
    })
  }

  for (let index = items.length - 1; index >= 0; index -= 1) {
    if (items[index].type === 'content') {
      items[index].isLastContent = true
      break
    }
  }

  return items
}

const auxiliaryBlockTitle = (block: AuxiliaryBlock) => {
  if (block?.title) {
    return block.title
  }

  const sequence = block?.sequence ? ` ${block.sequence}` : ''
  if (block?.kind === 'tool') {
    return `工具调用${sequence}`
  }
  if (block?.kind === 'subagent') {
    return `子智能体${sequence}`
  }
  return `推理过程${sequence}`
}

const auxiliaryBlockTimeText = (block: AuxiliaryBlock) => {
  return block?.durationMs ? formatDuration(block.durationMs) : ''
}

const toolStatusText = (toolCall: ToolCall | AuxiliaryBlock) => {
  if ('status' in toolCall && toolCall?.status === 'done') {
    return '完成'
  }
  if ('status' in toolCall && toolCall?.status === 'waiting') {
    return '等待确认'
  }
  if ('status' in toolCall && toolCall?.status === 'waiting_external') {
    return '等待外部执行'
  }
  if ('status' in toolCall && toolCall?.status === 'error') {
    return '异常'
  }
  return '调用中'
}

const hasStreamSummary = (message: Message) => {
  return Boolean(
    message?.eventStages?.length ||
    streamUsageTokenText(message) ||
    streamUsageTimeText(message)
  )
}

const streamUsageTokenText = (message: Message) => {
  const usageToken = Number(message?.usageToken)
  if (!Number.isFinite(usageToken) || usageToken <= 0) {
    return ''
  }
  return Math.round(usageToken).toLocaleString()
}

const streamUsageTimeText = (message: Message) => {
  const usageTime = Number(message?.usageTime)
  if (!Number.isFinite(usageTime) || usageTime <= 0) {
    return ''
  }

  if (usageTime > 1000) {
    return formatDuration(usageTime)
  }
  if (usageTime < 1) {
    return `${Math.round(usageTime * 1000)}ms`
  }
  if (usageTime < 10) {
    return `${usageTime.toFixed(2)}s`
  }
  return `${usageTime.toFixed(1)}s`
}

const renderMarkdownText = (text = '') => {
  return markdownRenderer.render(String(text || ''))
}

// ==================== 滚动处理 ====================
const scrollToBottom = async (force = false) => {
  await nextTick()
  const container = messageListRef.value
  if (container && (force || autoScrollEnabled.value)) {
    container.scrollTop = container.scrollHeight
    autoScrollEnabled.value = true
  }
}

const scheduleScrollToBottom = (force = false) => {
  pendingScrollForce = pendingScrollForce || force

  if (scrollFrame) {
    return
  }

  scrollFrame = window.requestAnimationFrame(() => {
    scrollFrame = null
    const forceScroll = pendingScrollForce
    pendingScrollForce = false
    const container = messageListRef.value
    if (container && (forceScroll || autoScrollEnabled.value)) {
      container.scrollTop = container.scrollHeight
      autoScrollEnabled.value = true
    }
  })
}

const isNearBottom = (container: HTMLElement) => {
  return container.scrollHeight - container.scrollTop - container.clientHeight <= SCROLL_BOTTOM_THRESHOLD
}

const handleMessageScroll = () => {
  const container = messageListRef.value
  if (container) {
    autoScrollEnabled.value = isNearBottom(container)
  }
}

const handleSessionScroll = () => {
  const container = sessionListRef.value
  if (!container || sessionPaging.loading || sessionPaging.finished) {
    return
  }
  const distanceToBottom = container.scrollHeight - container.scrollTop - container.clientHeight
  if (distanceToBottom <= SCROLL_PAGE_THRESHOLD) {
    loadSessions(true)
  }
}

// ==================== 会话管理 ====================
const normalizeSessionId = (value: any) => {
  return value === undefined || value === null ? '' : String(value)
}

const createMessagePaging = (): MessagePaging => ({
  current: 0,
  size: MESSAGE_PAGE_SIZE,
  total: 0,
  loadedRecords: 0,
  loading: false,
  finished: false,
  initialized: false
})

const buildLocalSession = (backendSession: any, fallbackTitle = '新会话'): Session => {
  const sessionId = normalizeSessionId(backendSession?.id)
  if (!sessionId) {
    throw new Error('Backend session id is empty')
  }

  return {
    id: sessionId,
    backendSessionId: sessionId,
    title: backendSession?.title || fallbackTitle,
    messages: [],
    historyRecords: [],
    messagePaging: createMessagePaging(),
    updatedAt: backendSession?.lastMessageAt || backendSession?.updatedAt || backendSession?.createdAt || nowText()
  }
}

const loadSessions = async (append = false) => {
  if (!agentId.value || sessionPaging.loading) return
  if (!append && sessionPaging.finished) return

  sessionPaging.loading = true
  try {
    const params = {
      current: append ? sessionPaging.current + 1 : 1,
      size: SESSION_PAGE_SIZE,
      keyword: sessionKeyword.value || undefined,
      agentId: agentId.value
    }
    const data = await pageAgentSessions(params)
    const records = (data?.records || []).map((item: any) => buildLocalSession(item))

    if (append) {
      sessions.value.push(...records)
    } else {
      sessions.value = records
    }

    sessionPaging.current = params.current
    sessionPaging.total = Number(data?.total || 0)
    sessionPaging.finished = sessions.value.length >= sessionPaging.total

    if (!activeSessionId.value && sessions.value.length) {
      await selectSession(sessions.value[0].id)
    }
  } catch (error) {
    console.error('加载会话失败', error)
    ElMessage.error('加载会话失败')
  } finally {
    sessionPaging.loading = false
  }
}

const loadMessages = async (sessionId: string) => {
  const session = sessions.value.find((item) => item.id === sessionId)
  if (!session) return

  try {
    const data = await pageAgentMessages({
      current: 1,
      size: MESSAGE_PAGE_SIZE,
      sessionId
    })
    session.messages = (data?.records || []).map((item: any) => ({
      id: String(item.id),
      role: item.role || 'user',
      content: item.content || '',
      displayContent: item.content || '',
      contentSegments: item.content ? [{
        id: `content-${item.id}`,
        type: 'content' as const,
        content: item.content
      }] : [],
      createdAt: item.createdAt || item.createTime || '',
      auxiliaryBlocks: [],
      eventStages: [],
      streamTiming: null,
      streamStatus: 'done'
    }))
    session.hasMore = session.messages.length < (data?.total || 0)
  } catch (error) {
    console.error('加载消息失败', error)
  }
}

const selectSession = async (sessionId: string) => {
  activeSessionId.value = sessionId
  autoScrollEnabled.value = true
  await loadMessages(sessionId)
  nextTick(() => scrollToBottom(true))
}

const createSession = async () => {
  if (!agentId.value) return

  sessionCreating.value = true
  try {
    const data = await createAgentSession({
      agentId: agentId.value,
      title: '新会话'
    })
    const newSession = buildLocalSession(data, '新会话')
    sessions.value.unshift(newSession)
    activeSessionId.value = newSession.id
    autoScrollEnabled.value = true
    await scrollToBottom(true)
  } catch (error) {
    console.error('创建会话失败', error)
    ElMessage.error('创建会话失败')
  } finally {
    sessionCreating.value = false
  }
}

const removeSession = async (session: Session) => {
  try {
    await ElMessageBox.confirm(`确定删除会话"${session.title}"吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  try {
    await deleteAgentSession(session.backendSessionId || session.id)
    const index = sessions.value.findIndex((item) => item.id === session.id)
    if (index >= 0) {
      sessions.value.splice(index, 1)
      if (activeSessionId.value === session.id) {
        activeSessionId.value = sessions.value.length ? sessions.value[0].id : ''
        if (activeSessionId.value) {
          await loadMessages(activeSessionId.value)
        }
      }
    }
    ElMessage.success('会话已删除')
  } catch (error) {
    console.error('删除会话失败', error)
    ElMessage.error('删除会话失败')
  }
}

const clearSessions = async () => {
  if (!sessions.value.length) {
    return
  }

  try {
    await ElMessageBox.confirm('确认清空所有历史对话吗？', '清空确认', {
      type: 'warning',
      confirmButtonText: '清空',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  const loadedSessions = [...sessions.value]
  await Promise.all(loadedSessions.map((session) => deleteAgentSession(session.backendSessionId || session.id)))
  sessions.value = []
  activeSessionId.value = ''
  sessionKeyword.value = ''
  await loadSessions()
}

// ==================== 消息发送 ====================
const appendMessage = (role: 'user' | 'assistant', content: string, extra: Partial<Message> = {}, options: { forceScroll?: boolean } = {}) => {
  const session = activeSession.value
  if (!session) {
    throw new Error('Active session is empty')
  }
  const message: Message = {
    id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`,
    role,
    content,
    displayContent: content,
    contentSegments: content ? [{
      id: createContentSegmentId(),
      type: 'content',
      content
    }] : [],
    createdAt: nowText(),
    ...extra
  }

  session.messages.push(message)
  session.updatedAt = nowText()

  if (role === 'user' && session.messages.length <= 2) {
    session.title = content.slice(0, 18) || '新会话'
  }

  scrollToBottom(Boolean(options.forceScroll))
  return session.messages[session.messages.length - 1]
}

const stopStream = () => {
  abortController.value?.abort()
  abortController.value = null
  streaming.value = false
}

const sendMessage = async () => {
  if (!canSend.value || !activeSession.value) return

  const content = inputMessage.value.trim()
  inputMessage.value = ''
  autoScrollEnabled.value = true

  appendMessage('user', content, {}, { forceScroll: true })
  const assistantMessage = appendMessage(
    'assistant',
    '',
    {
      pending: true,
      typing: true,
      eventStages: [],
      streamTiming: null,
      streamStatus: 'running',
      usageToken: 0,
      usageTime: 0,
      thinkingContent: '',
      thinkingExpanded: true,
      toolResultContent: '',
      toolResultExpanded: true,
      toolCalls: [],
      auxiliaryBlocks: [],
      activeReasoningBlockId: null,
      activeToolBlockId: null,
      activeToolBlockIds: {},
      activeSubAgentBlockIds: {},
      pendingReasoningStartedAt: null,
      toolExpanded: true,
      pendingIntervention: null,
      planState: null,
      planTasks: [],
      planProgress: buildPlanProgress([]),
      planExpanded: true,
      lastPlanEvent: null,
      runId: null
    },
    { forceScroll: true }
  )

  streaming.value = true
  abortController.value = new AbortController()

  try {
    await chatStream(
      {
        agentId: agentId.value,
        sessionId: activeSessionId.value,
        content: content
      },
      {
        onEvent: (streamEvent: any) => {
          if (streamEvent.runId) {
            assistantMessage.runId = streamEvent.runId
          }
          recordStreamEvent(assistantMessage, streamEvent)
          appendAuxiliaryDelta(assistantMessage, activeSession.value, streamEvent)
        },
        onMessage: (delta: string) => {
          appendStreamText(assistantMessage, activeSession.value, delta)
        },
        onError: (error: string) => {
          finishStreamEvents(assistantMessage, 'error')
          assistantMessage.content = error
          assistantMessage.displayContent = error
          assistantMessage.pending = false
          assistantMessage.typing = false
          assistantMessage.error = true
          if (activeSession.value) {
            activeSession.value.updatedAt = nowText()
          }
          scrollToBottom()
        },
        onDone: (streamEvent: any) => {
          const usageToken = Number(streamEvent?.usageToken ?? streamEvent?.data?.usageToken)
          const usageTime = Number(streamEvent?.usageTime ?? streamEvent?.data?.usageTime)

          if (Number.isFinite(usageToken) && usageToken > 0) {
            assistantMessage.usageToken = usageToken
          }
          if (Number.isFinite(usageTime) && usageTime > 0) {
            assistantMessage.usageTime = usageTime
          }

          finishStreamEvents(assistantMessage, 'done')
          if (activeSession.value) {
            activeSession.value.updatedAt = nowText()
          }
        }
      },
      {
        signal: abortController.value.signal
      }
    )

    assistantMessage.pending = false
    assistantMessage.typing = false
    assistantMessage.displayContent = assistantMessage.content
    if (activeSession.value) {
      activeSession.value.updatedAt = nowText()
    }
  } catch (error: any) {
    finishStreamEvents(assistantMessage, error.name === 'AbortError' ? 'done' : 'error')
    if (error.name === 'AbortError') {
      if (!assistantMessage.content) {
        assistantMessage.content = '已停止生成'
        assistantMessage.displayContent = assistantMessage.content
        appendContentSegment(assistantMessage, assistantMessage.content)
      } else {
        assistantMessage.displayContent = assistantMessage.displayContent || assistantMessage.content
      }
    } else {
      assistantMessage.content = error.message || '对话请求失败'
      assistantMessage.displayContent = assistantMessage.content
      assistantMessage.error = true
    }
    assistantMessage.pending = false
    assistantMessage.typing = false
  } finally {
    streaming.value = false
    abortController.value = null
    scrollToBottom()
  }
}

const togglePlanDrawer = () => {
  planDrawerOpen.value = !planDrawerOpen.value
}

const toggleAuxiliaryPanel = (message: Message, key: string | AuxiliaryBlock) => {
  if (!message) {
    return
  }

  if (typeof key === 'object' && key) {
    key.expanded = !key.expanded
    key.expandedByUser = true
    return
  }

  if (key === 'thinking') {
    message.thinkingExpanded = !message.thinkingExpanded
  }
  if (key === 'tool') {
    message.toolExpanded = !message.toolExpanded
  }
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const searchSessions = () => {
  if (sessionSearchTimer) {
    clearTimeout(sessionSearchTimer)
  }
  sessionSearchTimer = setTimeout(() => {
    sessionPaging.current = 0
    sessionPaging.finished = false
    loadSessions()
  }, 300)
}

// ==================== 生命周期 ====================
watch(
  [() => route.params.agentId, () => route.query.sessionId],
  async () => {
    stopStream()
    planDrawerOpen.value = false
    agentInfo.id = agentId.value
    agentInfo.name = String(route.query.agentName || '')
    agentInfo.key = String(route.query.agentKey || '')
    sessions.value = []
    activeSessionId.value = ''
    sessionPaging.current = 0
    sessionPaging.finished = false
    await loadSessions()
  },
  { immediate: true }
)

watch(
  sessionKeyword,
  () => {
    searchSessions()
  }
)

onBeforeUnmount(() => {
  stopStream()

  if (sessionSearchTimer) {
    clearTimeout(sessionSearchTimer)
    sessionSearchTimer = null
  }

  if (scrollFrame) {
    window.cancelAnimationFrame(scrollFrame)
    scrollFrame = null
  }
  pendingScrollForce = false
})
</script>

<template>
  <div class="agent-chat-page" :class="{ 'is-plan-drawer-open': planDrawerOpen }">
    <!-- 左侧会话面板 -->
    <aside class="chat-session-panel">
      <el-button
        class="new-session-button"
        type="primary"
        plain
        :icon="Plus"
        :loading="sessionCreating"
        :disabled="sessionCreating"
        @click="createSession"
      >
        新建对话
      </el-button>

      <el-input
        v-model="sessionKeyword"
        class="session-search"
        :prefix-icon="Search"
        clearable
        placeholder="搜索历史对话"
      />

      <div
        ref="sessionListRef"
        class="session-list"
        @scroll="handleSessionScroll"
      >
        <div class="session-group-title">
          {{ sessionKeyword ? '搜索结果' : '今天' }}
        </div>
        <button
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: session.id === activeSessionId }"
          type="button"
          @click="selectSession(session.id)"
        >
          <el-icon class="session-chat-icon"><ChatLineRound /></el-icon>
          <span class="session-main">
            <strong>{{ session.title }}</strong>
            <small>{{ session.updatedAt }}</small>
          </span>
          <el-tooltip content="删除会话" placement="right">
            <el-button
              class="session-delete"
              :icon="MoreFilled"
              link
              @click.stop="removeSession(session)"
            />
          </el-tooltip>
        </button>
        <div
          v-if="!sessions.length"
          class="session-empty"
        >
          {{ sessionPaging.loading ? '正在加载会话...' : '未找到相关对话' }}
        </div>
        <div
          v-else-if="sessionPaging.loading || !sessionPaging.finished"
          class="session-empty session-page-state"
        >
          {{ sessionPaging.loading ? '正在加载更多...' : '向下滚动加载更多' }}
        </div>
      </div>

      <button
        class="clear-session-button"
        type="button"
        @click="clearSessions"
      >
        <el-icon><Delete /></el-icon>
        <span>清空历史记录</span>
      </button>
    </aside>

    <!-- 中间聊天窗口 -->
    <section class="chat-window">
      <header class="chat-header">
        <div class="chat-agent-title">
          <h2>{{ agentInfo.name || '智能体对话' }}</h2>
          <el-tag class="agent-state-tag" :type="streaming ? 'warning' : 'success'">
            {{ streaming ? '生成中' : '就绪' }}
          </el-tag>
        </div>
        <div class="chat-header-actions">
          <button
            class="plan-drawer-toggle"
            type="button"
            :aria-expanded="planDrawerOpen"
            @click="togglePlanDrawer"
          >
            <span>{{ planDrawerOpen ? '收起工作区 >' : '< 工作区' }}</span>
          </button>
        </div>
      </header>

      <div
        ref="messageListRef"
        class="message-list"
        @scroll="handleMessageScroll"
      >
        <div
          v-for="message in activeMessages"
          :key="message.id"
          class="message-row"
          :class="[message.role, { error: message.error, typing: message.typing }]"
        >
          <div class="message-bubble">
            <template v-if="message.role === 'assistant'">
              <template
                v-for="item in assistantMessageItems(message)"
                :key="item.id"
              >
                <div
                  v-if="item.type === 'content'"
                  class="message-content markdown-content assistant-content-segment"
                  :class="{ 'is-last-content': item.isLastContent }"
                >
                  <div
                    class="markdown-rendered"
                    v-html="renderMarkdownText(item.content)"
                  />
                </div>
                <section
                  v-else-if="item.block"
                  class="auxiliary-panel"
                >
                  <button
                    class="auxiliary-toggle"
                    type="button"
                    @click="toggleAuxiliaryPanel(message, item.block)"
                  >
                    <span class="auxiliary-title">{{ auxiliaryBlockTitle(item.block) }}</span>
                    <small class="auxiliary-action">{{ item.block.expanded ? '收起' : '展开' }}</small>
                    <small v-if="auxiliaryBlockTimeText(item.block)" class="auxiliary-time">耗时 {{ auxiliaryBlockTimeText(item.block) }}</small>
                  </button>
                  <div
                    v-show="item.block.expanded"
                    class="auxiliary-content"
                  >
                    <div v-if="item.block.kind === 'reasoning'">
                      {{ item.block.content }}
                    </div>
                    <div
                      v-else-if="item.block.kind === 'tool'"
                      class="tool-call"
                    >
                      <div class="tool-call-header">
                        <span class="tool-call-name">{{ item.block.toolCall?.name || '工具调用' }}</span>
                        <small>{{ toolStatusText(item.block.toolCall || item.block) }}</small>
                      </div>
                      <div
                        v-if="item.block.toolCall?.process"
                        class="tool-call-section"
                      >
                        <span class="tool-call-section-title">调用过程</span>
                        <div class="tool-call-text">{{ item.block.toolCall.process }}</div>
                      </div>
                      <div
                        v-if="item.block.toolCall?.result"
                        class="tool-call-section"
                      >
                        <span class="tool-call-section-title">调用结果</span>
                        <div class="tool-call-text">{{ item.block.toolCall.result }}</div>
                      </div>
                    </div>
                    <div
                      v-else-if="item.block.kind === 'subagent'"
                      class="subagent-call"
                    >
                      <div class="subagent-call-header">
                        <span class="subagent-call-name">{{ item.block.subAgent?.name || item.block.subAgentName || '子智能体' }}</span>
                        <small>{{ item.block.status === 'done' ? '完成' : '输出中' }}</small>
                      </div>
                      <div
                        v-if="item.block.content"
                        class="subagent-call-content markdown-rendered"
                        v-html="renderMarkdownText(item.block.content)"
                      />
                    </div>
                  </div>
                </section>
              </template>
            </template>
            <div
              v-else-if="message.content || message.displayContent"
              class="message-content"
            >
              {{ message.content || message.displayContent }}
            </div>
            <div
              v-if="message.role === 'assistant' && hasStreamSummary(message)"
              class="stream-stages"
            >
              <div class="stream-stages-header">
                <span v-if="streamUsageTokenText(message)">耗时统计</span>
                <span v-if="streamUsageTokenText(message)">Token消耗： {{ streamUsageTokenText(message) }}</span>
                <span v-if="streamUsageTimeText(message)">模型耗时 {{ streamUsageTimeText(message) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-if="!activeMessages.length" class="empty-chat">
          {{ activeSession?.messagePaging?.loading ? '正在加载消息...' : '暂无消息' }}
        </div>
      </div>

      <el-button
        v-if="!autoScrollEnabled"
        class="back-bottom-button"
        :icon="Bottom"
        circle
        type="primary"
        @click="scrollToBottom(true)"
      />

      <footer class="chat-input-panel">
        <div class="chat-input-shortcuts">
          <el-button size="small">使用工具</el-button>
        </div>
        <el-input
          v-model="inputMessage"
          class="chat-input"
          type="textarea"
          :autosize="{ minRows: 1, maxRows: 3 }"
          resize="none"
          :placeholder="`给${agentInfo.name || '智能体'}发送消息，输入 / 使用快捷指令...`"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <div class="chat-actions">
          <el-button
            v-if="streaming"
            :icon="Close"
            @click="stopStream"
          >
            停止
          </el-button>
          <el-button
            type="primary"
            :icon="Promotion"
            :disabled="!canSend"
            @click="sendMessage"
          >
            发送
          </el-button>
        </div>
      </footer>
    </section>

    <!-- 右侧执行计划面板 -->
    <aside
      class="execution-plan-panel"
      :class="{ open: planDrawerOpen }"
      :aria-hidden="!planDrawerOpen"
    >
      <header class="execution-plan-header">
        <div class="execution-plan-title">
          <h3>执行计划</h3>
          <p v-if="activePlanMessage?.planState?.title || activePlanMessage?.planState?.planNo">
            {{ planPanelTitle(activePlanMessage) }}
          </p>
        </div>
        <div class="execution-plan-actions">
          <el-tag
            size="small"
            :type="planStatusType(activePlanMessage?.planState?.status || '')"
          >
            {{ planStatusText(activePlanMessage?.planState?.status || '') }}
          </el-tag>
        </div>
      </header>

      <div class="execution-plan-meta">
        <span>本次任务</span>
        <span v-if="planProgressText(activePlanMessage)">进度 {{ planProgressText(activePlanMessage) }}</span>
      </div>

      <div
        v-if="executionPlanProgress.total"
        class="execution-plan-progress"
      >
        <div class="execution-progress-bar">
          <span :style="{ width: `${executionPlanProgress.percent || 0}%` }" />
        </div>
        <small>{{ executionPlanProgress.completed }}/{{ executionPlanProgress.total }} 完成</small>
      </div>

      <ol
        v-if="activePlanMessage?.planTasks?.length"
        class="execution-task-list"
      >
        <li
          v-for="(task, taskIndex) in activePlanMessage.planTasks"
          :key="task.id"
          class="execution-task-item"
          :class="executionTaskStateClass(task.state)"
        >
          <div class="execution-task-rail">
            <span class="execution-task-index">{{ task.taskIndex || taskIndex + 1 }}</span>
          </div>
          <div class="execution-task-content">
            <div class="execution-task-head">
              <strong>{{ task.subject }}</strong>
              <span class="execution-task-state">{{ taskStateText(task.state) }}</span>
            </div>
            <p v-if="task.detail">{{ task.detail }}</p>
          </div>
        </li>
      </ol>
      <div
        v-else
        class="execution-task-empty"
      >
        <strong>{{ activePlanMessage ? '计划生成中' : '暂无执行计划' }}</strong>
        <span>{{ activePlanMessage?.lastPlanEvent?.occurredAt || (activePlanMessage ? '正在等待任务列表' : 'Agent 生成计划后会在这里更新') }}</span>
      </div>
    </aside>
  </div>
</template>

<style scoped>
.agent-chat-page {
  display: grid;
  position: relative;
  height: calc(100vh - 120px);
  min-height: 560px;
  grid-template-columns: 280px minmax(0, 1fr) 0;
  padding: 16px;
  gap: 16px;
  overflow: hidden;
  transition: grid-template-columns 180ms ease;
  background: #f5f7fa;
}

.agent-chat-page.is-plan-drawer-open {
  grid-template-columns: 230px minmax(0, 1fr) 230px;
}

.chat-session-panel,
.chat-window,
.execution-plan-panel {
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
}

.chat-session-panel {
  display: flex;
  flex-direction: column;
  padding: 20px 12px 14px;
}

.new-session-button {
  width: 100%;
  height: 35px;
  margin-bottom: 14px;
  border-radius: 8px;
  font-weight: 760;
}

.session-search {
  margin-bottom: 20px;
}

.session-search :deep(.el-input__wrapper) {
  min-height: 35px;
  border-radius: 8px;
}

.session-group-title {
  margin: 0 0 10px;
  color: #74839b;
  font-size: 13px;
  font-weight: 760;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 0 12px;
}

.session-item {
  display: grid;
  width: 100%;
  min-height: 52px;
  align-items: center;
  grid-template-columns: 24px minmax(0, 1fr) 28px;
  gap: 10px;
  margin: 0 0 8px;
  padding: 10px 8px;
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  background: transparent;
  text-align: left;
}

.session-item:hover {
  background: #f3f7ff;
}

.session-item.active {
  border-color: #d8e6ff;
  background: #eaf2ff;
}

.session-chat-icon {
  color: #1d6ff2;
  font-size: 18px;
}

.session-main {
  min-width: 0;
}

.session-main strong,
.session-main small {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-main strong {
  color: #15243d;
  font-size: 14px;
  font-weight: 760;
}

.session-main small {
  margin-top: 5px;
  color: #7d8aa0;
  font-size: 12px;
}

.session-delete {
  flex: 0 0 auto;
  color: #5f6f89;
}

.session-empty {
  display: grid;
  min-height: 80px;
  place-items: center;
  color: #94a3b8;
  font-size: 13px;
}

.session-page-state {
  min-height: 36px;
  padding-bottom: 8px;
}

.clear-session-button {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  gap: 8px;
  border: 0;
  cursor: pointer;
  background: transparent;
  color: #1d6ff2;
  font: inherit;
  font-size: 14px;
  font-weight: 760;
}

.clear-session-button:hover {
  color: #1257c8;
}

.chat-window {
  display: flex;
  position: relative;
  flex-direction: column;
}

.chat-header {
  display: flex;
  height: 40px;
  min-height: 40px;
  flex: 0 0 40px;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  box-sizing: border-box;
  padding: 0 20px;
  border-bottom: 1px solid var(--border);
  background: linear-gradient(180deg, rgba(247, 251, 255, 0.96), rgba(255, 255, 255, 0.9));
}

.chat-header-actions {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: flex-end;
}

.chat-agent-title {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 20px;
}

.chat-agent-title h2 {
  margin: 0;
  color: #0f1f3a;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 0;
  line-height: 22px;
}

.agent-state-tag {
  flex: 0 0 auto;
  height: 24px;
  margin: 0;
  border-radius: 6px;
  font-weight: 700;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 28px 34px 22px;
  background: linear-gradient(180deg, #f7fbff 0%, #ffffff 38%, #f8fbff 100%);
}

.back-bottom-button {
  border: 1px solid #a0cfff;
  background: #a0cfff;
  position: absolute;
  right: 24px;
  bottom: 112px;
  z-index: 3;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.18);
}

.message-row {
  display: flex;
  margin-bottom: 18px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.assistant {
  justify-content: flex-start;
}

.message-row.user + .message-row.assistant,
.message-row.assistant + .message-row.user {
  padding-top: 26px;
}

.message-bubble {
  max-width: min(860px, 88%);
  border: 0;
  padding: 0;
  background: transparent;
  box-shadow: none;
}

.message-row.assistant .message-bubble {
  display: grid;
  gap: 8px;
  max-width: min(920px, 100%);
}

.message-row.user .message-bubble {
  max-width: min(680px, 76%);
  color: #17233c;
}

.message-row.error .message-bubble {
  color: var(--danger);
}

.message-content {
  margin: 40px 0px 40px 0px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
}

.message-row.user .message-content {
  padding: 12px 18px;
  border: 1px solid #e5eaf3;
  border-radius: 20px;
  background: #f3f3f3;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.message-row.assistant .message-content {
  color: #17233c;
  font-size: 15px;
}

.assistant-content-segment {
  min-width: 0;
}

.message-content.markdown-content {
  white-space: normal;
}

.markdown-rendered :deep(> :first-child) {
  margin-top: 0;
}

.markdown-rendered :deep(> :last-child) {
  margin-bottom: 0;
}

.markdown-rendered :deep(p) {
  margin: 0 0 10px;
}

.markdown-rendered :deep(h1),
.markdown-rendered :deep(h2),
.markdown-rendered :deep(h3),
.markdown-rendered :deep(h4),
.markdown-rendered :deep(h5),
.markdown-rendered :deep(h6) {
  margin: 14px 0 8px;
  color: var(--ink);
  font-weight: 760;
  line-height: 1.35;
}

.markdown-rendered :deep(h1) {
  font-size: 22px;
}

.markdown-rendered :deep(h2) {
  font-size: 19px;
}

.markdown-rendered :deep(h3) {
  font-size: 17px;
}

.markdown-rendered :deep(ul),
.markdown-rendered :deep(ol) {
  margin: 8px 0 10px;
  padding-left: 22px;
}

.markdown-rendered :deep(li + li) {
  margin-top: 4px;
}

.markdown-rendered :deep(blockquote) {
  margin: 10px 0;
  padding-left: 12px;
  border-left: 3px solid var(--border-strong);
  color: var(--muted);
}

.markdown-rendered :deep(a) {
  color: var(--primary);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.markdown-rendered :deep(code) {
  border-radius: 4px;
  padding: 2px 5px;
  background: rgba(15, 23, 42, 0.08);
  color: #0f172a;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
  font-size: 0.92em;
}

.markdown-rendered :deep(pre) {
  overflow-x: auto;
  margin: 10px 0;
  padding: 12px;
  border-radius: 8px;
  background: #0f172a;
  color: #e5e7eb;
  line-height: 1.6;
}

.markdown-rendered :deep(pre code) {
  padding: 0;
  background: transparent;
  color: inherit;
  font-size: 13px;
}

.auxiliary-panel {
  min-width: 0;
  margin: 5px 0px 5px 0px;
}

.auxiliary-toggle {
  display: inline-flex;
  width: auto;
  max-width: 100%;
  min-height: 22px;
  align-items: center;
  padding: 0;
  border: 0;
  cursor: pointer;
  background: transparent;
  color: #64748b;
  text-align: left;
}

.auxiliary-toggle:hover {
  color: #2563eb;
}

.auxiliary-title {
  min-width: 0;
  overflow: hidden;
  font-size: 13px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.auxiliary-toggle small {
  color: #64748b;
  font-size: 12px;
  white-space: nowrap;
}

.auxiliary-action {
  margin-left: 20px;
}

.auxiliary-time {
  margin-left: 12px;
}

.auxiliary-content {
  margin-top: 20px;
  margin-left: 20px;
  padding: 2px 0 4px;
  color: #8b9bb0;
  font-size: 12px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.tool-call {
  display: grid;
  gap: 6px;
  color: #8b9bb0;
  font-size: 12px;
  white-space: normal;
}

.tool-call-header {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  color: #8b9bb0;
  white-space: normal;
}

.tool-call-name {
  min-width: 0;
  overflow-wrap: anywhere;
  font-weight: 700;
}

.tool-call-header small {
  flex: 0 0 auto;
  color: #8b9bb0;
  font-size: 12px;
}

.tool-call-section {
  display: grid;
  gap: 4px;
}

.tool-call-section-title {
  color: #8b9bb0;
  font-size: 12px;
  font-weight: 700;
}

.tool-call-text {
  color: #8b9bb0;
  font-size: 12px;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.subagent-call {
  display: grid;
  gap: 8px;
  white-space: normal;
}

.subagent-call-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #334155;
}

.subagent-call-name {
  min-width: 0;
  overflow-wrap: anywhere;
  font-weight: 760;
}

.subagent-call-header small {
  flex: 0 0 auto;
  color: #64748b;
  font-size: 12px;
}

.subagent-call-content {
  color: #374151;
  overflow-wrap: anywhere;
}

.stream-stages {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed var(--border);
}

.stream-stages-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-start;
  gap: 30px;
  margin-top: 10px;
  color: var(--subtle);
  font-size: 12px;
}

.stream-stages-header span {
  white-space: nowrap;
}

.stream-stages-header span:first-child {
  color: var(--subtle);
  font-weight: 700;
}

.plan-drawer-toggle {
  display: inline-flex;
  height: 34px;
  align-items: center;
  gap: 6px;
  border: 1px solid #cfe0f8;
  border-radius: 999px;
  padding: 0 10px;
  cursor: pointer;
  color: #ffffff;
  background: #1d6ff2;
  box-shadow: 0 10px 24px rgba(42, 87, 143, 0.12);
  font: inherit;
  font-size: 13px;
  font-weight: 800;
  transition: background 180ms ease, color 180ms ease;
}

.plan-drawer-toggle:hover,
.agent-chat-page.is-plan-drawer-open .plan-drawer-toggle {
  color: #ffffff;
  background: #a0cfff;
}

.execution-plan-panel {
  display: flex;
  position: relative;
  z-index: 9;
  grid-column: 3;
  grid-row: 1;
  flex-direction: column;
  width: 230px;
  height: 100%;
  padding: 16px 12px;
  overflow-y: auto;
  opacity: 0;
  transform: translateX(calc(100% + 5px));
  transition: transform 180ms ease, opacity 180ms ease;
  pointer-events: none;
}

.execution-plan-panel.open {
  opacity: 1;
  transform: translateX(0);
  pointer-events: auto;
}

.execution-plan-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
}

.execution-plan-title {
  min-width: 0;
}

.execution-plan-header h3 {
  margin: 0;
  color: #0f1f3a;
  font-size: 17px;
  font-weight: 820;
  letter-spacing: 0;
}

.execution-plan-header p {
  margin: 5px 0 0;
  overflow: hidden;
  color: #6c7890;
  font-size: 12px;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.execution-plan-actions {
  display: inline-flex;
  align-items: center;
  flex: 0 0 auto;
}

.execution-plan-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 5px 8px;
  margin-bottom: 10px;
  color: #1f2f49;
  font-size: 12px;
  font-weight: 760;
}

.execution-plan-meta span + span::before {
  margin-right: 6px;
  color: #8da0ba;
  content: '·';
}

.execution-plan-progress {
  display: grid;
  grid-template-columns: minmax(0, 1fr) max-content;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.execution-progress-bar {
  overflow: hidden;
  height: 6px;
  border-radius: 999px;
  background: #e5eef9;
}

.execution-progress-bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #1d6ff2, #5aa2ff);
  transition: width 180ms ease;
}

.execution-plan-progress small {
  color: #64748b;
  font-size: 11px;
  white-space: nowrap;
}

.execution-task-list {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.execution-task-item {
  display: grid;
  position: relative;
  min-height: 0;
  grid-template-columns: 30px minmax(0, 1fr);
  gap: 8px;
  padding: 0;
}

.execution-task-item:last-child {
  min-height: 0;
  padding-bottom: 0;
}

.execution-task-rail {
  display: flex;
  position: relative;
  justify-content: center;
}

.execution-task-item:not(:last-child) .execution-task-rail::after {
  position: absolute;
  top: 30px;
  bottom: -10px;
  left: 50%;
  border-left: 2px dashed #d9e5f5;
  content: '';
  transform: translateX(-50%);
}

.execution-task-index {
  display: grid;
  position: relative;
  z-index: 1;
  width: 28px;
  height: 28px;
  place-items: center;
  border-radius: 50%;
  background: #eaf4ff;
  color: #1d6ff2;
  font-size: 13px;
  font-weight: 800;
}

.execution-task-content {
  min-width: 0;
  padding: 2px 0 10px;
}

.execution-task-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) max-content;
  align-items: start;
  gap: 6px;
}

.execution-task-content strong {
  display: block;
  color: #1d2c45;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.45;
  overflow-wrap: anywhere;
}

.execution-task-state {
  border-radius: 999px;
  padding: 2px 6px;
  color: #1d6ff2;
  background: #eef5ff;
  font-size: 11px;
  font-weight: 760;
  white-space: nowrap;
}

.execution-task-content p {
  display: -webkit-box;
  overflow: hidden;
  margin: 5px 0 0;
  color: #79869b;
  font-size: 12px;
  line-height: 1.55;
  overflow-wrap: anywhere;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.execution-task-item.in_progress .execution-task-index {
  background: #fff4db;
  color: #d98909;
}

.execution-task-item.in_progress .execution-task-state {
  color: #b77904;
  background: #fff7df;
}

.execution-task-item.completed .execution-task-state {
  color: #159252;
  background: #eafaf1;
}

.execution-task-item.failed .execution-task-index,
.execution-task-item.blocked .execution-task-index {
  background: #fff0f2;
  color: #e5485d;
}

.execution-task-item.failed .execution-task-state,
.execution-task-item.blocked .execution-task-state {
  color: #d92d45;
  background: #fff0f2;
}

.execution-task-item.cancelled .execution-task-index {
  background: #eef2f7;
  color: #64748b;
}

.execution-task-item.cancelled .execution-task-state {
  color: #64748b;
  background: #eef2f7;
}

.execution-task-empty {
  display: flex;
  min-height: 92px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px dashed #d9e5f5;
  border-radius: 10px;
  background: #f8fbff;
  color: #8b9ab0;
  font-size: 12px;
  text-align: center;
}

.execution-task-empty strong {
  color: #1d2c45;
  font-size: 13px;
}

.message-row.assistant.typing .assistant-content-segment.is-last-content::after {
  display: inline-block;
  width: 15px;
  height: 15px;
  margin-left: 5px;
  border: 2px solid rgba(139, 155, 176, 0.28);
  border-top-color: #8b9bb0;
  border-radius: 50%;
  animation: cursor-spin 0.8s linear infinite;
  content: '';
  vertical-align: -1px;
}

@keyframes cursor-spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-chat {
  display: grid;
  height: 100%;
  place-items: center;
  color: var(--subtle);
}

.chat-input-panel {
  display: grid;
  grid-template-areas:
    "shortcuts shortcuts"
    "input actions";
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: 8px 12px;
  margin: 0 32px 8px;
  padding: 10px 12px;
  border: 1px solid #d8e4f5;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.06);
}

.chat-input-shortcuts {
  display: flex;
  grid-area: shortcuts;
  gap: 8px;
}

.chat-input {
  grid-area: input;
}

.chat-input :deep(.el-textarea__inner) {
  min-height: 38px !important;
  max-height: 96px !important;
  padding: 8px 0;
  border: 0;
  box-shadow: none;
  color: #17233c;
  font-size: 14px;
  line-height: 22px;
}

.chat-actions {
  display: flex;
  grid-area: actions;
  align-items: center;
  gap: 8px;
}

.chat-actions .el-button {
  min-width: 76px;
  height: 38px;
  border-radius: 999px;
  font-weight: 760;
}

@media (max-width: 960px) {
  .agent-chat-page,
  .agent-chat-page.is-plan-drawer-open {
    height: auto;
    min-height: 0;
    grid-template-columns: 1fr;
  }

  .chat-session-panel {
    min-height: 220px;
  }

  .chat-window {
    min-height: 560px;
  }

  .execution-plan-panel {
    position: absolute;
    top: 5px;
    right: 5px;
    bottom: 5px;
    grid-column: auto;
    width: min(280px, calc(100% - 20px));
    height: auto;
    min-height: 360px;
  }

  .chat-input-panel {
    grid-template-areas:
      "shortcuts"
      "input actions";
    grid-template-columns: minmax(0, 1fr) auto;
    margin: 0 16px 8px;
  }

  .chat-actions {
    justify-content: flex-end;
  }
}
</style>