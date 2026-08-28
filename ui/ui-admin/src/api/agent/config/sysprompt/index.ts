import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 系统提示词表信息 */
export interface SysPrompt {
          id: number; // 提示词模板主键ID
          promptName?: string; // 提示词模板名称
          description?: string; // 描述
          sysPrompt: string; // 提示词内容
  }

// 系统提示词表 API
export const SysPromptApi = {
  // 查询系统提示词表分页
  getSysPromptPage: async (params: any) => {
    return await request.get({ url: `/agent/sys-prompt/page`, params })
  },

  // 查询系统提示词表详情
  getSysPrompt: async (id: number) => {
    return await request.get({ url: `/agent/sys-prompt/get?id=` + id })
  },

  // 新增系统提示词表
  createSysPrompt: async (data: SysPrompt) => {
    return await request.post({ url: `/agent/sys-prompt/create`, data })
  },

  // 修改系统提示词表
  updateSysPrompt: async (data: SysPrompt) => {
    return await request.put({ url: `/agent/sys-prompt/update`, data })
  },

  // 删除系统提示词表
  deleteSysPrompt: async (id: number) => {
    return await request.delete({ url: `/agent/sys-prompt/delete?id=` + id })
  },

  /** 批量删除系统提示词表 */
  deleteSysPromptList: async (ids: number[]) => {
    return await request.delete({ url: `/agent/sys-prompt/delete-list?ids=${ids.join(',')}` })
  },

  // 导出系统提示词表 Excel
  exportSysPrompt: async (params) => {
    return await request.download({ url: `/agent/sys-prompt/export-excel`, params })
  }
}