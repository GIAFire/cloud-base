// 知识库相关类型定义

/** 知识库信息 */
export interface KnowledgeBaseVO {
  id: number
  name: string
  description?: string
  embeddingModelId?: number
  embeddingModelName?: string
  documentCount?: number
  chunkCount?: number
  status?: number
  createTime?: Date
  updateTime?: Date
}

/** 知识库选项 */
export interface KnowledgeBaseOptionVO {
  id: number
  name: string
}

/** 知识库指标 */
export interface KnowledgeMetricsVO {
  totalKnowledgeBases: number
  totalDocuments: number
  totalChunks: number
  activeKnowledgeBases: number
}

/** 知识文档信息 */
export interface KnowledgeDocumentVO {
  id: number
  knowledgeBaseId: number
  name: string
  type?: string
  size?: number
  chunkCount?: number
  status?: number
  createTime?: Date
}

/** 知识分块信息 */
export interface KnowledgeChunkVO {
  id: number
  documentId: number
  content: string
  metadata?: Record<string, any>
  embedding?: number[]
  createTime?: Date
}

/** 文档索引请求 */
export interface DocumentIndexReqVO {
  chunkSize?: number
  chunkOverlap?: number
  separator?: string
}

/** 知识库创建请求 */
export interface KnowledgeBaseCreateReqVO {
  name: string
  description?: string
  embeddingModelId?: number
}

/** 知识库更新请求 */
export interface KnowledgeBaseUpdateReqVO {
  id: number
  name?: string
  description?: string
  embeddingModelId?: number
}
