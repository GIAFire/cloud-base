import request from '@/config/axios'

/** Agent 定义表信息 */
export interface Info {
  id: number // Agent 主键ID
  modelId: number // 关联 ai_agent_model.id
  sysPromptId: number // 关联 ai_agent_sys_prompt.id
  agentCode?: string // Agent唯一编码
  agentName?: string // Agent 显示名称
  description: string // Agent 描述
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
}

// Agent 定义表 API
export const InfoApi = {
  // 查询Agent 定义表分页
  getInfoPage: async (params: any) => {
    return await request.get({ url: `/agent/info/page`, params })
  },

  // 查询Agent 定义表详情
  getInfo: async (id: number | string) => {
    return await request.get({ url: `/agent/info/get?id=` + id })
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
