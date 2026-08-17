import request from '@/config/axios'
import type {
  PromptVO,
  PromptMetricsVO,
  PromptAnalyticsVO,
  PromptCreateReqVO,
  PromptUpdateReqVO
} from './types'

const baseUrl = '/agent/sysPrompt'

// 获取提示词指标
export const getPromptMetrics = () => {
  return request.get<PromptMetricsVO>({ url: `${baseUrl}/metrics` })
}

// 获取提示词分析数据
export const getPromptAnalytics = (limit: number = 5) => {
  return request.get<PromptAnalyticsVO>({ url: `${baseUrl}/analytics`, params: { limit } })
}

// 分页查询提示词
export const pagePrompts = (params: PageParam) => {
  return request.get({ url: `${baseUrl}/page`, params })
}

// 获取提示词详情
export const getPrompt = (id: number | string) => {
  return request.get<PromptVO>({ url: `${baseUrl}/${id}` })
}

// 创建提示词
export const createPrompt = (data: PromptCreateReqVO) => {
  return request.post({ url: `${baseUrl}/create`, data })
}

// 更新提示词
export const updatePrompt = (data: PromptUpdateReqVO) => {
  return request.post({ url: `${baseUrl}/update`, data })
}

// 删除提示词
export const deletePrompt = (id: number | string) => {
  return request.delete({ url: `${baseUrl}/${id}` })
}
