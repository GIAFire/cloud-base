import request from '@/config/axios'
import type {
  AgentVO,
  AgentRunVO,
  AgentMetricsVO,
  AgentPageReqVO,
  AgentRunPageReqVO
} from './types'

/** 获取 Agent 指标 */
export const getAgentMetrics = (): Promise<AgentMetricsVO> => {
  return request.get({ url: '/agent/agent/metrics' })
}

/** 获取最近的 Agent 运行记录 */
export const getRecentAgentRuns = (size: number = 5): Promise<AgentRunVO[]> => {
  return request.get({ url: '/agent/agent/recent-runs', params: { size } })
}

/** 分页查询 Agent 列表 */
export const pageAgents = (params: AgentPageReqVO) => {
  return request.get({ url: '/agent/agent/page', params })
}

/** 获取 Agent 详情 */
export const getAgent = (id: number): Promise<AgentVO> => {
  return request.get({ url: `/agent/agent/${id}` })
}

/** 创建 Agent */
export const createAgent = (data: AgentVO) => {
  return request.post({ url: '/agent/agent', data })
}

/** 更新 Agent */
export const updateAgent = (id: number, data: AgentVO) => {
  return request.put({ url: `/agent/agent/${id}`, data })
}

/** 删除 Agent */
export const deleteAgent = (id: number) => {
  return request.delete({ url: `/agent/agent/${id}` })
}

/** 分页查询 Agent 运行记录 */
export const pageAgentRuns = (id: number, params: AgentRunPageReqVO) => {
  return request.get({ url: `/agent/agent/${id}/runs`, params })
}
