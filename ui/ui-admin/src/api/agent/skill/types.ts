// 技能管理相关类型定义

/** 技能信息 */
export interface SkillVO {
  id: number
  name: string
  description?: string
  type?: string
  category?: string
  tags?: string[]
  version?: string
  status?: number
  config?: Record<string, any>
  createTime?: Date
  updateTime?: Date
}

/** 技能指标 */
export interface SkillMetricsVO {
  totalSkills: number
  activeSkills: number
  totalExecutions: number
  successRate: number
}

/** 技能文件信息 */
export interface SkillFileVO {
  id: number
  skillId: number
  name: string
  path: string
  type: 'file' | 'folder'
  content?: string
  size?: number
  createTime?: Date
  updateTime?: Date
}

/** 技能日志 */
export interface SkillLogVO {
  id: number
  skillId: number
  skillName?: string
  input?: string
  output?: string
  status?: number
  errorMessage?: string
  duration?: number
  createTime?: Date
}

/** 技能创建请求 */
export interface SkillCreateReqVO {
  name: string
  description?: string
  type?: string
  category?: string
  tags?: string[]
  config?: Record<string, any>
}

/** 技能更新请求 */
export interface SkillUpdateReqVO {
  id: number
  name?: string
  description?: string
  type?: string
  category?: string
  tags?: string[]
  config?: Record<string, any>
}

/** 技能文件创建请求 */
export interface SkillFileCreateReqVO {
  skillId: number
  name: string
  path: string
  type: 'file' | 'folder'
  content?: string
}

/** 技能文件更新请求 */
export interface SkillFileUpdateReqVO {
  id: number
  name?: string
  content?: string
}
