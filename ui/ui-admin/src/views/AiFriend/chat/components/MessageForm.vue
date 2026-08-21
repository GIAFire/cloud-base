<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    v-loading="formLoading"
    label-width="0px"
    :inline-message="true"
  >
    <el-table :data="formData" class="-mt-10px">
      <el-table-column label="序号" type="index" width="100" />
       <el-table-column label="消息序号" min-width="150">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.seq`" :rules="formRules.seq" class="mb-0px!">
            <el-input v-model="row.seq" placeholder="请输入会话内全局消息序号" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="协议角色" min-width="150">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.role`" :rules="formRules.role" class="mb-0px!">
            <el-input v-model="row.role" placeholder="请输入角色" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="消息类型" min-width="150">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.messageType`" :rules="formRules.messageType" class="mb-0px!">
            <el-select v-model="row.messageType" placeholder="请选择消息类型">
                <el-option label="请选择字典生成" value="" />
            </el-select>
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="文本内容" min-width="400">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.textContent`" :rules="formRules.textContent" class="mb-0px!">
            <Editor v-model="row.textContent" height="150px" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="Token用量" min-width="150">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.usageJson`" :rules="formRules.usageJson" class="mb-0px!">
            <el-input v-model="row.usageJson" placeholder="请输入Token用量" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="耗时，单位毫秒" min-width="150">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.durationMs`" :rules="formRules.durationMs" class="mb-0px!">
            <el-input v-model="row.durationMs" placeholder="请输入耗时，单位毫秒" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column align="center" fixed="right" label="操作" width="60">
        <template #default="{ $index }">
          <el-button @click="handleDelete($index)" link>—</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-form>
  <el-row justify="center" class="mt-3">
    <el-button @click="handleAdd" round>+ 添加Agent消息表：保存输入、思考、正文等消息</el-button>
  </el-row>
</template>
<script setup lang="ts">
import { SessionApi } from '@/api/AiFriend/chat'

const props = defineProps<{
  sessionId: number // 会话ID，关联ai_agent_session.id（主表的关联字段）
}>()
const formLoading = ref(false) // 表单的加载中
const formData = ref<any[]>([])
const formRules = reactive({
  sessionId: [{ required: true, message: '会话ID，关联ai_agent_session.id不能为空', trigger: 'blur' }],
  seq: [{ required: true, message: '会话内全局消息序号，用于历史恢复和稳定排序不能为空', trigger: 'blur' }],
  role: [{ required: true, message: '协议角色：USER、ASSISTANT、SYSTEM、TOOL不能为空', trigger: 'blur' }],
  messageType: [{ required: true, message: '消息类型：USER_TEXT、ASSISTANT_THINKING、ASSISTANT_TEXT、TOOL_CALL、TOOL_RESULT、SKILL_CALL、SKILL_RESULT、SUBAGENT_CALL、SUBAGENT_RESULT、PLAN_SNAPSHOT、PLAN_OPERATION、SYSTEM_NOTICE、ERROR不能为空', trigger: 'change' }]
})
const formRef = ref() // 表单 Ref

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.sessionId,
  async (val) => {
    // 1. 重置表单
    formData.value = []
    // 2. val 非空，则加载数据
    if (!val) {
      return;
    }
    try {
      formLoading.value = true
      formData.value = await SessionApi.getMessageListBySessionId(val)
    } finally {
      formLoading.value = false
    }
  },
  { immediate: true }
)

/** 新增按钮操作 */
const handleAdd = () => {
  const row = {
    id: undefined,
    sessionId: undefined,
    seq: undefined,
    role: undefined,
    messageType: undefined,
    textContent: undefined,
    usageJson: undefined,
    durationMs: undefined
  }
  row.sessionId = props.sessionId as any
  formData.value.push(row)
}

/** 删除按钮操作 */
const handleDelete = (index) => {
  formData.value.splice(index, 1)
}

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
