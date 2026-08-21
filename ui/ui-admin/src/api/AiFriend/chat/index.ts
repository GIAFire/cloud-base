import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** Agent消息表：保存输入、思考、正文等消息信息 */
export interface Message {
          id: number; // 消息主键ID
          sessionId?: number; // 会话ID，关联ai_agent_session.id
          seq?: number; // 会话内全局消息序号，用于历史恢复和稳定排序
          role?: string; // 协议角色：USER、ASSISTANT、SYSTEM、TOOL
          messageType?: string; // 消息类型：USER_TEXT、ASSISTANT_THINKING、ASSISTANT_TEXT、TOOL_CALL、TOOL_RESULT、SKILL_CALL、SKILL_RESULT、SUBAGENT_CALL、SUBAGENT_RESULT、PLAN_SNAPSHOT、PLAN_OPERATION、SYSTEM_NOTICE、ERROR
          textContent: string; // 可搜索和直接展示的文本内容
          usageJson: string; // Token用量，例如inputTokens、outputTokens、reasoningTokens、totalTokens
          durationMs: number; // 耗时，单位毫秒
}

/** Agent会话表：保存用户和Agent之间的一段连续对话信息 */
export interface Session {
          id: number; // 会话主键ID
          agentId?: number; // Agent ID，关联智能体定义表
          userId?: number; // 用户ID，表示该会话归属哪个登录用户
          title: string; // 会话标题，可由第一条用户消息或大模型生成
            messages?: Message[]
}

// Agent会话表：保存用户和Agent之间的一段连续对话 API
export const SessionApi = {
  // 查询Agent会话表：保存用户和Agent之间的一段连续对话分页
  getSessionPage: async (params: any) => {
    return await request.get({ url: `/AiFriend/session/page`, params })
  },

  // 查询Agent会话表：保存用户和Agent之间的一段连续对话详情
  getSession: async (id: number) => {
    return await request.get({ url: `/AiFriend/session/get?id=` + id })
  },

  // 新增Agent会话表：保存用户和Agent之间的一段连续对话
  createSession: async (data: Session) => {
    return await request.post({ url: `/AiFriend/session/create`, data })
  },

  // 修改Agent会话表：保存用户和Agent之间的一段连续对话
  updateSession: async (data: Session) => {
    return await request.put({ url: `/AiFriend/session/update`, data })
  },

  // 删除Agent会话表：保存用户和Agent之间的一段连续对话
  deleteSession: async (id: number) => {
    return await request.delete({ url: `/AiFriend/session/delete?id=` + id })
  },

  /** 批量删除Agent会话表：保存用户和Agent之间的一段连续对话 */
  deleteSessionList: async (ids: number[]) => {
    return await request.delete({ url: `/AiFriend/session/delete-list?ids=${ids.join(',')}` })
  },

  // 导出Agent会话表：保存用户和Agent之间的一段连续对话 Excel
  exportSession: async (params) => {
    return await request.download({ url: `/AiFriend/session/export-excel`, params })
  },

// ==================== 子表（Agent消息表：保存输入、思考、正文等消息） ====================

  // 获得Agent消息表：保存输入、思考、正文等消息列表
  getMessageListBySessionId: async (sessionId) => {
    return await request.get({ url: `/AiFriend/session/message/list-by-session-id?sessionId=` + sessionId })
  }
}