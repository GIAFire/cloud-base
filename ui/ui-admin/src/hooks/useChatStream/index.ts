import { computed, nextTick, onBeforeUnmount, reactive, ref, watch, type Ref } from 'vue'
import MarkdownIt from 'markdown-it'
import {
  chatStream,
  createAgentSession,
  deleteAgentSession,
  pageAgentMessages,
  pageAgentSessions,
  userConfirmStream
} from '@/api/agent/chat'
import type { StreamEventVO, AgentSessionVO, AgentMessageVO } from '@/api/agent/chat/types'

// ==================== 类型定义 ====================

/** 会话信息 */
export interface SessionVO {
  id: string
  backendSessionId: string
  title: string
  updatedAt: string
  messages: MessageVO[]
  historyRecords: HistoryRecordVO[]
  messagePaging: MessagePaging
}

/** 消息信息 */
export interface MessageVO {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  displayContent: string
  contentSegments: ContentSegment[]
  createdAt: string
  pending?: boolean
  typing?: boolean
  error?: boolean
  runId?: string
  // 流式相关
  eventStages?: EventStage[]
  streamTiming?: StreamTiming | null
  streamStatus?: 'running' | 'done' | 'error' | 'waiting'
  usageToken?: number
  usageTime?: number
  // 推理相关
  thinkingContent?: string
  thinkingExpanded?: boolean
  toolResultContent?: string
  toolResultExpanded?: boolean
  toolCalls?: ToolCall[]
  auxiliaryBlocks?: AuxiliaryBlock[]
  activeReasoningBlockId?: string | null
  activeToolBlockId?: string | null
  activeToolBlockIds?: Record<string, string>
  activeSubAgentBlockIds?: Record<string, string>
  pendingReasoningStartedAt?: number | null
  toolExpanded?: boolean
  pendingIntervention?: Intervention | null
  // 计划相关
  planState?: PlanState | null
  planTasks?: PlanTask[]
  planProgress?: PlanProgress
  planExpanded?: boolean
  lastPlanEvent?: LastPlanEvent | null
  // 历史消息相关
  dbId?: string
  seqStart?: number
  seqEnd?: number
  historyGroupKey?: string
  fromHistory?: boolean
  messageType?: string
  messageStatus?: string
}

/** 内容片段 */
export interface ContentSegment {
  id: string
  type: 'content' | 'auxiliary'
  content?: string
  blockId?: string
  isLastContent?: boolean
}

/** 辅助块 */
export interface AuxiliaryBlock {
  id: string
  kind: 'reasoning' | 'tool' | 'subagent'
  sequence: number
  title: string
  expanded: boolean
  expandedByUser: boolean
  status: 'running' | 'done' | 'error' | 'waiting' | 'waiting_external'
  startedAt: number
  endedAt: number | null
  durationMs: number
  content: string
  toolCall?: ToolCall | null
  subAgent?: SubAgent | null
  subAgentName?: string
  sourcePath?: string
}

/** 工具调用 */
export interface ToolCall {
  id: string
  name: string
  process: string
  result: string
  rawInput: string
  rawResult: string
  status: 'running' | 'done' | 'error' | 'waiting' | 'waiting_external'
  argumentStarted: boolean
  resultStarted: boolean
  startedAt: number
  updatedAt: number
  pendingToolCall?: NormalizedToolCallPayload
}

/** 子智能体 */
export interface SubAgent {
  name: string
  sourcePath: string
  status: 'running' | 'done' | 'error'
}

/** 事件阶段 */
export interface EventStage {
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
  status: 'running' | 'done' | 'error' | 'waiting'
}

/** 流式计时 */
export interface StreamTiming {
  startedAt: number
  lastAt: number
  totalMs: number
  eventCount: number
}

/** 干预请求 */
export interface Intervention {
  type: 'userConfirm' | 'externalExecution'
  replyId: string
  runId: string
  toolCalls: NormalizedToolCallPayload[]
}

/** 标准化工具调用负载 */
export interface NormalizedToolCallPayload {
  id: string
  name: string
  input: Record<string, any>
  content: string
  metadata: Record<string, any>
  state: string
}

/** 计划状态 */
export interface PlanState {
  title?: string
  planNo?: string
  status?: string
  [key: string]: any
}

/** 计划任务 */
export interface PlanTask {
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

/** 计划进度 */
export interface PlanProgress {
  total: number
  completed: number
  inProgress: number
  pending: number
  percent: number
}

/** 最后计划事件 */
export interface LastPlanEvent {
  type: string
  toolName: string
  toolCallId?: string
  occurredAt?: string
}

/** 历史记录 */
export interface HistoryRecordVO {
  id: string
  sessionId: string
  runId?: string
  role: string
  messageType: string
  messageStatus?: string
  textContent?: string
  errorMessage?: string
  contentJson?: string
  senderName?: string
  refKey?: string
  parentMessageId?: string
  seq?: number
  createdAt?: string
  startedAt?: string
  finishedAt?: string
  durationMs?: number
  usageJson?: string
}

/** 消息分页 */
export interface MessagePaging {
  current: number
  size: number
  total: number
  loadedRecords: number
  loading: boolean
  finished: boolean
  initialized: boolean
}

/** 会话分页 */
export interface SessionPaging {
  current: number
  size: number
  total: number
  loading: boolean
  finished: boolean
}

/** Composable 选项 */
export interface UseChatStreamOptions {
  agentId: Ref<string>
  autoScroll?: boolean
  pageSize?: number
  sessionPageSize?: number
}

/** Composable 返回值 */
export interface UseChatStreamReturn {
  // 状态
  sessions: Ref<SessionVO[]>
  activeSessionId: Ref<string>
  messages: Ref<MessageVO[]>
  streaming: Ref<boolean>
  inputMessage: Ref<string>
  sessionKeyword: Ref<string>
  sessionCreating: Ref<boolean>
  autoScrollEnabled: Ref<boolean>
  sessionPaging: SessionPaging
  activeSession: Ref<SessionVO | null>

  // 方法
  sendMessage: (content?: string) => Promise<void>
  switchSession: (sessionId: string) => Promise<void>
  createSession: () => Promise<SessionVO | null>
  deleteSession: (sessionId: string) => Promise<void>
  loadSessions: (options?: { reset?: boolean }) => Promise<void>
  loadMoreMessages: () => Promise<void>
  cancelStream: () => void
  renderMarkdown: (content: string) => string

  // 滚动相关
  scrollToBottom: (force?: boolean) => Promise<void>
  scheduleScrollToBottom: (force?: boolean) => void
  messageListRef: Ref<HTMLElement | null>
  sessionListRef: Ref<HTMLElement | null>
}

// ==================== 常量定义 ====================

const SCROLL_BOTTOM_THRESHOLD = 64
const SCROLL_PAGE_THRESHOLD = 80
const DEFAULT_SESSION_PAGE_SIZE = 20
const DEFAULT_MESSAGE_PAGE_SIZE = 30

const STREAM_STAGE_META: Record<string, { label: string; order: number }> = {
  reasoning: { label: '推理事件', order: 10 },
  tool: { label: '工具调用事件', order: 20 },
  subagent: { label: '子智能体事件', order: 30 },
  message: { label: '正文事件', order: 40 }
}

const SSE_EVENT_LABELS: Record<string, string> = {
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

// ==================== 工具函数 ====================

const isRecord = (value: any): value is Record<string, any> =>
  Boolean(value) && typeof value === 'object' && !Array.isArray(value)

const getNowMs = (): number =>
  typeof performance !== 'undefined' ? performance.now() : Date.now()

const nowText = (): string => {
  const date = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const normalizeSessionId = (value: any): string =>
  value === undefined || value === null ? '' : String(value)

const createToolCallId = (): string =>
  `tool-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`

const createAuxiliaryBlockId = (kind: string): string =>
  `${kind}-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`

const createContentSegmentId = (): string =>
  `content-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`

const formatDuration = (duration = 0): string => {
  const milliseconds = Math.max(0, Math.round(duration || 0))
  if (milliseconds < 1000) return `${milliseconds}ms`
  if (milliseconds < 10000) return `${(milliseconds / 1000).toFixed(1)}s`
  return `${Math.round(milliseconds / 1000)}s`
}

const eventLabel = (eventName = ''): string =>
  SSE_EVENT_LABELS[eventName] || eventName || '未知事件'

const normalizedSseEvent = (streamEvent: StreamEventVO): string =>
  String(streamEvent?.sseEvent || '').toLowerCase()

const getStageKey = (sseEvent = ''): string | null => {
  const normalizedEvent = String(sseEvent || '').toLowerCase()

  if (normalizedEvent.startsWith('subagent_')) return 'subagent'
  if (normalizedEvent.startsWith('thinking_')) return 'reasoning'
  if (normalizedEvent.startsWith('tool_call_') || normalizedEvent.startsWith('tool_result_')) return 'tool'
  if (normalizedEvent === 'require_user_confirm' ||
    normalizedEvent === 'require_external_execution' ||
    normalizedEvent === 'user_confirm_result' ||
    normalizedEvent === 'external_execution_result') return 'tool'
  if (normalizedEvent.startsWith('message_')) return 'message'

  return null
}

const isStageEndEvent = (sseEvent = ''): boolean => {
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

const isReasoningStartEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'thinking_start'

const isReasoningDeltaEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'thinking_delta'

const isReasoningEndEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'thinking_end'

const isReasoningStreamEvent = (streamEvent: StreamEventVO): boolean =>
  isReasoningStartEvent(streamEvent) ||
  isReasoningDeltaEvent(streamEvent) ||
  isReasoningEndEvent(streamEvent)

const isToolStreamEvent = (streamEvent: StreamEventVO): boolean => {
  const sseEvent = normalizedSseEvent(streamEvent)
  return sseEvent.startsWith('tool_call_') || sseEvent.startsWith('tool_result_')
}

const isToolCallStartEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'tool_call_start'

const isToolCallDeltaEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'tool_call_delta'

const isToolCallEndEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'tool_call_end'

const isToolResultStartEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'tool_result_start'

const isToolResultTextDeltaEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'tool_result_text_delta'

const isToolResultDataDeltaEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'tool_result_data_delta'

const isToolResultEndEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'tool_result_end'

const isRequireUserConfirmEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'require_user_confirm'

const isRequireExternalExecutionEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'require_external_execution'

const isInterventionEvent = (streamEvent: StreamEventVO): boolean =>
  isRequireUserConfirmEvent(streamEvent) || isRequireExternalExecutionEvent(streamEvent)

const isSubAgentStreamEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent).startsWith('subagent_')

const isSubAgentTextDeltaEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'subagent_message_delta'

const isSubAgentThinkingDeltaEvent = (streamEvent: StreamEventVO): boolean =>
  normalizedSseEvent(streamEvent) === 'subagent_thinking_delta'

const isSubAgentStartEvent = (streamEvent: StreamEventVO): boolean => {
  if (!isSubAgentStreamEvent(streamEvent)) return false
  const sseEvent = normalizedSseEvent(streamEvent)
  return sseEvent === 'subagent_agent_start' || sseEvent === 'subagent_message_start'
}

const isSubAgentEndEvent = (streamEvent: StreamEventVO): boolean => {
  if (!isSubAgentStreamEvent(streamEvent)) return false
  const sseEvent = normalizedSseEvent(streamEvent)
  return sseEvent === 'subagent_agent_end' ||
    sseEvent === 'subagent_message_end' ||
    sseEvent === 'subagent_thinking_end'
}

const normalizeSourcePath = (sourcePath = ''): string =>
  String(sourcePath || '').trim()

const subAgentNameFromSource = (sourcePath = ''): string => {
  const normalizedPath = normalizeSourcePath(sourcePath).replace(/\\/g, '/')
  if (!normalizedPath || normalizedPath.toLowerCase() === 'main') return ''
  const parts = normalizedPath.split('/').filter(Boolean)
  return parts[parts.length - 1] || normalizedPath
}

const getSubAgentName = (streamEvent: StreamEventVO): string =>
  streamEvent?.subAgentName || subAgentNameFromSource(streamEvent?.sourcePath) || ''

const subAgentBlockKey = (streamEvent: StreamEventVO): string =>
  normalizeSourcePath(streamEvent?.sourcePath) || getSubAgentName(streamEvent) || 'subagent'

// ==================== Composable 实现 ====================

export function useChatStream(options: UseChatStreamOptions): UseChatStreamReturn {
  const {
    agentId,
    autoScroll = true,
    pageSize = DEFAULT_MESSAGE_PAGE_SIZE,
    sessionPageSize = DEFAULT_SESSION_PAGE_SIZE
  } = options

  // ==================== 状态定义 ====================

  const sessions = ref<SessionVO[]>([])
  const activeSessionId = ref('')
  const inputMessage = ref('')
  const sessionKeyword = ref('')
  const streaming = ref(false)
  const sessionCreating = ref(false)
  const autoScrollEnabled = ref(autoScroll)
  const messageListRef = ref<HTMLElement | null>(null)
  const sessionListRef = ref<HTMLElement | null>(null)
  const abortController = ref<AbortController | null>(null)

  let scrollFrame: number | null = null
  let pendingScrollForce = false
  let sessionSearchTimer: ReturnType<typeof setTimeout> | null = null

  const sessionPaging = reactive<SessionPaging>({
    current: 0,
    size: sessionPageSize,
    total: 0,
    loading: false,
    finished: false
  })

  // ==================== 计算属性 ====================

  const activeSession = computed<SessionVO | null>(() =>
    sessions.value.find((item) => item.id === activeSessionId.value) || null
  )

  const messages = computed<MessageVO[]>(() =>
    activeSession.value?.messages || []
  )

  // ==================== Markdown 渲染器 ====================

  const markdownRenderer = new MarkdownIt({
    html: false,
    linkify: true,
    breaks: true
  })

  const defaultLinkOpenRenderer = markdownRenderer.renderer.rules.link_open ||
    ((tokens, index, options, env, self) => self.renderToken(tokens, index, options))

  markdownRenderer.renderer.rules.link_open = (tokens, index, options, env, self) => {
    tokens[index].attrSet('target', '_blank')
    tokens[index].attrSet('rel', 'noopener noreferrer')
    return defaultLinkOpenRenderer(tokens, index, options, env, self)
  }

  const renderMarkdown = (content: string): string =>
    markdownRenderer.render(String(content || ''))

  // ==================== 分页工具 ====================

  const createMessagePaging = (): MessagePaging => ({
    current: 0,
    size: pageSize,
    total: 0,
    loadedRecords: 0,
    loading: false,
    finished: false,
    initialized: false
  })

  const normalizePageResult = (page: any = {}): {
    records: any[]
    current: number
    size: number
    total: number
    pages: number
  } => {
    const records = Array.isArray(page.records) ? page.records : []
    const current = Number(page.current ?? 1) || 1
    const size = Number(page.size ?? records.length) || records.length || 0
    const total = Number(page.total ?? records.length) || 0
    const pages = Number(page.pages ?? (size ? Math.ceil(total / size) : 1)) || 1
    return { records, current, size, total, pages }
  }

  // ==================== 会话工具 ====================

  const getDefaultTitle = (): string => `智能体会话`

  const buildLocalSession = (backendSession: any, fallbackTitle = getDefaultTitle()): SessionVO => {
    const sessionId = normalizeSessionId(backendSession?.id)
    if (!sessionId) throw new Error('Backend session id is empty')

    return {
      id: sessionId,
      backendSessionId: sessionId,
      title: backendSession?.title || fallbackTitle,
      updatedAt: backendSession?.lastMessageAt || backendSession?.updatedAt || backendSession?.createdAt || nowText(),
      messages: [],
      historyRecords: [],
      messagePaging: createMessagePaging()
    }
  }

  const requestBackendSession = async (title = getDefaultTitle()): Promise<SessionVO> => {
    const backendSession = await createAgentSession({
      agentId: agentId.value,
      title
    })
    return buildLocalSession(backendSession, title)
  }

  // ==================== 滚动处理 ====================

  const isNearBottom = (container: HTMLElement | null): boolean => {
    if (!container) return true
    return container.scrollHeight - container.scrollTop - container.clientHeight <= SCROLL_BOTTOM_THRESHOLD
  }

  const scrollToBottom = async (force = false): Promise<void> => {
    await nextTick()
    const container = messageListRef.value
    if (container && (force || autoScrollEnabled.value)) {
      container.scrollTop = container.scrollHeight
      autoScrollEnabled.value = true
    }
  }

  const scheduleScrollToBottom = (force = false): void => {
    pendingScrollForce = pendingScrollForce || force
    if (scrollFrame) return

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

  const handleMessageScroll = (): void => {
    const container = messageListRef.value
    if (container) {
      autoScrollEnabled.value = isNearBottom(container)
      if (container.scrollTop <= SCROLL_PAGE_THRESHOLD) {
        loadMoreMessages()
      }
    }
  }

  const handleSessionScroll = (): void => {
    const container = sessionListRef.value
    if (!container || sessionPaging.loading || sessionPaging.finished) return
    const distanceToBottom = container.scrollHeight - container.scrollTop - container.clientHeight
    if (distanceToBottom <= SCROLL_PAGE_THRESHOLD) {
      loadSessions()
    }
  }

  // ==================== 消息工具 ====================

  const appendMessage = (
    role: 'user' | 'assistant' | 'system',
    content: string,
    extra: Partial<MessageVO> = {},
    options: { forceScroll?: boolean } = {}
  ): MessageVO => {
    const session = activeSession.value
    if (!session) throw new Error('Active session is empty')

    const message: MessageVO = {
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
      session.title = content.slice(0, 18) || getDefaultTitle()
    }

    scrollToBottom(Boolean(options.forceScroll))
    return session.messages[session.messages.length - 1]
  }

  // ==================== 流式事件处理 ====================

  const createToolCall = (name = ''): ToolCall => {
    const now = getNowMs()
    return {
      id: createToolCallId(),
      name: name || '工具调用',
      process: '',
      result: '',
      rawInput: '',
      rawResult: '',
      status: 'running',
      argumentStarted: false,
      resultStarted: false,
      startedAt: now,
      updatedAt: now
    }
  }

  const ensureAuxiliaryBlocks = (message: MessageVO): AuxiliaryBlock[] => {
    if (!Array.isArray(message.auxiliaryBlocks)) message.auxiliaryBlocks = []
    return message.auxiliaryBlocks
  }

  const ensureContentSegments = (message: MessageVO): ContentSegment[] => {
    if (!Array.isArray(message.contentSegments)) message.contentSegments = []
    return message.contentSegments
  }

  const ensureToolCalls = (message: MessageVO): ToolCall[] => {
    if (!Array.isArray(message.toolCalls)) message.toolCalls = []
    return message.toolCalls
  }

  const ensureToolBlockIds = (message: MessageVO): Record<string, string> => {
    if (!message.activeToolBlockIds || typeof message.activeToolBlockIds !== 'object') {
      message.activeToolBlockIds = {}
    }
    return message.activeToolBlockIds
  }

  const ensureSubAgentBlockIds = (message: MessageVO): Record<string, string> => {
    if (!message.activeSubAgentBlockIds || typeof message.activeSubAgentBlockIds !== 'object') {
      message.activeSubAgentBlockIds = {}
    }
    return message.activeSubAgentBlockIds
  }

  const ensureStreamTiming = (message: MessageVO): StreamTiming => {
    if (!message.streamTiming) {
      message.streamTiming = {
        startedAt: 0,
        lastAt: 0,
        totalMs: 0,
        eventCount: 0
      }
    }
    if (!Array.isArray(message.eventStages)) message.eventStages = []
    return message.streamTiming
  }

  const nextAuxiliaryBlockSequence = (message: MessageVO, kind: string): number =>
    ensureAuxiliaryBlocks(message).filter((block) => block.kind === kind).length + 1

  const createAuxiliaryBlock = (
    message: MessageVO,
    kind: 'reasoning' | 'tool' | 'subagent',
    extra: Partial<AuxiliaryBlock> = {}
  ): AuxiliaryBlock => {
    const now = getNowMs()
    const sequence = nextAuxiliaryBlockSequence(message, kind)
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
      status: 'running',
      startedAt: now,
      endedAt: null,
      durationMs: 0,
      content: '',
      ...extra
    }
  }

  const touchAuxiliaryBlock = (block: AuxiliaryBlock, status: 'running' | 'done' | 'error' | 'waiting' | 'waiting_external' = 'running'): void => {
    const now = getNowMs()
    block.status = status
    block.durationMs = block.startedAt ? now - block.startedAt : 0
    if (status === 'running') {
      block.endedAt = null
    } else {
      block.endedAt = now
    }
  }

  const findAuxiliaryBlock = (message: MessageVO, blockId: string | null): AuxiliaryBlock | null => {
    if (!blockId) return null
    return ensureAuxiliaryBlocks(message).find((block) => block.id === blockId) || null
  }

  const findLastRunningBlock = (message: MessageVO, kind: string): AuxiliaryBlock | null => {
    const blocks = ensureAuxiliaryBlocks(message)
    for (let index = blocks.length - 1; index >= 0; index -= 1) {
      const block = blocks[index]
      if (block.kind === kind && block.status === 'running') return block
    }
    return null
  }

  const appendContentSegment = (message: MessageVO, delta: string): void => {
    const text = delta == null ? '' : String(delta)
    if (!text) return

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

  const appendAuxiliaryBlockSegment = (message: MessageVO, block: AuxiliaryBlock): void => {
    if (!message || !block?.id) return

    const segments = ensureContentSegments(message)
    if (segments.some((segment) => segment.type === 'auxiliary' && segment.blockId === block.id)) return

    segments.push({
      id: `auxiliary-${block.id}`,
      type: 'auxiliary',
      blockId: block.id
    })
  }

  const streamPayload = (streamEvent: StreamEventVO = {} as StreamEventVO): Record<string, any> =>
    isRecord(streamEvent.payload) ? streamEvent.payload : {}

  const streamToolCallId = (streamEvent: StreamEventVO = {} as StreamEventVO): string =>
    String(streamPayload(streamEvent).toolCallId || streamPayload(streamEvent).tool_call_id || streamPayload(streamEvent).id || '')

  const appendToolLine = (toolCall: ToolCall, key: 'process' | 'result', text: string): void => {
    const value = String(text || '').trim()
    if (!value) return
    toolCall[key] = toolCall[key] ? `${toolCall[key]}\n${value}` : value
  }

  const appendToolText = (toolCall: ToolCall, key: 'process' | 'result' | 'rawInput' | 'rawResult', text: string): void => {
    const value = text == null ? '' : String(text)
    if (!value) return
    toolCall[key] = `${toolCall[key] || ''}${value}`
  }

  // ==================== 推理事件处理 ====================

  const getReasoningBlock = (message: MessageVO, shouldCreate = false): AuxiliaryBlock | null => {
    let block = findAuxiliaryBlock(message, message.activeReasoningBlockId || null)

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

  const appendReasoningEvent = (message: MessageVO, streamEvent: StreamEventVO): boolean => {
    if (!message || !isReasoningStreamEvent(streamEvent)) return false

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

  // ==================== 工具事件处理 ====================

  const getToolBlock = (
    message: MessageVO,
    shouldCreate = false,
    toolName = '',
    toolCallId = ''
  ): AuxiliaryBlock => {
    const callId = String(toolCallId || '')
    const activeIds = ensureToolBlockIds(message)
    let block = callId ? findAuxiliaryBlock(message, activeIds[callId]) : null

    if (!block && callId) {
      block = ensureAuxiliaryBlocks(message).find((item) => item.kind === 'tool' && item.toolCall?.id === callId) || null
    }

    if (!block && !callId) {
      block = shouldCreate ? null : findAuxiliaryBlock(message, message.activeToolBlockId || null)
    }

    if (!block && !shouldCreate && !callId) {
      block = findLastRunningBlock(message, 'tool')
    }

    if (!block && (shouldCreate || callId || !message.activeToolBlockId)) {
      block = createAuxiliaryBlock(message, 'tool', { toolCall: createToolCall(toolName) })
      if (callId) block.toolCall!.id = callId
      ensureAuxiliaryBlocks(message).push(block)
      ensureToolCalls(message).push(block.toolCall!)
      appendAuxiliaryBlockSegment(message, block)
    }

    if (block) {
      message.activeToolBlockId = block.id
      if (callId) {
        activeIds[callId] = block.id
        if (block.toolCall) block.toolCall.id = callId
      }
    }

    return block!
  }

  const getToolCallFromBlock = (message: MessageVO, block: AuxiliaryBlock, toolName = ''): ToolCall => {
    if (!block.toolCall) {
      block.toolCall = createToolCall(toolName)
      ensureToolCalls(message).push(block.toolCall)
    }

    if (toolName && (!block.toolCall.name || block.toolCall.name === '工具调用')) {
      block.toolCall.name = toolName
    }

    return block.toolCall
  }

  const appendToolEvent = (message: MessageVO, streamEvent: StreamEventVO): boolean => {
    if (!message || !isToolStreamEvent(streamEvent)) return false

    const delta = streamEvent?.delta == null ? '' : String(streamEvent.delta)
    const isStartEvent = isToolCallStartEvent(streamEvent)
    const toolCallId = streamToolCallId(streamEvent)
    const block = getToolBlock(message, isStartEvent, isStartEvent ? delta : '', toolCallId)
    const toolCall = getToolCallFromBlock(message, block, isStartEvent ? delta : '')
    if (toolCallId) toolCall.id = toolCallId
    toolCall.updatedAt = getNowMs()

    if (isStartEvent) {
      if (delta) toolCall.name = delta
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
      if (toolCallId) delete ensureToolBlockIds(message)[toolCallId]
      return true
    }

    touchAuxiliaryBlock(block, 'running')
    return true
  }

  // ==================== 子智能体事件处理 ====================

  const findRunningSubAgentBlock = (message: MessageVO, key: string, name: string): AuxiliaryBlock | null => {
    const blocks = ensureAuxiliaryBlocks(message)
    for (let index = blocks.length - 1; index >= 0; index -= 1) {
      const block = blocks[index]
      if (block.kind !== 'subagent' || block.status !== 'running') continue
      if (block.subAgent?.sourcePath === key || block.subAgent?.name === name) return block
    }
    return null
  }

  const getSubAgentBlock = (message: MessageVO, streamEvent: StreamEventVO, shouldCreate = false): AuxiliaryBlock | null => {
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

    if (!block) return null

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

  const appendSubAgentEvent = (message: MessageVO, streamEvent: StreamEventVO): boolean => {
    if (!message || !isSubAgentStreamEvent(streamEvent)) return false

    const shouldCreate = isSubAgentStartEvent(streamEvent) ||
      isSubAgentTextDeltaEvent(streamEvent) ||
      isSubAgentThinkingDeltaEvent(streamEvent)
    const shouldFinish = isSubAgentEndEvent(streamEvent)
    const shouldShowExposed = normalizedSseEvent(streamEvent) === 'subagent_exposed'
    if (!shouldCreate && !shouldFinish && !shouldShowExposed) return false

    const block = getSubAgentBlock(message, streamEvent, shouldCreate)
    if (!block) return false
    const delta = streamEvent?.delta == null ? '' : String(streamEvent.delta)

    if (shouldShowExposed) {
      const label = streamEvent?.payload?.label || getSubAgentName(streamEvent) || '子智能体'
      block.title = `子智能体 ${label}`
      appendToolLine(block as any, 'content', `${label} 已准备就绪`)
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

  // ==================== 干预事件处理 ====================

  const normalizeToolCallPayload = (toolCall: any = {}): NormalizedToolCallPayload => ({
    id: toolCall.id || '',
    name: toolCall.name || '工具调用',
    input: toolCall.input || {},
    content: toolCall.content || '',
    metadata: toolCall.metadata || {},
    state: toolCall.state || ''
  })

  const buildInterventionFromEvent = (message: MessageVO, streamEvent: StreamEventVO): Intervention | null => {
    if (!isInterventionEvent(streamEvent)) return null

    const payload = streamEvent.payload && typeof streamEvent.payload === 'object' ? streamEvent.payload : {}
    const toolCalls = Array.isArray(payload.toolCalls)
      ? payload.toolCalls.map(normalizeToolCallPayload)
      : []

    return {
      type: isRequireUserConfirmEvent(streamEvent) ? 'userConfirm' : 'externalExecution',
      replyId: payload.replyId || '',
      runId: streamEvent.runId || message?.runId || '',
      toolCalls
    }
  }

  const formatToolInput = (input: any): string => {
    if (input == null || input === '') return ''
    if (typeof input === 'string') return input
    try {
      return JSON.stringify(input, null, 2)
    } catch {
      return String(input)
    }
  }

  const appendInterventionEvent = (message: MessageVO, streamEvent: StreamEventVO): boolean => {
    const intervention = buildInterventionFromEvent(message, streamEvent)
    if (!message || !intervention) return false

    const isUserConfirm = intervention.type === 'userConfirm'
    const toolCalls = intervention.toolCalls.length ? intervention.toolCalls : [normalizeToolCallPayload({})]

    toolCalls.forEach((pendingToolCall, index) => {
      const block = getToolBlock(message, index > 0, pendingToolCall.name)
      const toolCall = getToolCallFromBlock(message, block, pendingToolCall.name)
      toolCall.id = pendingToolCall.id || toolCall.id
      toolCall.name = pendingToolCall.name || toolCall.name
      toolCall.pendingToolCall = pendingToolCall
      toolCall.status = isUserConfirm ? 'waiting' : 'waiting_external'
      appendToolLine(toolCall, 'process', isUserConfirm ? '等待用户确认' : '等待外部系统执行')

      const inputText = formatToolInput(pendingToolCall.input)
      if (inputText) appendToolLine(toolCall, 'process', `调用参数：${inputText}`)

      touchAuxiliaryBlock(block, isUserConfirm ? 'waiting' : 'waiting_external')
    })

    message.pendingIntervention = intervention
    message.streamStatus = 'waiting'
    return true
  }

  // ==================== 流式事件记录 ====================

  const recordStreamEvent = (message: MessageVO, streamEvent: StreamEventVO): void => {
    if (!message || !streamEvent) return

    const timing = ensureStreamTiming(message)
    const now = getNowMs()
    const eventName = streamEvent.sseEvent || 'unknown'
    const stageKey = getStageKey(eventName)
    if (!stageKey) return

    const stageMeta = STREAM_STAGE_META[stageKey]

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

  const applyDoneUsage = (message: MessageVO, streamEvent: StreamEventVO): void => {
    if (!message || !streamEvent) return

    const usageToken = Number(streamEvent.usageToken ?? streamEvent.data?.usageToken)
    const usageTime = Number(streamEvent.usageTime ?? streamEvent.data?.usageTime)

    if (Number.isFinite(usageToken) && usageToken > 0) message.usageToken = usageToken
    if (Number.isFinite(usageTime) && usageTime > 0) message.usageTime = usageTime
  }

  // ==================== 辅助事件处理 ====================

  const appendAuxiliaryDelta = (message: MessageVO, session: SessionVO | null, streamEvent: StreamEventVO): void => {
    if (!message || !streamEvent) return

    let changed = false
    const subAgentEvent = isSubAgentStreamEvent(streamEvent)

    if (appendSubAgentEvent(message, streamEvent)) changed = true

    if (!subAgentEvent) {
      if (appendReasoningEvent(message, streamEvent)) changed = true
      if (appendToolEvent(message, streamEvent)) changed = true
      if (appendInterventionEvent(message, streamEvent)) changed = true
    }

    if (!changed) return
    if (session) session.updatedAt = nowText()
    scheduleScrollToBottom()
  }

  const finishStreamEvents = (message: MessageVO, status: 'running' | 'done' | 'error' | 'waiting' = 'done'): void => {
    if (!message?.streamTiming) return

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

  const appendStreamText = (message: MessageVO, session: SessionVO | null, delta: string): void => {
    if (!delta) return

    message.content = `${message.content || ''}${delta}`
    message.displayContent = message.content
    appendContentSegment(message, delta)
    message.pending = false
    message.typing = true
    if (session) session.updatedAt = nowText()
    scheduleScrollToBottom()
  }

  // ==================== 流式执行 ====================

  const runAssistantStream = async (
    streamer: typeof chatStream,
    data: any,
    assistantMessage: MessageVO,
    session: SessionVO | null
  ): Promise<Intervention | null> => {
    let pendingIntervention: Intervention | null = null
    abortController.value = new AbortController()

    await streamer(
      data,
      {
        onEvent: (streamEvent) => {
          if (streamEvent.runId) assistantMessage.runId = streamEvent.runId
          recordStreamEvent(assistantMessage, streamEvent)
          appendAuxiliaryDelta(assistantMessage, session, streamEvent)
          if (assistantMessage.pendingIntervention) {
            pendingIntervention = assistantMessage.pendingIntervention
          }
        },
        onMessage: (delta) => {
          appendStreamText(assistantMessage, session, delta)
        },
        onError: (message) => {
          finishStreamEvents(assistantMessage, 'error')
          assistantMessage.content = message
          assistantMessage.displayContent = message
          assistantMessage.contentSegments = [{
            id: createContentSegmentId(),
            type: 'content',
            content: message
          }]
          assistantMessage.pending = false
          assistantMessage.typing = false
          assistantMessage.error = true
          assistantMessage.pendingIntervention = null
          if (session) session.updatedAt = nowText()
          scrollToBottom()
        },
        onDone: (streamEvent) => {
          applyDoneUsage(assistantMessage, streamEvent)
          finishStreamEvents(assistantMessage, assistantMessage.pendingIntervention ? 'waiting' : 'done')
          if (session) session.updatedAt = nowText()
        }
      },
      { signal: abortController.value.signal }
    )

    assistantMessage.pending = false
    assistantMessage.typing = false
    assistantMessage.displayContent = assistantMessage.content
    if (session) session.updatedAt = nowText()

    return pendingIntervention
  }

  // ==================== 会话管理 ====================

  const createSession = async (): Promise<SessionVO | null> => {
    if (!agentId.value || sessionCreating.value) return null

    sessionCreating.value = true
    try {
      const session = await requestBackendSession()
      sessions.value.unshift(session)
      activeSessionId.value = session.id
      autoScrollEnabled.value = true
      await scrollToBottom(true)
      return session
    } catch (error: any) {
      console.error('Create session failed:', error)
      return null
    } finally {
      sessionCreating.value = false
    }
  }

  const resetSessionPaging = (): void => {
    sessionPaging.current = 0
    sessionPaging.total = 0
    sessionPaging.loading = false
    sessionPaging.finished = false
  }

  const mergeSessionPage = (incomingSessions: SessionVO[], reset = false): void => {
    if (reset) sessions.value = []
    const existingIds = new Set(sessions.value.map((session) => session.id))
    incomingSessions.forEach((session) => {
      if (!existingIds.has(session.id)) {
        sessions.value.push(session)
        existingIds.add(session.id)
      }
    })
  }

  const loadSessions = async (options: { reset?: boolean } = {}): Promise<void> => {
    const { reset = false } = options
    if (!agentId.value || sessionPaging.loading) return
    if (reset) {
      resetSessionPaging()
      sessions.value = []
      activeSessionId.value = ''
    } else if (sessionPaging.finished) {
      return
    }

    sessionPaging.loading = true
    try {
      const page = normalizePageResult(await pageAgentSessions({
        current: sessionPaging.current + 1,
        size: sessionPaging.size,
        agentId: agentId.value,
        keyword: sessionKeyword.value.trim()
      }))
      const nextSessions = page.records.map((record) => buildLocalSession(record))
      mergeSessionPage(nextSessions, reset)
      sessionPaging.current = page.current
      sessionPaging.total = page.total
      sessionPaging.finished = page.current >= page.pages || sessions.value.length >= page.total || nextSessions.length < page.size

      if (reset && !activeSessionId.value && sessions.value.length > 0) {
        activeSessionId.value = sessions.value[0].id
      }
    } catch (error: any) {
      console.error('Load sessions failed:', error)
    } finally {
      sessionPaging.loading = false
    }
  }

  const switchSession = async (sessionId: string): Promise<void> => {
    const session = sessions.value.find((s) => s.id === sessionId)
    if (!session) return

    activeSessionId.value = sessionId
    autoScrollEnabled.value = true

    if (!session.messagePaging?.initialized) {
      await loadSessionMessages(session, { reset: true, scrollToLatest: true })
    } else {
      await scrollToBottom(true)
    }
  }

  const deleteSession = async (sessionId: string): Promise<void> => {
    const session = sessions.value.find((s) => s.id === sessionId)
    if (!session) return

    try {
      await deleteAgentSession(session.backendSessionId || session.id)
    } catch (error: any) {
      console.error('Delete session failed:', error)
      return
    }

    const index = sessions.value.findIndex((item) => item.id === sessionId)
    sessions.value.splice(index, 1)

    if (activeSessionId.value === sessionId) {
      activeSessionId.value = sessions.value[Math.max(index - 1, 0)]?.id || sessions.value[0]?.id || ''
      const nextSession = activeSession.value
      if (nextSession && !nextSession.messagePaging?.initialized) {
        await loadSessionMessages(nextSession, { reset: true, scrollToLatest: true })
      }
    }
  }

  // ==================== 消息加载 ====================

  const loadSessionMessages = async (
    session: SessionVO,
    options: { reset?: boolean; scrollToLatest?: boolean } = {}
  ): Promise<void> => {
    const { reset = false, scrollToLatest = false } = options
    if (!session?.backendSessionId) return

    if (!session.messagePaging) session.messagePaging = createMessagePaging()
    const paging = session.messagePaging
    if (paging.loading) return
    if (reset) {
      session.messages = []
      session.historyRecords = []
      Object.assign(paging, createMessagePaging())
    } else if (paging.finished) {
      return
    }

    const container = messageListRef.value
    const previousHeight = container?.scrollHeight || 0
    const previousTop = container?.scrollTop || 0

    paging.loading = true
    try {
      const page = normalizePageResult(await pageAgentMessages({
        current: paging.current + 1,
        size: paging.size,
        sessionId: session.backendSessionId
      }))

      // 简化处理：直接将记录转换为消息
      const historyMessages: MessageVO[] = page.records.map((record: any) => ({
        id: normalizeSessionId(record.id),
        role: String(record.role || '').toLowerCase() === 'user' ? 'user' : 'assistant',
        content: record.textContent || record.content || '',
        displayContent: record.textContent || record.content || '',
        contentSegments: (record.textContent || record.content) ? [{
          id: createContentSegmentId(),
          type: 'content',
          content: record.textContent || record.content || ''
        }] : [],
        createdAt: record.createdAt || record.createTime || '',
        fromHistory: true
      }))

      const liveMessages = reset
        ? []
        : session.messages.filter((message) => !message.fromHistory)

      session.messages = [...historyMessages, ...liveMessages]

      paging.current = page.current
      paging.total = page.total
      paging.loadedRecords = historyMessages.length
      paging.finished = page.current >= page.pages || paging.loadedRecords >= page.total || page.records.length < page.size
      paging.initialized = true

      await nextTick()
      if (scrollToLatest) {
        await scrollToBottom(true)
      } else if (!reset && container) {
        container.scrollTop = container.scrollHeight - previousHeight + previousTop
      }
    } catch (error: any) {
      console.error('Load messages failed:', error)
    } finally {
      paging.loading = false
    }
  }

  const loadMoreMessages = async (): Promise<void> => {
    const session = activeSession.value
    if (!session?.messagePaging?.initialized || session.messagePaging.loading || session.messagePaging.finished) return
    await loadSessionMessages(session)
  }

  // ==================== 发送消息 ====================

  const cancelStream = (): void => {
    abortController.value?.abort()
    abortController.value = null
    streaming.value = false
  }

  const sendMessage = async (content?: string): Promise<void> => {
    const messageContent = (content || inputMessage.value).trim()
    if (!messageContent) return
    if (!agentId.value) return

    let session = activeSession.value
    if (!session) {
      session = await createSession()
    }
    const backendSessionId = normalizeSessionId(session?.backendSessionId)
    if (!session || !backendSessionId) return

    inputMessage.value = ''
    autoScrollEnabled.value = true
    appendMessage('user', messageContent, {}, { forceScroll: true })

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
        planProgress: { total: 0, completed: 0, inProgress: 0, pending: 0, percent: 0 },
        planExpanded: true,
        lastPlanEvent: null,
        runId: null
      },
      { forceScroll: true }
    )

    streaming.value = true
    abortController.value = new AbortController()

    try {
      await runAssistantStream(
        chatStream,
        {
          agentId: agentId.value,
          sessionId: backendSessionId,
          content: messageContent
        },
        assistantMessage,
        session
      )
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
        assistantMessage.contentSegments = [{
          id: createContentSegmentId(),
          type: 'content',
          content: assistantMessage.content
        }]
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

  // ==================== 生命周期 ====================

  watch(
    () => agentId.value,
    async () => {
      cancelStream()
      await loadSessions({ reset: true })
    },
    { immediate: true }
  )

  watch(
    sessionKeyword,
    () => {
      if (sessionSearchTimer) clearTimeout(sessionSearchTimer)
      sessionSearchTimer = setTimeout(() => {
        sessionSearchTimer = null
        loadSessions({ reset: true })
      }, 300)
    }
  )

  onBeforeUnmount(() => {
    cancelStream()
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

  // ==================== 返回 ====================

  return {
    // 状态
    sessions,
    activeSessionId,
    messages,
    streaming,
    inputMessage,
    sessionKeyword,
    sessionCreating,
    autoScrollEnabled,
    sessionPaging,
    activeSession: activeSession as Ref<SessionVO | null>,

    // 方法
    sendMessage,
    switchSession,
    createSession,
    deleteSession,
    loadSessions,
    loadMoreMessages,
    cancelStream,
    renderMarkdown,

    // 滚动相关
    scrollToBottom,
    scheduleScrollToBottom,
    messageListRef,
    sessionListRef
  }
}

// ==================== 辅助工具导出 ====================

export {
  formatDuration,
  eventLabel,
  nowText,
  normalizeSessionId
}
