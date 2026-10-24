import request from '@/config/axios'

/** Agent 绑定的分类或标签摘要 */
export interface InfoTaxonomy {
  id: number
  name: string
  sort?: number
  status?: number
}

/** Agent 定义表信息 */
export interface Info {
  id: number // Agent 主键ID
  modelId: number // 关联 ai_agent_model.id
  sysPromptId: number // 关联 ai_agent_sys_prompt.id
  agentCode?: string // Agent唯一编码
  agentName?: string // Agent 显示名称
  agentKind?: 'CHAT' | 'CUSTOMER_SERVICE' // Agent 用途
  description: string // Agent 描述
  personality?: string // Agent 人格设定
  maxIters?: number // 最大循环次数
  permissionMode?: string // 默认权限模式
  compactionEnabled?: boolean // 是否启用上下文压缩：1启用，0关闭
  triggerMessages: number // 触发压缩消息数
  keepMessages: number // 压缩后保留消息数
  triggerTokens: number // 触发压缩token数
  keepTokens: number // 压缩后保留token数
  toolResultEvictionEnabled?: boolean // 是否启用大工具结果卸载：1启用，0关闭
  memoryEnable: boolean // 是否启用长期记忆:1启用,0禁用
  planModeEnabled?: boolean // 是否启用Plan Mode：1启用，0关闭
  allowShellInPlanMode?: boolean // Plan阶段是否允许shell工具：1允许，0禁止；生产环境建议关闭
  categoryIds?: number[] // 绑定的分类 ID
  tagIds?: number[] // 绑定的标签 ID
  categories?: InfoTaxonomy[] // 分类摘要，包含已禁用但仍绑定的分类
  tags?: InfoTaxonomy[] // 标签摘要，包含已禁用但仍绑定的标签
  createTime?: string // 创建时间
}

// Agent 定义表 API
export const InfoApi = {
  getPersonality: async (id: number | string) => {
    return await request.get({ url: `/agent/info/getPersonality?id=` + id })
  },
  // 查询Agent 定义表分页
  getInfoPage: async (params: any) => {
    return await request.get<PageResult<Info[]>>({ url: `/agent/info/page`, params })
  },

  // 查询Agent 定义表详情
  getInfo: async (id: number | string) => {
    return await request.get<Info>({ url: `/agent/info/get?id=` + id })
  },

  // 新增Agent 定义表
  createInfo: async (data: Info) => {
    return await request.post({ url: `/agent/info/create`, data })
  },

  // 修改Agent 定义表
  updateInfo: async (data: Info) => {
    return await request.put({ url: `/agent/info/update`, data })
  },

  // 删除Agent 定义表
  deleteInfo: async (id: number) => {
    return await request.delete({ url: `/agent/info/delete?id=` + id })
  },

  /** 批量删除Agent 定义表 */
  deleteInfoList: async (ids: number[]) => {
    return await request.delete({ url: `/agent/info/delete-list?ids=${ids.join(',')}` })
  },

  // 导出Agent 定义表 Excel
  exportInfo: async (params) => {
    return await request.download({ url: `/agent/info/export-excel`, params })
  }
}
