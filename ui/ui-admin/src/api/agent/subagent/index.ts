import request from '@/config/axios'
import type {
  SubagentVO,
  SubagentMetricsVO,
  LocalAgentOptionVO,
  SubagentTaskVO,
  SubagentCreateReqVO,
  SubagentUpdateReqVO
} from './types'

const baseUrl = '/agent'

// 查询子代理列表
export const listSubagents = () => {
  return request.get<SubagentVO[]>({ url: `${baseUrl}/subagent/list` })
}

// 分页查询子代理
export const pageSubagents = (params: PageParam) => {
  return request.get({ url: `${baseUrl}/subagent/page`, params })
}

// 获取子代理指标
export const getSubagentMetrics = () => {
  return request.get<SubagentMetricsVO>({ url: `${baseUrl}/subagent/metrics` })
}

// 获取本地代理选项列表
export const listLocalAgentOptions = () => {
  return request.get<LocalAgentOptionVO[]>({ url: `${baseUrl}/subagent/local-agent-options` })
}

// 查询最近子代理任务
export const listRecentSubagentTasks = (params?: any) => {
  return request.get<SubagentTaskVO[]>({ url: `${baseUrl}/subagent/recent-tasks`, params })
}

// 获取子代理详情
export const getSubagent = (id: number | string) => {
  return request.get<SubagentVO>({ url: `${baseUrl}/subagent/${id}` })
}

// 创建子代理
export const createSubagent = (data: SubagentCreateReqVO) => {
  return request.post({ url: `${baseUrl}/subagent/create`, data })
}

// 更新子代理
export const updateSubagent = (data: SubagentUpdateReqVO) => {
  return request.post({ url: `${baseUrl}/subagent/update`, data })
}

// 删除子代理
export const deleteSubagent = (id: number | string) => {
  return request.get({ url: `${baseUrl}/subagent/delete/${id}` })
}

// 查询子代理任务列表
export const listSubagentTasks = () => {
  return request.get<SubagentTaskVO[]>({ url: `${baseUrl}/subagentTask/list` })
}
