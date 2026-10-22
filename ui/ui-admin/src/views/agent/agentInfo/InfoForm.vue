<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="模型名称" prop="modelId">
        <el-select
          v-model="formData.modelId"
          clearable
          filterable
          placeholder="请选择模型"
          no-data-text="暂无模型配置"
          class="!w-100%"
        >
          <el-option
            v-for="model in modelOptions"
            :key="model.id"
            :label="model.modelName"
            :value="model.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="提示词名称" prop="sysPromptId">
        <el-select
          v-model="formData.sysPromptId"
          clearable
          filterable
          placeholder="请选择提示词"
          no-data-text="暂无提示词配置"
          class="!w-100%"
        >
          <el-option
            v-for="prompt in sysPromptOptions"
            :key="prompt.id"
            :label="prompt.promptName"
            :value="prompt.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="code" prop="agentCode">
        <el-input v-model="formData.agentCode" placeholder="请输入Agent唯一编码" />
      </el-form-item>
      <el-form-item label="名称" prop="agentName">
        <el-input v-model="formData.agentName" placeholder="请输入Agent 显示名称" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="formData.description" type="textarea" placeholder="请输入Agent 描述" />
      </el-form-item>
      <el-form-item label="人格" prop="personality">
        <el-input v-model="formData.personality" type="textarea" placeholder="请输入Agent 人格" />
      </el-form-item>
      <el-form-item label="最大循环" prop="maxIters">
        <el-input v-model="formData.maxIters" placeholder="请输入最大循环次数" />
      </el-form-item>
      <el-form-item label="权限模式" prop="permissionMode">
        <el-radio-group v-model="formData.permissionMode">
          <el-radio
            v-for="dict in getStrDictOptions(DICT_TYPE.AGENT_PERMISSION_TYPE)"
            :key="dict.value"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="上下文压缩" prop="compactionEnabled">
        <el-radio-group v-model="formData.compactionEnabled">
          <el-radio
            v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
            :key="String(dict.value)"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="触发压缩数" prop="triggerMessages">
        <el-input v-model="formData.triggerMessages" placeholder="请输入触发压缩消息数" />
      </el-form-item>
      <el-form-item label="保留消息数" prop="keepMessages">
        <el-input v-model="formData.keepMessages" placeholder="请输入压缩后保留消息数" />
      </el-form-item>
      <el-form-item label="触发token数" prop="triggerTokens">
        <el-input v-model="formData.triggerTokens" placeholder="请输入触发压缩token数" />
      </el-form-item>
      <el-form-item label="保留token数" prop="keepTokens">
        <el-input v-model="formData.keepTokens" placeholder="请输入压缩后保留token数" />
      </el-form-item>
      <el-form-item label="大工具卸载" prop="toolResultEvictionEnabled">
        <el-radio-group v-model="formData.toolResultEvictionEnabled">
          <el-radio
            v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
            :key="String(dict.value)"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="长期记忆" prop="memoryEnable">
        <el-radio-group v-model="formData.memoryEnable">
          <el-radio
            v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
            :key="String(dict.value)"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Plan Mode" prop="planModeEnabled">
        <el-radio-group v-model="formData.planModeEnabled">
          <el-radio
            v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
            :key="String(dict.value)"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="shell工具" prop="allowShellInPlanMode">
        <el-radio-group v-model="formData.allowShellInPlanMode">
          <el-radio
            v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
            :key="String(dict.value)"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
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
import { InfoApi, Info } from '@/api/agent/agentInfo'
import { ModelApi, type ModelSimple } from '@/api/agent/config/model'
import { SysPromptApi, type SysPromptSimple } from '@/api/agent/config/sysprompt'

/** Agent 定义表 表单 */
defineOptions({ name: 'InfoForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const modelOptions = ref<ModelSimple[]>([]) // 模型下拉选项
const sysPromptOptions = ref<SysPromptSimple[]>([]) // 提示词下拉选项
const formData = ref({
  id: undefined,
  modelId: undefined,
  sysPromptId: undefined,
  agentCode: undefined,
  agentName: undefined,
  description: undefined,
  personality: undefined,
  maxIters: undefined,
  permissionMode: undefined,
  compactionEnabled: undefined,
  triggerMessages: undefined,
  keepMessages: undefined,
  triggerTokens: undefined,
  keepTokens: undefined,
  toolResultEvictionEnabled: undefined,
  memoryEnable: undefined,
  planModeEnabled: undefined,
  allowShellInPlanMode: undefined
})
const formRules = reactive({
  agentCode: [{ required: true, message: 'Agent唯一编码不能为空', trigger: 'blur' }],
  agentName: [{ required: true, message: 'Agent 显示名称不能为空', trigger: 'blur' }],
  personality: [{ required: true, message: 'Agent 人格不能为空', trigger: 'blur' }],
  maxIters: [{ required: true, message: '最大循环次数不能为空', trigger: 'blur' }],
  permissionMode: [{ required: true, message: '默认权限模式不能为空', trigger: 'blur' }],
  compactionEnabled: [{ required: true, message: '是否启用上下文压缩：1启用，0关闭不能为空', trigger: 'blur' }],
  toolResultEvictionEnabled: [{ required: true, message: '是否启用大工具结果卸载：1启用，0关闭不能为空', trigger: 'blur' }],
  planModeEnabled: [{ required: true, message: '是否启用Plan Mode：1启用，0关闭不能为空', trigger: 'blur' }],
  allowShellInPlanMode: [{ required: true, message: 'Plan阶段是否允许shell工具：1允许，0禁止；生产环境建议关闭不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  formLoading.value = true
  try {
    const infoPromise = id ? InfoApi.getInfo(id) : Promise.resolve(undefined)
    const [models, sysPrompts, info] = await Promise.all([
      ModelApi.getModelSimpleList(),
      SysPromptApi.getSysPromptSimpleList(),
      infoPromise
    ])
    modelOptions.value = models || []
    sysPromptOptions.value = sysPrompts || []
    // 修改时，设置数据
    if (info) {
      formData.value = info
    }
  } finally {
    formLoading.value = false
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
    const data = formData.value as unknown as Info
    if (formType.value === 'create') {
      await InfoApi.createInfo(data)
      message.success(t('common.createSuccess'))
    } else {
      await InfoApi.updateInfo(data)
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
    modelId: undefined,
    sysPromptId: undefined,
    agentCode: undefined,
    agentName: undefined,
    description: undefined,
    maxIters: undefined,
    permissionMode: undefined,
    compactionEnabled: undefined,
    triggerMessages: undefined,
    keepMessages: undefined,
    triggerTokens: undefined,
    keepTokens: undefined,
    toolResultEvictionEnabled: undefined,
    memoryEnable: undefined,
    planModeEnabled: undefined,
    allowShellInPlanMode: undefined
  }
  formRef.value?.resetFields()
}
</script>
