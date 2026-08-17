// 子代理相关类型定义

/** 子代理信息 */
export interface SubagentVO {
  id: number
  name: string
  description?: string
  type?: string
  agentId?: number
  agentName?: string
  modelConfigId?: number
  modelConfigName?: string
  systemPrompt?: string
  skills?: string[]
  tools?: string[]
  status?: number
  config?: Record<string, any>
  createTime?: Date
  updateTime?: Date
}

/** 子代理指标 */
export interface SubagentMetricsVO {
  totalSubagents: number
  activeSubagents: number
  totalTasks: number
  successRate: number
}

/** 本地代理选项 */
export interface LocalAgentOptionVO {
  id: number
  name: string
  type?: string
}

/** 子代理任务 */
export interface SubagentTaskVO {
  id: number
  subagentId: number
  subagentName?: string
  input?: string
  output?: string
  status?: number
  errorMessage?: string
  duration?: number
  createTime?: Date
  endTime?: Date
}

/** 子代理创建请求 */
export interface SubagentCreateReqVO {
  name: string
  description?: string
  type?: string
  agentId?: number
  modelConfigId?: number
  systemPrompt?: string
  skills?: string[]
  tools?: string[]
  config?: Record<string, any>
}

/** 子代理更新请求 */
export interface SubagentUpdateReqVO {
  id: number
  name?: string
  description?: string
  type?: string
  agentId?: number
  modelConfigId?: number
  systemPrompt?: string
  skills?: string[]
  tools?: string[]
  config?: Record<string, any>
}
