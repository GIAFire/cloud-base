<template>
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
      v-for="category in options"
      :key="category.id"
      :disabled="category.status === CommonStatusEnum.DISABLE"
      :label="
        category.status === CommonStatusEnum.DISABLE ? `${category.name}（已禁用）` : category.name
      "
      :value="category.id"
    />
  </el-select>
</template>

<script setup lang="ts">
import { CategoryApi, type AgentCategorySimple } from '@/api/agent/category'
import { CommonStatusEnum } from '@/utils/constants'

defineOptions({ name: 'AgentCategorySelect' })

interface CategoryOption {
  id: number
  name: string
  sort?: number
  status?: number
}

const props = withDefaults(
  defineProps<{
    modelValue?: number[]
    selectedItems?: CategoryOption[]
    placeholder?: string
  }>(),
  {
    modelValue: () => [],
    selectedItems: () => [],
    placeholder: '请选择分类'
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
const activeOptions = ref<AgentCategorySimple[]>([])
const options = computed<CategoryOption[]>(() => {
  const optionMap = new Map<number, CategoryOption>()
  for (const category of props.selectedItems) optionMap.set(category.id, category)
  for (const category of activeOptions.value) optionMap.set(category.id, category)
  return [...optionMap.values()].sort(
    (left, right) => (left.sort ?? 0) - (right.sort ?? 0) || left.id - right.id
  )
})

const getList = async () => {
  loading.value = true
  try {
    activeOptions.value = await CategoryApi.getCategorySimpleList()
  } finally {
    loading.value = false
  }
}

onMounted(getList)
</script>
