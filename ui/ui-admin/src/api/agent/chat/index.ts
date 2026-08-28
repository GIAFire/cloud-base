import request from '@/config/axios'
import { config } from '@/config/axios/config'
import { getAccessToken, getTenantId, getVisitTenantId } from '@/utils/auth'

export interface ChatSession {
  id: string
  agentId: string
  title?: string
  lastMessageSeq: string
  lastMessageAt?: number | string
}

export interface ChatSessionPage {
  list: ChatSession[]
  total: number
}

export interface ChatMessage {
  id: string
  sessionId: string
  runId: string
  seq: string
  role: 'USER' | 'ASSISTANT' | 'SYSTEM' | 'TOOL'
  messageType: string
  content: string
  createTime?: number | string
}

export interface ChatMessagePage {
  list: ChatMessage[]
  nextBeforeSeq?: string
  hasMore: boolean
}

export interface ChatStreamRequest {
  agentId: string
  sessionId?: string
  requestId: string
  content: string
}

export type ChatStreamEventType =
  | 'run.started'
  | 'message.thinking.delta'
  | 'message.delta'
  | 'run.completed'
  | 'run.failed'

export interface ChatStreamEvent {
  schemaVersion: number
  sessionId: string
  runId: string
  sequence: number
  type: ChatStreamEventType
  delta?: string
  errorCode?: string
  errorMessage?: string
}

export const getChatSessionPage = (params: { agentId: string; pageNo: number; pageSize: number }) =>
  request.get<ChatSessionPage>({ url: '/agent/chat/session/page', params })

export const getChatMessagePage = (params: {
  sessionId: string
  beforeSeq?: string
  pageSize: number
}) => request.get<ChatMessagePage>({ url: '/agent/chat/message/page', params })

/**
 * 浏览器原生 fetch 流客户端。POST SSE 需要认证和租户请求头，不能使用 EventSource。
 * 本函数不自动重连；调用者必须在切换会话或离开页面时取消 signal。
 */
export const streamChat = async (
  body: ChatStreamRequest,
  signal: AbortSignal,
  onEvent: (event: ChatStreamEvent) => void
) => {
  const headers: Record<string, string> = {
    Accept: 'text/event-stream',
    'Content-Type': 'application/json'
  }
  const accessToken = getAccessToken()
  if (accessToken) headers.Authorization = `Bearer ${accessToken}`

  if (import.meta.env.VITE_APP_TENANT_ENABLE === 'true') {
    const tenantId = getTenantId()
    const visitTenantId = getVisitTenantId()
    if (tenantId) headers['tenant-id'] = String(tenantId)
    if (accessToken && visitTenantId) headers['visit-tenant-id'] = String(visitTenantId)
  }

  const response = await fetch(`${config.base_url}/agent/chat/stream`, {
    method: 'POST',
    headers,
    body: JSON.stringify(body),
    signal
  })
  const contentType = response.headers.get('content-type') || ''
  if (!response.ok || !contentType.includes('text/event-stream') || !response.body) {
    throw new Error(await readErrorMessage(response))
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  while (true) {
    const { value, done } = await reader.read()
    buffer += decoder.decode(value, { stream: !done }).replace(/\r\n/g, '\n').replace(/\r/g, '\n')
    let boundary = buffer.indexOf('\n\n')
    while (boundary >= 0) {
      const block = buffer.slice(0, boundary)
      buffer = buffer.slice(boundary + 2)
      dispatchEventBlock(block, onEvent)
      boundary = buffer.indexOf('\n\n')
    }
    if (done) break
  }
  if (buffer.trim()) dispatchEventBlock(buffer, onEvent)
}

const dispatchEventBlock = (block: string, onEvent: (event: ChatStreamEvent) => void) => {
  const dataLines: string[] = []
  for (const line of block.split('\n')) {
    if (line.startsWith(':')) continue
    if (line.startsWith('data:')) dataLines.push(line.slice(5).trimStart())
  }
  if (!dataLines.length) return
  onEvent(JSON.parse(dataLines.join('\n')) as ChatStreamEvent)
}

const readErrorMessage = async (response: Response) => {
  const fallback = `聊天请求失败（HTTP ${response.status}）`
  try {
    const text = await response.text()
    if (!text) return fallback
    const result = JSON.parse(text) as { msg?: string; message?: string }
    return result.msg || result.message || fallback
  } catch {
    return fallback
  }
}

export const createChatRequestId = () => {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) return crypto.randomUUID()
  return `${Date.now()}-${Math.random().toString(36).slice(2)}`
}
