/** Agent 信息 */
export interface AgentVO {
  id: number
  name: string
  description?: string
  systemPrompt?: string
  modelConfigId?: number
  status: number
  createTime: Date
  updateTime?: Date
}

/** Agent 运行记录 */
export interface AgentRunVO {
  id: number
  agentId: number
  agentName?: string
  status: number
  startTime: Date
  endTime?: Date
  duration?: number
  inputTokens?: number
  outputTokens?: number
  totalTokens?: number
  errorMessage?: string
}

/** Agent 指标 */
export interface AgentMetricsVO {
  totalAgents: number
  activeAgents: number
  totalRuns: number
  successRuns: number
  failedRuns: number
  avgDuration?: number
}

/** Agent 分页查询参数 */
export interface AgentPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  status?: number
}

/** Agent 运行记录分页查询参数 */
export interface AgentRunPageReqVO {
  pageNo: number
  pageSize: number
  status?: number
  startTime?: Date
  endTime?: Date
}
