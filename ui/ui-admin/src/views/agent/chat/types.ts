export interface ChatViewMessage {
  key: string
  id?: string
  runId?: string
  seq?: string
  role: 'user' | 'assistant'
  content: string
  thinking?: string
  status?: 'streaming' | 'completed' | 'failed'
  errorMessage?: string
  createTime?: number | string
}
