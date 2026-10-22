import request from '@/config/axios'

/** Agent 标签 */
export interface AgentTag {
  id?: number
  name: string
  sort: number
  status: number
  createTime?: string
}

/** Agent 标签精简信息 */
export type AgentTagSimple = Pick<AgentTag, 'id' | 'name' | 'sort' | 'status'> & {
  id: number
}

export interface AgentTagPageQuery {
  pageNo: number
  pageSize: number
  name?: string
  status?: number
  createTime?: string[]
}

export const TagApi = {
  getTagPage: (params: AgentTagPageQuery) =>
    request.get<PageResult<AgentTag[]>>({ url: '/agent/tag/page', params }),

  getTag: (id: number) => request.get<AgentTag>({ url: `/agent/tag/get?id=${id}` }),

  getTagSimpleList: () => request.get<AgentTagSimple[]>({ url: '/agent/tag/simple-list' }),

  createTag: (data: AgentTag) => request.post<number>({ url: '/agent/tag/create', data }),

  updateTag: (data: AgentTag) => request.put<boolean>({ url: '/agent/tag/update', data }),

  deleteTag: (id: number) => request.delete<boolean>({ url: `/agent/tag/delete?id=${id}` })
}
