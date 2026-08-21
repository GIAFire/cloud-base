<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-width="100px"
    v-loading="formLoading"
  >
     <el-form-item label="模型接口协议：openaiCompatible、dashscope、anthropic、ollama" prop="protocol">
      <el-radio-group v-model="formData.protocol">
        <el-radio
          v-for="dict in getStrDictOptions(DICT_TYPE.PROTOCOL_TYPE)"
          :key="dict.value"
          :label="dict.value"
          >
          {{ dict.label }}
        </el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="兼容 OpenAI 协议时的自定义 baseUrl" prop="baseUrl">
      <el-input v-model="formData.baseUrl" placeholder="请输入兼容 OpenAI 协议时的自定义 baseUrl" />
    </el-form-item>
    <el-form-item label="API Key；当前阶段明文存储，仅模型详情接口允许返回" prop="apiKey">
      <el-input v-model="formData.apiKey" placeholder="请输入API Key；当前阶段明文存储，仅模型详情接口允许返回" />
    </el-form-item>
    <el-form-item label="模型说明" prop="description">
      <Editor v-model="formData.description" height="150px" />
    </el-form-item>
    <el-form-item label="真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5" prop="modelName">
      <el-input v-model="formData.modelName" placeholder="请输入真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5" />
    </el-form-item>
    <el-form-item label="是否支持流式" prop="streaming">
      <el-radio-group v-model="formData.streaming">
        <el-radio
          v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
          :key="dict.value"
          :label="dict.value"
          >
          {{ dict.label }}
        </el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="是否支持思考" prop="thinking">
      <el-radio-group v-model="formData.thinking">
        <el-radio
          v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
          :key="dict.value"
          :label="dict.value"
          >
          {{ dict.label }}
        </el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="采样温度，控制随机性" prop="temperature">
      <el-input v-model="formData.temperature" placeholder="请输入采样温度，控制随机性" />
    </el-form-item>
    <el-form-item label="核采样参数" prop="topP">
      <el-input v-model="formData.topP" placeholder="请输入核采样参数" />
    </el-form-item>
    <el-form-item label="最大输出token" prop="maxTokens">
      <el-input v-model="formData.maxTokens" placeholder="请输入最大输出token" />
    </el-form-item>
    <el-form-item label="最大思考token" prop="thinkingBudget">
      <el-input v-model="formData.thinkingBudget" placeholder="请输入最大思考token" />
    </el-form-item>
  </el-form>
</template>
<script setup lang="ts">
import { getStrDictOptions, getBoolDictOptions, DICT_TYPE } from '@/utils/dict'
import { AgentInfoApi } from '@/api/AiFriend/config'

const props = defineProps<{
  id: number // 模型配置主键ID（主表的关联字段）
}>()
const formLoading = ref(false) // 表单的加载中
const formData = ref<any>({})
const formRules = reactive({
  protocol: [{ required: true, message: '模型接口协议：openaiCompatible、dashscope、anthropic、ollama不能为空', trigger: 'blur' }],
  modelName: [{ required: true, message: '真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5不能为空', trigger: 'blur' }],
  streaming: [{ required: true, message: '是否支持流式不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.id,
  async (val) => {
    // 1. 重置表单
    formData.value = {
      id: undefined,
      protocol: undefined,
      baseUrl: undefined,
      apiKey: undefined,
      description: undefined,
      modelName: undefined,
      streaming: undefined,
      thinking: undefined,
      temperature: undefined,
      topP: undefined,
      maxTokens: undefined,
      thinkingBudget: undefined,
    }
    // 2. val 非空，则加载数据
    if (!val) {
      return;
    }
    try {
      formLoading.value = true
      const data = await AgentInfoApi.getModelById(val)
      if (!data) {
        return
      }
      formData.value = data
    } finally {
      formLoading.value = false
    }
  },
  { immediate: true }
)

/** 表单校验 */
const validate = () => {
  return formRef.value.validate()
}

/** 表单值 */
const getData = () => {
  return formData.value
}

defineExpose({ validate, getData })
</script>