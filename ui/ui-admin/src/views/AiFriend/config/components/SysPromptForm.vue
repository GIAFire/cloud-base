<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-width="100px"
    v-loading="formLoading"
  >
     <el-form-item label="提示词模板名称" prop="promptName">
      <el-input v-model="formData.promptName" placeholder="请输入提示词模板名称" />
    </el-form-item>
    <el-form-item label="描述" prop="description">
      <Editor v-model="formData.description" height="150px" />
    </el-form-item>
    <el-form-item label="提示词内容" prop="sysPrompt">
      <el-input v-model="formData.sysPrompt" placeholder="请输入提示词内容" />
    </el-form-item>
  </el-form>
</template>
<script setup lang="ts">
import { AgentInfoApi } from '@/api/AiFriend/config'

const props = defineProps<{
  id: number // 提示词模板主键ID（主表的关联字段）
}>()
const formLoading = ref(false) // 表单的加载中
const formData = ref<any>({})
const formRules = reactive({
  promptName: [{ required: true, message: '提示词模板名称不能为空', trigger: 'blur' }],
  description: [{ required: true, message: '描述不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.id,
  async (val) => {
    // 1. 重置表单
    formData.value = {
      id: undefined,
      promptName: undefined,
      description: undefined,
      sysPrompt: undefined,
    }
    // 2. val 非空，则加载数据
    if (!val) {
      return;
    }
    try {
      formLoading.value = true
      const data = await AgentInfoApi.getSysPromptById(val)
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