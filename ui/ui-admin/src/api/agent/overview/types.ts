// 运行总览相关类型定义

/** 运行总览数据 */
export interface RunOverviewVO {
  totalRuns: number
  successRuns: number
  failedRuns: number
  avgDuration: number
  totalTokens: number
  totalCost: number
  activeAgents: number
  recentRuns: RunItemVO[]
  runsTrend: TrendItemVO[]
}

/** 运行项 */
export interface RunItemVO {
  id: number
  agentId: number
  agentName?: string
  status: number
  duration?: number
  tokens?: number
  cost?: number
  createTime?: Date
  endTime?: Date
}

/** 趋势项 */
export interface TrendItemVO {
  date: string
  count: number
  successCount: number
  failedCount: number
}

/** 交互历史项 */
export interface InteractionHistoryVO {
  id: number
  agentId: number
  agentName?: string
  sessionId: number
  userId?: number
  userName?: string
  input?: string
  output?: string
  status: number
  duration?: number
  tokens?: number
  createTime?: Date
}

/** 运行总览查询参数 */
export interface RunOverviewParams {
  startDate?: string
  endDate?: string
  agentId?: number
}
