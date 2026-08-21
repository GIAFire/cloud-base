import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 模型配置表信息 */
export interface Model {
          id: number; // 模型配置主键ID
          protocol?: string; // 模型接口协议：openaiCompatible、dashscope、anthropic、ollama
          baseUrl: string; // 兼容 OpenAI 协议时的自定义 baseUrl
          apiKey: string; // API Key；当前阶段明文存储，仅模型详情接口允许返回
          description: string; // 模型说明
          modelName?: string; // 真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5
          streaming?: boolean; // 是否支持流式
          thinking: boolean; // 是否支持思考
          temperature: number; // 采样温度，控制随机性
          topP: number; // 核采样参数
          maxTokens: number; // 最大输出token
          thinkingBudget: number; // 最大思考token
}

/** 系统提示词表信息 */
export interface SysPrompt {
          id: number; // 提示词模板主键ID
          promptName?: string; // 提示词模板名称
          description?: string; // 描述
          sysPrompt: string; // 提示词内容
}

/** Agent 定义表信息 */
export interface AgentInfo {
          modelId: number; // 关联 ai_agent_model.id
          sysPromptId: number; // 关联 ai_agent_sys_prompt.id
          agentCode?: string; // Agent唯一编码
          agentName?: string; // Agent 显示名称
          description: string; // Agent 描述
          maxIters?: number; // 最大循环次数
          permissionMode?: string; // 默认权限模式
          compactionEnabled?: boolean; // 是否启用上下文压缩：1启用，0关闭
          triggerMessages: number; // 触发压缩消息数
          keepMessages: number; // 压缩后保留消息数
          triggerTokens: number; // 触发压缩token数
          keepTokens: number; // 压缩后保留token数
          toolResultEvictionEnabled?: boolean; // 是否启用大工具结果卸载：1启用，0关闭
          memoryEnable: boolean; // 是否启用长期记忆:1启用,0禁用
          planModeEnabled?: boolean; // 是否启用Plan Mode：1启用，0关闭
          allowShellInPlanMode?: boolean; // Plan阶段是否允许shell工具：1允许，0禁止；生产环境建议关闭
            model?: Model
          sysprompt?: SysPrompt
}

// Agent 定义表 API
export const AgentInfoApi = {
  // 查询Agent 定义表分页
  getAgentInfoPage: async (params: any) => {
    return await request.get({ url: `/AiFriend/agent-info/page`, params })
  },

  // 查询Agent 定义表详情
  getAgentInfo: async (id: number) => {
    return await request.get({ url: `/AiFriend/agent-info/get?id=` + id })
  },

  // 新增Agent 定义表
  createAgentInfo: async (data: AgentInfo) => {
    return await request.post({ url: `/AiFriend/agent-info/create`, data })
  },

  // 修改Agent 定义表
  updateAgentInfo: async (data: AgentInfo) => {
    return await request.put({ url: `/AiFriend/agent-info/update`, data })
  },

  // 删除Agent 定义表
  deleteAgentInfo: async (id: number) => {
    return await request.delete({ url: `/AiFriend/agent-info/delete?id=` + id })
  },

  /** 批量删除Agent 定义表 */
  deleteAgentInfoList: async (ids: number[]) => {
    return await request.delete({ url: `/AiFriend/agent-info/delete-list?ids=${ids.join(',')}` })
  },

  // 导出Agent 定义表 Excel
  exportAgentInfo: async (params) => {
    return await request.download({ url: `/AiFriend/agent-info/export-excel`, params })
  },

// ==================== 子表（模型配置表） ====================

  // 获得模型配置表
  getModelById: async (id) => {
    return await request.get({ url: `/AiFriend/agent-info/model/get-by-id?id=` + id })
  },

// ==================== 子表（系统提示词表） ====================

  // 获得系统提示词表
  getSysPromptById: async (id) => {
    return await request.get({ url: `/AiFriend/agent-info/sys-prompt/get-by-id?id=` + id })
  }
}