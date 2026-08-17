import request from '@/config/axios'
import { getAccessToken } from '@/utils/auth'
import type {
  AgentSessionVO,
  AgentMessageVO,
  StreamEventVO,
  StreamHandlers,
  StreamOptions,
  ChatReqVO,
  SessionCreateReqVO,
  UserConfirmReqVO,
  ExternalExecutionReqVO
} from './types'

const baseUrl = '/agent'

// 流式对话请求
export const chatStream = async (data: ChatReqVO, handlers: StreamHandlers = {}, options: StreamOptions = {}) => {
  return postStream(`${baseUrl}/agent/chat/chatStream`, data, handlers, options)
}

// 创建代理会话
export const createAgentSession = (data: SessionCreateReqVO) => {
  return request.post<AgentSessionVO>({ url: `${baseUrl}/agentSession/create`, data })
}

// 分页查询代理会话
export const pageAgentSessions = (params: PageParam) => {
  return request.get({ url: `${baseUrl}/agentSession/page`, params })
}

// 删除代理会话
export const deleteAgentSession = (id: number | string) => {
  return request.get({ url: `${baseUrl}/agentSession/delete/${id}` })
}

// 分页查询代理消息
export const pageAgentMessages = (params: PageParam) => {
  return request.get({ url: `${baseUrl}/agentMessage/page`, params })
}

// 用户确认流式请求
export const userConfirmStream = async (data: UserConfirmReqVO, handlers: StreamHandlers = {}, options: StreamOptions = {}) => {
  return postStream(`${baseUrl}/agent/chat/userConfirm`, data, handlers, options)
}

// 外部执行流式请求
export const externalExecutionStream = async (data: ExternalExecutionReqVO, handlers: StreamHandlers = {}, options: StreamOptions = {}) => {
  return postStream(`${baseUrl}/agent/chat/externalExecution`, data, handlers, options)
}

// ==================== 内部工具函数 ====================

const isObject = (value: any): value is object => typeof value === 'object' && value !== null

const isPlainObject = (value: any): value is Record<string, any> => {
  return Object.prototype.toString.call(value) === '[object Object]'
}

const isIdKey = (key: string): boolean => {
  return key === 'id' || key === 'ids' || key.endsWith('Id') || key.endsWith('Ids') || key.endsWith('ID') || key.endsWith('IDs')
}

const stringifyRequestIds = (value: any, key = ''): any => {
  if (isIdKey(key)) {
    if (Array.isArray(value)) {
      return value.map((item) => stringifyRequestIds(item, key))
    }
    return value === '' || value === undefined || value === null ? value : String(value)
  }

  if (Array.isArray(value)) {
    return value.map((item) => stringifyRequestIds(item))
  }

  if (!isPlainObject(value)) {
    return value
  }

  return Object.entries(value).reduce((result, [entryKey, entryValue]) => {
    result[entryKey] = stringifyRequestIds(entryValue, entryKey)
    return result
  }, {} as Record<string, any>)
}

const stringifyChatRequest = (data: any = {}): string => {
  return JSON.stringify(stringifyRequestIds(data))
}

const readRawId = (rawData: string, key: string): string | null => {
  const match = String(rawData || '').match(new RegExp(`"${key}"\\s*:\\s*"?([0-9]+)"?`))
  return match ? match[1] : null
}

const normalizeId = (value: any, rawData: string, key: string): string | null => {
  return readRawId(rawData, key) ?? (value === undefined || value === null ? null : String(value))
}

const normalizeStreamEvent = (eventName: string, eventId: string, parsedData: any, rawData: string): StreamEventVO => {
  const dataObject = isObject(parsedData) ? parsedData : null
  const delta = dataObject
    ? dataObject.delta ?? dataObject.content ?? ''
    : String(parsedData)

  return {
    id: eventId || normalizeId(dataObject?.seq, rawData, 'seq') || '',
    sseEvent: eventName,
    eventName,
    eventType: dataObject?.eventType || eventName,
    runId: normalizeId(dataObject?.runId, rawData, 'runId') || '',
    seq: dataObject?.seq ?? null,
    sourcePath: dataObject?.sourcePath ?? null,
    subAgentName: dataObject?.subAgentName ?? null,
    subAgentInstanceId: normalizeId(dataObject?.subAgentInstanceId, rawData, 'subAgentInstanceId'),
    subAgentTaskId: normalizeId(dataObject?.subAgentTaskId, rawData, 'subAgentTaskId'),
    usageToken: dataObject?.usageToken ?? null,
    usageTime: dataObject?.usageTime ?? null,
    payload: dataObject?.payload ?? null,
    delta,
    data: parsedData,
    rawData,
    receivedAt: Date.now()
  }
}

const isTextDeltaEvent = (streamEvent: StreamEventVO): boolean => {
  const sseEvent = String(streamEvent.sseEvent || '')
  return sseEvent === 'message_delta'
}

const isStreamDoneEvent = (streamEvent: StreamEventVO): boolean => {
  return streamEvent.sseEvent === 'agent_end' ||
    streamEvent.sseEvent === 'request_stop' ||
    streamEvent.sseEvent === 'exceed_max_iters' ||
    streamEvent.sseEvent === 'require_user_confirm' ||
    streamEvent.sseEvent === 'require_external_execution'
}

const dispatchSseBlock = (block: string, handlers: StreamHandlers): boolean => {
  if (!block.trim()) {
    return false
  }

  let eventName = 'message'
  let eventId = ''
  const dataLines: string[] = []

  block.split(/\r?\n/).forEach((line) => {
    if (!line || line.startsWith(':')) {
      return
    }

    const index = line.indexOf(':')
    const field = index >= 0 ? line.slice(0, index) : line
    let value = index >= 0 ? line.slice(index + 1) : ''

    if (value.startsWith(' ')) {
      value = value.slice(1)
    }

    if (field === 'event') {
      eventName = value
    }
    if (field === 'id') {
      eventId = value
    }
    if (field === 'data') {
      dataLines.push(value)
    }
  })

  if (!dataLines.length) {
    return false
  }

  const rawData = dataLines.join('\n')
  let parsedData: any = rawData

  try {
    parsedData = JSON.parse(rawData)
  } catch {
    parsedData = rawData
  }

  const streamEvent = normalizeStreamEvent(eventName, eventId, parsedData, rawData)
  const content = streamEvent.delta

  handlers.onEvent?.(streamEvent)

  if (isStreamDoneEvent(streamEvent)) {
    handlers.onDone?.(streamEvent)
    return true
  }

  if (streamEvent.sseEvent === 'error') {
    handlers.onError?.(content || '对话请求失败', streamEvent)
    return true
  }

  if (isTextDeltaEvent(streamEvent)) {
    handlers.onMessage?.(content || '', streamEvent)
  }

  return false
}

const consumeSseBuffer = (buffer: string, handlers: StreamHandlers): { rest: string; completed: boolean } => {
  const blocks = buffer.split(/\r?\n\r?\n/)
  const rest = blocks.pop() || ''
  let completed = false

  for (const block of blocks) {
    if (dispatchSseBlock(block, handlers)) {
      completed = true
      break
    }
  }

  return { rest, completed }
}

const postStream = async (url: string, data: any, handlers: StreamHandlers = {}, options: StreamOptions = {}) => {
  // 使用正确的 token 获取方式
  const token = getAccessToken()
  // 使用正确的 baseURL：VITE_BASE_URL + VITE_API_URL
  const baseURL = (import.meta.env.VITE_BASE_URL || '') + (import.meta.env.VITE_API_URL || '')

  const headers: Record<string, string> = {
    Accept: 'text/event-stream',
    'Content-Type': 'application/json'
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${baseURL}${url}`, {
    method: 'POST',
    headers,
    body: stringifyChatRequest(data),
    signal: options.signal
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || `对话请求失败：${response.status}`)
  }

  if (!response.body) {
    throw new Error('当前浏览器不支持流式响应')
  }

  handlers.onOpen?.()

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()

    if (done) {
      break
    }

    buffer += decoder.decode(value, { stream: true })
    const result = consumeSseBuffer(buffer, handlers)
    buffer = result.rest
    if (result.completed) {
      await reader.cancel()
      return
    }
  }

  buffer += decoder.decode()
  if (buffer.trim()) {
    dispatchSseBlock(buffer, handlers)
  }
}
