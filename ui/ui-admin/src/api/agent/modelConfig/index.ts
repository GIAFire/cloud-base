import request from '@/config/axios'
import type {
  ModelConfigVO,
  ModelCallLogVO,
  ModelMetricsVO,
  ModelAnalyticsVO,
  ModelConfigPageReqVO,
  ModelCallLogPageReqVO,
  ModelConfigTestReqVO
} from './types'

/** 获取模型指标 */
export const getModelMetrics = (): Promise<ModelMetricsVO> => {
  return request.get({ url: '/agent/modelConfig/metrics' })
}

/** 获取模型分析数据 */
export const getModelAnalytics = (days: number = 7): Promise<ModelAnalyticsVO> => {
  return request.get({ url: '/agent/modelConfig/analytics', params: { days } })
}

/** 获取模型配置列表 */
export const listModelConfig = (): Promise<ModelConfigVO[]> => {
  return request.get({ url: '/agent/modelConfig/list' })
}

/** 分页查询模型配置列表 */
export const pageModelConfig = (params: ModelConfigPageReqVO) => {
  return request.get({ url: '/agent/modelConfig/page', params })
}

/** 获取模型配置详情 */
export const getModelConfig = (id: number): Promise<ModelConfigVO> => {
  return request.get({ url: `/agent/modelConfig/${id}` })
}

/** 添加模型配置 */
export const addModelConfig = (data: ModelConfigVO) => {
  return request.post({ url: '/agent/modelConfig/create', data })
}

/** 更新模型配置 */
export const updateModelConfig = (data: ModelConfigVO) => {
  return request.post({ url: '/agent/modelConfig/update', data })
}

/** 删除模型配置 */
export const deleteModelConfig = (id: number) => {
  return request.delete({ url: `/agent/modelConfig/${id}` })
}

/** 测试模型配置 */
export const testModelConfig = (data: ModelConfigTestReqVO) => {
  return request.post({ url: '/agent/modelConfig/test', data, timeout: 620000 })
}

/** 分页查询模型调用日志 */
export const pageModelCallLogs = (params: ModelCallLogPageReqVO) => {
  return request.get({ url: '/agent/modelConfig/logs/page', params })
}
