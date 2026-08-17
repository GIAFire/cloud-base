// 系统提示词相关类型定义

/** 系统提示词信息 */
export interface PromptVO {
  id: number
  promptName: string
  description?: string
  sysPrompt: string
  version?: number
  bindingCount?: number
  contentLength?: number
  contentPreview?: string
  createdAt?: string
  updatedAt?: string
}

/** 提示词指标 */
export interface PromptMetricsVO {
  total: number
  newToday: number
  boundPrompts: number
  boundAgents: number
}

/** 绑定排行项 */
export interface BindingRankingItem {
  id: number
  promptName: string
  bindingCount: number
}

/** 最近更新项 */
export interface RecentlyUpdatedItem {
  id: number
  promptName: string
  contentLength: number
  updatedAt: string
}

/** 提示词分析数据 */
export interface PromptAnalyticsVO {
  bindingRanking: BindingRankingItem[]
  recentlyUpdated: RecentlyUpdatedItem[]
}

/** 提示词创建请求 */
export interface PromptCreateReqVO {
  promptName: string
  description?: string
  sysPrompt: string
}

/** 提示词更新请求 */
export interface PromptUpdateReqVO {
  id: number
  promptName?: string
  description?: string
  sysPrompt?: string
  version?: number
}
