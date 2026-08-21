<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="Agent ID" prop="agentId">
        <el-input v-model="formData.agentId" placeholder="请输入Agent ID" />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="formData.userId" placeholder="请输入用户ID" />
      </el-form-item>
      <el-form-item label="会话标题" prop="title">
        <el-input v-model="formData.title" placeholder="请输入会话标题" />
      </el-form-item>
    </el-form>
    <!-- 子表的表单 -->
    <el-tabs v-model="subTabsName">
      <el-tab-pane label="消息" name="message">
        <MessageForm ref="messageFormRef" :session-id="formData.id" />
      </el-tab-pane>
    </el-tabs>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { SessionApi, Session } from '@/api/AiFriend/chat'
import MessageForm from './components/MessageForm.vue'

/** Agent会话表：保存用户和Agent之间的一段连续对话 表单 */
defineOptions({ name: 'SessionForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  agentId: undefined,
  userId: undefined,
  title: undefined
})
const formRules = reactive({
  agentId: [{ required: true, message: 'Agent ID', trigger: 'blur' }],
  userId: [{ required: true, message: '用户ID', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 子表的表单 */
const subTabsName = ref('message')
const messageFormRef = ref()

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
      formData.value = await SessionApi.getSession(id)
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
  // 校验子表单
  try {
    await messageFormRef.value.validate()
  } catch (e) {
    subTabsName.value = 'message'
    return
  }
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as Session
    // 拼接子表的数据
    data.messages = messageFormRef.value.getData()
    if (formType.value === 'create') {
      await SessionApi.createSession(data)
      message.success(t('common.createSuccess'))
    } else {
      await SessionApi.updateSession(data)
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
    agentId: undefined,
    userId: undefined,
    title: undefined
  }
  formRef.value?.resetFields()
}
</script>
