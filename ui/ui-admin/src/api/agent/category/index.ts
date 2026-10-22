import request from '@/config/axios'

/** Agent 分类 */
export interface AgentCategory {
  id?: number
  name: string
  sort: number
  status: number
  createTime?: string
}

/** Agent 分类精简信息 */
export type AgentCategorySimple = Pick<AgentCategory, 'id' | 'name' | 'sort' | 'status'> & {
  id: number
}

export interface AgentCategoryPageQuery {
  pageNo: number
  pageSize: number
  name?: string
  status?: number
  createTime?: string[]
}

export const CategoryApi = {
  getCategoryPage: (params: AgentCategoryPageQuery) =>
    request.get<PageResult<AgentCategory[]>>({ url: '/agent/category/page', params }),

  getCategory: (id: number) => request.get<AgentCategory>({ url: `/agent/category/get?id=${id}` }),

  getCategorySimpleList: () =>
    request.get<AgentCategorySimple[]>({ url: '/agent/category/simple-list' }),

  createCategory: (data: AgentCategory) =>
    request.post<number>({ url: '/agent/category/create', data }),

  updateCategory: (data: AgentCategory) =>
    request.put<boolean>({ url: '/agent/category/update', data }),

  deleteCategory: (id: number) =>
    request.delete<boolean>({ url: `/agent/category/delete?id=${id}` })
}
