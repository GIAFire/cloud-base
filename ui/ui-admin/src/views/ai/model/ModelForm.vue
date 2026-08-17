<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="min(760px, 94vw)">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-position="top"
    >
      <!-- 身份与连接 -->
      <el-divider content-position="left">
        <span class="text-14px font-600">身份与连接</span>
      </el-divider>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="配置名称" prop="configName">
            <el-input v-model="formData.configName" maxlength="100" placeholder="例如：生产环境 GPT-4.1" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模型供应商" prop="providerName">
            <el-input v-model="formData.providerName" maxlength="100" placeholder="例如：OpenAI、阿里云、本地部署" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="接口协议" prop="protocol">
            <el-select v-model="formData.protocol" placeholder="请选择接口协议">
              <el-option
                v-for="item in protocols"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模型名称" prop="modelName">
            <el-input v-model="formData.modelName" maxlength="200" placeholder="例如：gpt-4.1、qwen-max" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="24">
          <el-form-item label="Base URL" prop="baseURL">
            <el-input v-model="formData.baseURL" placeholder="https://api.example.com/v1" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="24">
          <el-form-item label="API Key（可选）">
            <el-input
              v-model="formData.apiKey"
              :type="showApiKey ? 'text' : 'password'"
              autocomplete="new-password"
              placeholder="本地模型或 Header 鉴权可留空"
              @input="formData.removeApiKey = false"
            >
              <template #suffix>
                <el-icon class="cursor-pointer" @click="showApiKey = !showApiKey">
                  <component :is="showApiKey ? Hide : View" />
                </el-icon>
              </template>
            </el-input>
            <el-button v-if="formData.id && formData.apiKey" link type="danger" @click="clearApiKey">
              清空密钥
            </el-button>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="24">
          <el-form-item label="描述">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="说明使用场景、环境或限制"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 生成参数 -->
      <el-divider content-position="left">
        <span class="text-14px font-600">生成参数</span>
      </el-divider>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="流式输出">
            <el-switch v-model="formData.streaming" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="思考模式">
            <el-switch v-model="formData.thinking" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="配置状态">
            <el-radio-group v-model="formData.status">
              <el-radio-button :value="1">启用</el-radio-button>
              <el-radio-button :value="0">停用</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="Temperature">
            <el-input-number v-model="formData.temperature" :min="0" :max="2" :step="0.1" :precision="2" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Top P">
            <el-input-number v-model="formData.topP" :min="0" :max="1" :step="0.05" :precision="2" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="最大输出 Token" prop="maxTokens">
            <el-input-number v-model="formData.maxTokens" :min="1" :max="10000000" :step="256" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item v-if="Number(formData.thinking) === 1" label="思考预算 Token">
            <el-input-number v-model="formData.thinkingBudget" :min="1" :max="10000000" :step="256" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="超时时间（毫秒）" prop="timeoutMs">
            <el-input-number v-model="formData.timeoutMs" :min="1000" :max="600000" :step="1000" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大尝试次数（含首次）" prop="maxAttempts">
            <el-input-number v-model="formData.maxAttempts" :min="1" :max="10" />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 自定义 Header -->
      <el-divider content-position="left">
        <div class="flex justify-between items-center">
          <span class="text-14px font-600">自定义 Header</span>
          <el-button :icon="Plus" size="small" @click="addHeader">新增 Header</el-button>
        </div>
      </el-divider>
      <div v-if="formData.headers.length" class="space-y-10px">
        <div
          v-for="(header, index) in formData.headers"
          :key="header.id || index"
          class="flex gap-10px items-center"
          :class="{ 'p-11px rounded-10px bg-red-50': header.remove }"
        >
          <template v-if="!header.remove">
            <el-input v-model="header.headerName" placeholder="Header 名称" class="flex-1" />
            <el-input
              v-model="header.headerValue"
              :type="header.visible ? 'text' : 'password'"
              autocomplete="new-password"
              placeholder="Header 值"
              class="flex-1"
            >
              <template #suffix>
                <el-icon class="cursor-pointer" @click="header.visible = !header.visible">
                  <component :is="header.visible ? Hide : View" />
                </el-icon>
              </template>
            </el-input>
            <el-button circle text type="danger" :icon="Delete" @click="removeHeader(header, index)" />
          </template>
          <template v-else>
            <span class="flex-1 text-red-600">{{ header.headerName }} 将在保存时删除</span>
            <el-button link type="primary" @click="restoreHeader(header)">撤销</el-button>
          </template>
        </div>
      </div>
      <el-empty v-else description="未配置自定义 Header" :image-size="64" />
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button :loading="testing" @click="handleTest">测试连接</el-button>
      <el-button type="primary" :loading="formLoading" @click="submitForm">保存配置</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref, reactive, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Hide, Key, Plus, View } from '@element-plus/icons-vue'
import * as ModelConfigApi from '@/api/agent/modelConfig'
import type { FormRules } from 'element-plus'

defineOptions({ name: 'ModelForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const showApiKey = ref(false)
const testing = ref(false)
const formRef = ref()

const protocols = [
  { value: 'openaiCompatible', label: 'OpenAI 兼容协议' },
  { value: 'dashscope', label: 'DashScope' },
  { value: 'anthropic', label: 'Anthropic' },
  { value: 'ollama', label: 'Ollama' }
]

const createForm = () => ({
  id: null,
  configName: '',
  providerName: '',
  protocol: 'openaiCompatible',
  baseURL: '',
  apiKey: '',
  removeApiKey: false,
  description: '',
  modelName: '',
  streaming: 1,
  thinking: 0,
  temperature: 0.7,
  topP: 1,
  maxTokens: 4096,
  timeoutMs: 60000,
  thinkingBudget: null,
  maxAttempts: 1,
  status: 1,
  headers: []
})

const formData = ref(createForm())

const formRules = reactive<FormRules>({
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  providerName: [{ required: true, message: '请输入模型供应商', trigger: 'blur' }],
  protocol: [{ required: true, message: '请选择接口协议', trigger: 'change' }],
  baseURL: [
    { required: true, message: '请输入 Base URL', trigger: 'blur' },
    { type: 'url', message: '请输入合法的 HTTP/HTTPS 地址', trigger: 'blur' }
  ],
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  maxTokens: [{ required: true, message: '请输入最大输出 Token', trigger: 'change' }],
  timeoutMs: [{ required: true, message: '请输入超时时间', trigger: 'change' }],
  maxAttempts: [{ required: true, message: '请输入最大尝试次数', trigger: 'change' }]
})

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新建模型配置' : '编辑模型配置'
  formType.value = type
  resetForm()
  
  if (id) {
    formLoading.value = true
    try {
      const detail = await ModelConfigApi.getModelConfig(id)
      formData.value = {
        ...createForm(),
        ...detail,
        id: detail?.id || id,
        apiKey: detail?.apiKey || '',
        headers: (detail?.headers || []).map((header: any) => ({
          ...header,
          remove: false,
          visible: false
        }))
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

/** 重置表单 */
const resetForm = () => {
  formData.value = createForm()
  showApiKey.value = false
  formRef.value?.resetFields()
}

/** 添加 Header */
const addHeader = () => {
  formData.value.headers.push({
    id: null,
    headerName: '',
    headerValue: '',
    remove: false,
    visible: false
  })
}

/** 删除 Header */
const removeHeader = (header: any, index: number) => {
  if (header.id) {
    header.remove = true
    return
  }
  formData.value.headers.splice(index, 1)
}

/** 恢复 Header */
const restoreHeader = (header: any) => {
  header.remove = false
}

/** 清空 API Key */
const clearApiKey = () => {
  formData.value.apiKey = ''
  formData.value.removeApiKey = true
}

/** 构建提交数据 */
const buildPayload = () => ({
  id: formData.value.id,
  configName: formData.value.configName.trim(),
  providerName: formData.value.providerName.trim(),
  protocol: formData.value.protocol,
  baseURL: formData.value.baseURL.trim(),
  apiKey: formData.value.removeApiKey ? null : formData.value.apiKey,
  removeApiKey: formData.value.removeApiKey,
  description: formData.value.description?.trim() || '',
  modelName: formData.value.modelName.trim(),
  streaming: Number(formData.value.streaming),
  thinking: Number(formData.value.thinking),
  temperature: Number(formData.value.temperature),
  topP: Number(formData.value.topP),
  maxTokens: Number(formData.value.maxTokens),
  timeoutMs: Number(formData.value.timeoutMs),
  thinkingBudget: Number(formData.value.thinking) === 1 && formData.value.thinkingBudget
    ? Number(formData.value.thinkingBudget)
    : null,
  maxAttempts: Number(formData.value.maxAttempts),
  status: Number(formData.value.status),
  headers: formData.value.headers.map(({ visible, ...header }: any) => header)
})

/** 测试连接 */
const handleTest = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  
  testing.value = true
  try {
    const payload = buildPayload()
    const result = await ModelConfigApi.testModelConfig(payload)
    if (result?.success) {
      const duration = Number(result.durationMs || 0)
      const durationText = duration >= 1000 ? `${(duration / 1000).toFixed(2)} s` : `${Math.round(duration)} ms`
      ElMessage.success(`连接成功，耗时 ${durationText}`)
    } else {
      ElMessage.error(result?.errorMessage || '模型测试失败')
    }
  } finally {
    testing.value = false
  }
}

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  
  formLoading.value = true
  try {
    const data = buildPayload()
    if (formType.value === 'create') {
      await ModelConfigApi.addModelConfig(data)
      message.success('模型配置创建成功')
    } else {
      await ModelConfigApi.updateModelConfig(data)
      message.success('模型配置已更新，后续调用立即生效')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.cursor-pointer {
  cursor: pointer;
}
</style>
