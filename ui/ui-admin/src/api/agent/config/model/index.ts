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

// 模型配置表 API
export const ModelApi = {
  // 查询模型配置表分页
  getModelPage: async (params: any) => {
    return await request.get({ url: `/agent/model/page`, params })
  },

  // 查询模型配置表详情
  getModel: async (id: number) => {
    return await request.get({ url: `/agent/model/get?id=` + id })
  },

  // 新增模型配置表
  createModel: async (data: Model) => {
    return await request.post({ url: `/agent/model/create`, data })
  },

  // 修改模型配置表
  updateModel: async (data: Model) => {
    return await request.put({ url: `/agent/model/update`, data })
  },

  // 删除模型配置表
  deleteModel: async (id: number) => {
    return await request.delete({ url: `/agent/model/delete?id=` + id })
  },

  /** 批量删除模型配置表 */
  deleteModelList: async (ids: number[]) => {
    return await request.delete({ url: `/agent/model/delete-list?ids=${ids.join(',')}` })
  },

  // 导出模型配置表 Excel
  exportModel: async (params) => {
    return await request.download({ url: `/agent/model/export-excel`, params })
  }
}