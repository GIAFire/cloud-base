<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="接口协议" prop="protocol">
        <el-select
          v-model="formData.protocol"
          clearable
          placeholder="请选择模型接口协议"
          class="!w-100%"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.AGENT_MODEL_PROTOCOL)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="baseUrl" prop="baseUrl">
        <el-input v-model="formData.baseUrl" placeholder="请输入 baseUrl" />
      </el-form-item>
      <el-form-item label="API Key" prop="apiKey">
        <el-input v-model="formData.apiKey" placeholder="请输入API Key" />
      </el-form-item>
      <el-form-item label="模型说明" prop="description">
        <Editor v-model="formData.description" height="150px" />
      </el-form-item>
      <el-form-item label="模型名称" prop="modelName">
        <el-input v-model="formData.modelName" placeholder="请输入模型名称" />
      </el-form-item>
      <el-form-item label="是否流式" prop="streaming">
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
      <el-form-item label="是否思考" prop="thinking">
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
      <el-form-item label="采样温度" prop="temperature">
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
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { getStrDictOptions, getBoolDictOptions, DICT_TYPE } from '@/utils/dict'
import { ModelApi, Model } from '@/api/agent/config/model'

/** 模型配置表 表单 */
defineOptions({ name: 'ModelForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
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
  thinkingBudget: undefined
})
const formRules = reactive({
  protocol: [{ required: true, message: '模型接口协议：openaiCompatible、dashscope、anthropic、ollama不能为空', trigger: 'blur' }],
  modelName: [{ required: true, message: '真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5不能为空', trigger: 'blur' }],
  streaming: [{ required: true, message: '是否支持流式不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ModelApi.getModel(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as Model
    if (formType.value === 'create') {
      await ModelApi.createModel(data)
      message.success(t('common.createSuccess'))
    } else {
      await ModelApi.updateModel(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
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
    thinkingBudget: undefined
  }
  formRef.value?.resetFields()
}
</script>
