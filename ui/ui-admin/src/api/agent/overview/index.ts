import request from '@/config/axios'
import type { RunOverviewVO, RunOverviewParams, InteractionHistoryVO } from './types'

const baseUrl = '/agent/overview'

// 获取运行总览
export const getRunOverview = (params?: RunOverviewParams) => {
  return request.get<RunOverviewVO>({ url: baseUrl, params })
}

// 分页查询交互历史
export const getInteractionHistory = (params: PageParam) => {
  return request.get({ url: `${baseUrl}/interactions/page`, params })
}
