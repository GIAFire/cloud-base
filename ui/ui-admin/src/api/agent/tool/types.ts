/** 工具信息配置 */
export interface ToolInfoConfigVO {
  id: number
  name: string
  description?: string
  groupId?: number
  groupName?: string
  type: string
  config?: string
  status: number
  createTime: Date
  updateTime?: Date
}

/** 工具分组配置 */
export interface ToolGroupConfigVO {
  id: number
  name: string
  description?: string
  sort?: number
  createTime: Date
}

/** 工具调用日志 */
export interface ToolCallLogVO {
  id: number
  toolId: number
  toolName?: string
  agentId?: number
  agentName?: string
  input?: string
  output?: string
  status: number
  duration: number
  errorMessage?: string
  createTime: Date
}

/** 工具权限 */
export interface ToolRolePermissionVO {
  id: number
  toolId: number
  toolName?: string
  roleId: number
  roleName?: string
  enabled: boolean
  createTime: Date
}

/** 工具指标 */
export interface ToolMetricsVO {
  totalTools: number
  activeTools: number
  totalCalls: number
  successCalls: number
  failedCalls: number
  avgDuration?: number
}

/** 工具调用日志分页查询参数 */
export interface ToolCallLogPageReqVO {
  pageNo: number
  pageSize: number
  toolId?: number
  agentId?: number
  status?: number
  startTime?: Date
  endTime?: Date
}

/** 工具权限分页查询参数 */
export interface ToolPermissionPageReqVO {
  pageNo: number
  pageSize: number
  toolId?: number
  roleId?: number
}

/** 工具权限保存参数 */
export interface ToolPermissionSaveReqVO {
  toolId: number
  roleIds: number[]
}
