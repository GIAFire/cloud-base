<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="标签名称" prop="name">
        <el-input
          v-model="formData.name"
          maxlength="100"
          placeholder="请输入标签名称"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :disabled="formLoading" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { TagApi, type AgentTag } from '@/api/agent/tag'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'

defineOptions({ name: 'AgentTagForm' })

const { t } = useI18n()
const message = useMessage()

const createDefaultData = (): AgentTag => ({
  id: undefined,
  name: '',
  sort: 0,
  status: CommonStatusEnum.ENABLE
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref<'create' | 'update'>('create')
const formData = ref<AgentTag>(createDefaultData())
const formRules = reactive({
  name: [{ required: true, message: '标签名称不能为空', trigger: 'blur' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})
const formRef = ref()

const open = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t(`action.${type}`)
  formType.value = type
  resetForm()
  if (!id) return

  formLoading.value = true
  try {
    formData.value = await TagApi.getTag(id)
  } finally {
    formLoading.value = false
  }
}
defineExpose({ open })

const emit = defineEmits<{
  success: [tag: AgentTag]
}>()

const submitForm = async () => {
  await formRef.value?.validate()
  formLoading.value = true
  try {
    let savedTag = formData.value
    if (formType.value === 'create') {
      const id = await TagApi.createTag(formData.value)
      savedTag = { ...formData.value, id }
      message.success(t('common.createSuccess'))
    } else {
      await TagApi.updateTag(formData.value)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success', savedTag)
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = createDefaultData()
  formRef.value?.resetFields()
}
</script>
