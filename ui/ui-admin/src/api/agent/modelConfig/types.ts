/** 模型配置信息 */
export interface ModelConfigVO {
  id: number
  name: string
  provider: string
  model: string
  apiKey?: string
  baseUrl?: string
  maxTokens?: number
  temperature?: number
  status: number
  createTime: Date
  updateTime?: Date
}

/** 模型调用日志 */
export interface ModelCallLogVO {
  id: number
  modelConfigId: number
  modelConfigName?: string
  inputTokens: number
  outputTokens: number
  totalTokens: number
  duration: number
  status: number
  errorMessage?: string
  createTime: Date
}

/** 模型指标 */
export interface ModelMetricsVO {
  totalModels: number
  activeModels: number
  totalCalls: number
  successCalls: number
  failedCalls: number
  avgDuration?: number
  totalInputTokens?: number
  totalOutputTokens?: number
}

/** 模型分析数据 */
export interface ModelAnalyticsVO {
  dates: string[]
  callCounts: number[]
  tokenCounts: number[]
  avgDurations: number[]
}

/** 模型配置分页查询参数 */
export interface ModelConfigPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  provider?: string
  status?: number
}

/** 模型调用日志分页查询参数 */
export interface ModelCallLogPageReqVO {
  pageNo: number
  pageSize: number
  modelConfigId?: number
  status?: number
  startTime?: Date
  endTime?: Date
}

/** 模型配置测试参数 */
export interface ModelConfigTestReqVO {
  id?: number
  provider: string
  model: string
  apiKey?: string
  baseUrl?: string
  prompt?: string
}
