// 对话相关类型定义

/** 代理会话信息 */
export interface AgentSessionVO {
  id: number
  agentId: number
  agentName?: string
  userId?: number
  userName?: string
  title?: string
  status?: number
  messageCount?: number
  createTime?: Date
  updateTime?: Date
}

/** 代理消息信息 */
export interface AgentMessageVO {
  id: number
  sessionId: number
  role: 'user' | 'assistant' | 'system'
  content: string
  contentType?: string
  metadata?: Record<string, any>
  createTime?: Date
}

/** 流式事件 */
export interface StreamEventVO {
  id: string
  sseEvent: string
  eventName: string
  eventType: string
  runId: string
  seq: number | null
  sourcePath: string | null
  subAgentName: string | null
  subAgentInstanceId: string | null
  subAgentTaskId: string | null
  usageToken: number | null
  usageTime: number | null
  payload: any
  delta: string
  data: any
  rawData: string
  receivedAt: number
}

/** 流式事件处理器 */
export interface StreamHandlers {
  onOpen?: () => void
  onMessage?: (content: string, event: StreamEventVO) => void
  onEvent?: (event: StreamEventVO) => void
  onDone?: (event: StreamEventVO) => void
  onError?: (message: string, event: StreamEventVO) => void
}

/** 流式请求选项 */
export interface StreamOptions {
  signal?: AbortSignal
}

/** 对话请求 */
export interface ChatReqVO {
  sessionId: number
  agentId: number
  message: string
  files?: string[]
  metadata?: Record<string, any>
}

/** 会话创建请求 */
export interface SessionCreateReqVO {
  agentId: number
  title?: string
}

/** 用户确认请求 */
export interface UserConfirmReqVO {
  sessionId: number
  runId: string
  confirmed: boolean
  message?: string
}

/** 外部执行请求 */
export interface ExternalExecutionReqVO {
  sessionId: number
  runId: string
  result: any
}
