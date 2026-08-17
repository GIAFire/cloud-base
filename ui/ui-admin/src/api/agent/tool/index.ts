import request from '@/config/axios'
import type {
  ToolInfoConfigVO,
  ToolGroupConfigVO,
  ToolCallLogVO,
  ToolRolePermissionVO,
  ToolMetricsVO,
  ToolCallLogPageReqVO,
  ToolPermissionPageReqVO,
  ToolPermissionSaveReqVO
} from './types'

/** 获取工具信息配置列表 */
export const listTools = (): Promise<ToolInfoConfigVO[]> => {
  return request.get({ url: '/agent/toolInfoConfig/list' })
}

/** 获取工具指标 */
export const getToolMetrics = (): Promise<ToolMetricsVO> => {
  return request.get({ url: '/agent/toolInfoConfig/metrics' })
}

/** 创建工具 */
export const createTool = (data: ToolInfoConfigVO) => {
  return request.post({ url: '/agent/toolInfoConfig/create', data })
}

/** 更新工具 */
export const updateTool = (data: ToolInfoConfigVO) => {
  return request.post({ url: '/agent/toolInfoConfig/update', data })
}

/** 获取工具分组列表 */
export const listToolGroups = (): Promise<ToolGroupConfigVO[]> => {
  return request.get({ url: '/agent/toolGroupConfig/list' })
}

/** 分页查询工具调用日志 */
export const pageToolCallLogs = (params: ToolCallLogPageReqVO) => {
  return request.get({ url: '/agent/toolCallLog/management/page', params })
}

/** 分页查询工具权限 */
export const pageToolPermissions = (params: ToolPermissionPageReqVO) => {
  return request.get({ url: '/agent/toolRolePermission/management/page', params })
}

/** 保存工具权限 */
export const saveToolPermission = (data: ToolPermissionSaveReqVO) => {
  return request.post({ url: '/agent/toolRolePermission/management/save', data })
}

/** 禁用工具权限 */
export const disableToolPermission = (id: number) => {
  return request.post({ url: `/agent/toolRolePermission/management/${id}/disable` })
}
