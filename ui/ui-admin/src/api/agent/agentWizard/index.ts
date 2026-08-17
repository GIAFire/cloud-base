import request from '@/config/axios'

const baseUrl = '/agent'

// 获取向导模型列表
export const listWizardModels = () => {
  return request.get({ url: `${baseUrl}/modelConfig/list` })
}

// 获取向导提示词列表
export const listWizardPrompts = () => {
  return request.get({ url: `${baseUrl}/sysPrompt/list` })
}

// 获取向导工具列表
export const listWizardTools = () => {
  return request.get({ url: `${baseUrl}/toolInfoConfig/list` })
}

// 获取向导知识库列表
export const listWizardKnowledgeBases = () => {
  return request.get({ url: `${baseUrl}/knowledgeBases/options` })
}

// 获取向导子智能体列表
export const listWizardSubagents = () => {
  return request.get({ url: `${baseUrl}/subagent/list` })
}
