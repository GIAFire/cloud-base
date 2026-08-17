import request from '@/config/axios'
import type {
  KnowledgeBaseVO,
  KnowledgeBaseOptionVO,
  KnowledgeMetricsVO,
  KnowledgeDocumentVO,
  KnowledgeChunkVO,
  DocumentIndexReqVO,
  KnowledgeBaseCreateReqVO,
  KnowledgeBaseUpdateReqVO
} from './types'

const baseUrl = '/agent'

// 查询知识库列表
export const listKnowledgeBases = (params?: any) => {
  return request.get<KnowledgeBaseVO[]>({ url: `${baseUrl}/knowledgeBases`, params })
}

// 获取知识库指标
export const getKnowledgeMetrics = () => {
  return request.get<KnowledgeMetricsVO>({ url: `${baseUrl}/knowledgeBases/metrics` })
}

// 获取知识库详情
export const getKnowledgeBase = (knowledgeBaseId: number | string) => {
  return request.get<KnowledgeBaseVO>({ url: `${baseUrl}/knowledgeBases/${knowledgeBaseId}` })
}

// 获取知识库选项列表
export const listKnowledgeBaseOptions = () => {
  return request.get<KnowledgeBaseOptionVO[]>({ url: `${baseUrl}/knowledgeBases/options` })
}

// 创建知识库
export const createKnowledgeBase = (data: KnowledgeBaseCreateReqVO) => {
  return request.post({ url: `${baseUrl}/knowledgeBases`, data, timeout: 120000 })
}

// 更新知识库
export const updateKnowledgeBase = (knowledgeBaseId: number | string, data: KnowledgeBaseUpdateReqVO) => {
  return request.put({ url: `${baseUrl}/knowledgeBases/${knowledgeBaseId}`, data, timeout: 120000 })
}

// 删除知识库
export const deleteKnowledgeBase = (knowledgeBaseId: number | string) => {
  return request.delete({ url: `${baseUrl}/knowledgeBases/${knowledgeBaseId}`, timeout: 120000 })
}

// 查询知识文档列表
export const listKnowledgeDocuments = (knowledgeBaseId: number | string, params?: any) => {
  return request.get<KnowledgeDocumentVO[]>({ url: `${baseUrl}/knowledgeBases/${knowledgeBaseId}/documents`, params })
}

// 上传知识文档
export const uploadKnowledgeDocument = (knowledgeBaseId: number | string, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post({ url: `${baseUrl}/knowledgeBases/${knowledgeBaseId}/documents`, data: formData, timeout: 120000 })
}

// 删除知识文档
export const deleteKnowledgeDocument = (documentId: number | string) => {
  return request.delete({ url: `${baseUrl}/knowledgeDocuments/${documentId}` })
}

// 查询知识分块列表
export const listKnowledgeChunks = (documentId: number | string, params?: any) => {
  return request.get<KnowledgeChunkVO[]>({ url: `${baseUrl}/knowledgeDocuments/${documentId}/chunks`, params })
}

// 提交文档索引
export const submitDocumentIndex = (documentId: number | string, data: DocumentIndexReqVO) => {
  return request.post({ url: `${baseUrl}/knowledgeDocuments/${documentId}/index`, data })
}
