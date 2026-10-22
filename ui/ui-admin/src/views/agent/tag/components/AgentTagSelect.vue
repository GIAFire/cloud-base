<template>
  <div class="agent-tag-select">
    <el-select
      v-model="selectedIds"
      v-loading="loading"
      clearable
      collapse-tags
      collapse-tags-tooltip
      filterable
      multiple
      :placeholder="placeholder"
    >
      <el-option
        v-for="tag in options"
        :key="tag.id"
        :disabled="tag.status === CommonStatusEnum.DISABLE"
        :label="tag.status === CommonStatusEnum.DISABLE ? `${tag.name}（已禁用）` : tag.name"
        :value="tag.id"
      />
    </el-select>
    <el-button
      v-if="showAdd"
      v-hasPermi="['agent:tag:create']"
      link
      type="primary"
      @click="formRef?.open('create')"
    >
      新增标签
    </el-button>
    <TagForm ref="formRef" @success="handleCreated" />
  </div>
</template>

<script setup lang="ts">
import { TagApi, type AgentTag, type AgentTagSimple } from '@/api/agent/tag'
import { CommonStatusEnum } from '@/utils/constants'
import TagForm from '../TagForm.vue'

defineOptions({ name: 'AgentTagSelect' })

interface TagOption {
  id: number
  name: string
  sort?: number
  status?: number
}

const props = withDefaults(
  defineProps<{
    modelValue?: number[]
    selectedItems?: TagOption[]
    placeholder?: string
    showAdd?: boolean
  }>(),
  {
    modelValue: () => [],
    selectedItems: () => [],
    placeholder: '请选择标签',
    showAdd: false
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: number[]]
}>()

const selectedIds = computed({
  get: () => props.modelValue,
  set: (value: number[]) => emit('update:modelValue', value)
})

const loading = ref(false)
const activeOptions = ref<AgentTagSimple[]>([])
const options = computed<TagOption[]>(() => {
  const optionMap = new Map<number, TagOption>()
  for (const tag of props.selectedItems) optionMap.set(tag.id, tag)
  for (const tag of activeOptions.value) optionMap.set(tag.id, tag)
  return [...optionMap.values()].sort(
    (left, right) => (left.sort ?? 0) - (right.sort ?? 0) || left.id - right.id
  )
})

const getList = async () => {
  loading.value = true
  try {
    activeOptions.value = await TagApi.getTagSimpleList()
  } finally {
    loading.value = false
  }
}

const formRef = ref<InstanceType<typeof TagForm>>()
const handleCreated = async (tag: AgentTag) => {
  await getList()
  if (!tag.id || tag.status !== CommonStatusEnum.ENABLE || props.modelValue.includes(tag.id)) {
    return
  }
  emit('update:modelValue', [...props.modelValue, tag.id])
}

onMounted(getList)
</script>

<style scoped lang="scss">
.agent-tag-select {
  display: flex;
  width: 100%;
  gap: 8px;
  align-items: center;

  :deep(.el-select) {
    flex: 1;
    min-width: 0;
  }
}
</style>
