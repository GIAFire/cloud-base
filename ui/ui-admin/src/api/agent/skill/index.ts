import request from '@/config/axios'
import type {
  SkillVO,
  SkillMetricsVO,
  SkillFileVO,
  SkillLogVO,
  SkillCreateReqVO,
  SkillUpdateReqVO,
  SkillFileCreateReqVO,
  SkillFileUpdateReqVO
} from './types'

const baseUrl = '/agent'

// 查询技能列表
export const listSkills = () => {
  return request.get<SkillVO[]>({ url: `${baseUrl}/skillInfo/list` })
}

// 分页查询技能
export const pageSkills = (params: PageParam) => {
  return request.get({ url: `${baseUrl}/skillInfo/page`, params })
}

// 获取技能指标
export const getSkillMetrics = () => {
  return request.get<SkillMetricsVO>({ url: `${baseUrl}/skillInfo/metrics` })
}

// 获取技能详情
export const getSkill = (id: number | string) => {
  return request.get<SkillVO>({ url: `${baseUrl}/skillInfo/${id}` })
}

// 创建技能
export const createSkill = (data: any) => {
  // 字段映射：前端 skillCode -> 后端 skillKey，roleCodes -> roleIds
  const payload = {
    ...data,
    skillKey: data.skillCode || data.skillKey,
    skillName: data.skillName || data.name,
    roleIds: data.roleCodes || data.roleIds || ['0'],
    skillCode: undefined,
    roleCodes: undefined,
    name: undefined
  }
  return request.post({ url: `${baseUrl}/skillInfo/create`, data: payload })
}

// 更新技能
export const updateSkill = (data: any) => {
  // 字段映射：前端 skillCode -> 后端 skillKey，roleCodes -> roleIds
  const payload = {
    ...data,
    skillKey: data.skillCode || data.skillKey,
    skillName: data.skillName || data.name,
    roleIds: data.roleCodes || data.roleIds || ['0'],
    skillCode: undefined,
    roleCodes: undefined,
    name: undefined
  }
  return request.post({ url: `${baseUrl}/skillInfo/update`, data: payload })
}

// 删除技能
export const deleteSkill = (id: number | string) => {
  return request.delete({ url: `${baseUrl}/skillInfo/${id}` })
}

// 查询技能文件列表
export const listSkillFilesBySkill = (skillId: number | string) => {
  return request.get<SkillFileVO[]>({ url: `${baseUrl}/skillResource/skill/${skillId}` })
}

// 获取技能文件内容
export const getSkillFileContent = (id: number | string) => {
  return request.get<string>({ url: `${baseUrl}/skillResource/content/${id}` })
}

// 创建技能文件节点
export const createSkillPackageNode = (data: SkillFileCreateReqVO) => {
  return request.post({ url: `${baseUrl}/skillResource/create`, data })
}

// 更新技能文件
export const updateSkillPackageFile = (data: SkillFileUpdateReqVO) => {
  return request.post({ url: `${baseUrl}/skillResource/update`, data })
}

// 删除技能文件节点
export const deleteSkillPackageNode = (id: number | string) => {
  return request.delete({ url: `${baseUrl}/skillResource/${id}` })
}

// 删除技能文件夹
export const deleteSkillPackageFolder = (skillId: number | string, path: string) => {
  return request.delete({ url: `${baseUrl}/skillResource/folder`, params: { skillId, path } })
}

// 分页查询技能日志
export const pageSkillLogs = (params: PageParam) => {
  return request.get({ url: `${baseUrl}/skillInfo/logs/page`, params })
}

// 查询最近技能日志
export const listRecentSkillLogs = (params?: any) => {
  return request.get<SkillLogVO[]>({ url: `${baseUrl}/skillInfo/logs/recent`, params })
}
