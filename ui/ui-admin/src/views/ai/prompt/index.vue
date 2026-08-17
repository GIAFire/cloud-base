<script lang="ts" setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar,
  Delete,
  Document,
  Edit,
  Link,
  MoreFilled,
  Plus,
  Refresh,
  Search,
  User,
  View
} from '@element-plus/icons-vue'
import {
  createPrompt,
  deletePrompt,
  getPrompt,
  getPromptAnalytics,
  getPromptMetrics,
  pagePrompts,
  updatePrompt
} from '@/api/agent/prompt'
import type {
  PromptVO,
  PromptMetricsVO,
  PromptAnalyticsVO,
  PromptCreateReqVO,
  PromptUpdateReqVO
} from '@/api/agent/prompt/types'
import { ContentWrap } from '@/components/ContentWrap'

defineOptions({ name: 'AiPrompt' })

// 状态管理
const loading = ref(false)
const submitting = ref(false)
const drawerVisible = ref(false)
const previewVisible = ref(false)
const formRef = ref()
const rows = ref<PromptVO[]>([])
const total = ref(0)
const metricsData = ref<PromptMetricsVO>({
  total: 0,
  newToday: 0,
  boundPrompts: 0,
  boundAgents: 0
})
const analytics = ref<PromptAnalyticsVO>({
  bindingRanking: [],
  recentlyUpdated: []
})
const previewPrompt = ref<PromptVO | null>(null)

const query = reactive({
  current: 1,
  size: 8,
  keyword: ''
})

interface FormState {
  id: number | null
  promptName: string
  description: string
  sysPrompt: string
  version: number | null
  bindingCount: number
}

const createForm = (): FormState => ({
  id: null,
  promptName: '',
  description: '',
  sysPrompt: '',
  version: null,
  bindingCount: 0
})

const form = reactive<FormState>(createForm())

const rules = {
  promptName: [
    { required: true, message: '请输入提示词名称', trigger: 'blur' },
    { min: 1, max: 100, message: '名称长度不能超过 100 个字符', trigger: 'blur' }
  ],
  sysPrompt: [
    { required: true, message: '请输入提示词内容', trigger: 'blur' },
    { min: 1, max: 50000, message: '内容不能超过 50000 个字符', trigger: 'blur' }
  ]
}

const drawerTitle = computed(() => (form.id ? '编辑系统提示词' : '新建系统提示词'))

const metricCards = computed(() => [
  {
    label: '提示词总数',
    value: formatNumber(metricsData.value.total),
    note: '当前租户未删除记录',
    icon: Document,
    tone: 'blue'
  },
  {
    label: '今日新增',
    value: formatNumber(metricsData.value.newToday),
    note: '今天创建的提示词',
    icon: Calendar,
    tone: 'violet'
  },
  {
    label: '已绑定提示词',
    value: formatNumber(metricsData.value.boundPrompts),
    note: `未绑定 ${formatNumber(unboundCount.value)} 个`,
    icon: Link,
    tone: 'green'
  },
  {
    label: '绑定智能体',
    value: formatNumber(metricsData.value.boundAgents),
    note: '按当前有效智能体统计',
    icon: User,
    tone: 'amber'
  }
])

const unboundCount = computed(
  () =>
    Math.max(
      Number(metricsData.value.total || 0) - Number(metricsData.value.boundPrompts || 0),
      0
    )
)

const maxBindingCount = computed(
  () =>
    Math.max(
      ...(analytics.value.bindingRanking || []).map((item) => Number(item.bindingCount || 0)),
      1
    )
)

const contentLength = computed(() => form.sysPrompt?.length || 0)

const loadDashboard = async () => {
  loading.value = true
  try {
    const [metricResult, analyticsResult, pageResult] = await Promise.all([
      getPromptMetrics(),
      getPromptAnalytics(5),
      pagePrompts(pageParams())
    ])
    metricsData.value = metricResult || {
      total: 0,
      newToday: 0,
      boundPrompts: 0,
      boundAgents: 0
    }
    analytics.value = analyticsResult || { bindingRanking: [], recentlyUpdated: [] }
    rows.value = pageResult?.records || []
    total.value = Number(pageResult?.total || 0)
  } finally {
    loading.value = false
  }
}

const loadPage = async () => {
  loading.value = true
  try {
    const result = await pagePrompts(pageParams())
    rows.value = result?.records || []
    total.value = Number(result?.total || 0)
  } finally {
    loading.value = false
  }
}

const pageParams = () => ({
  current: query.current,
  size: query.size,
  keyword: query.keyword || undefined
})

const search = () => {
  query.current = 1
  loadPage()
}

const resetForm = () => Object.assign(form, createForm())

const openCreate = async () => {
  resetForm()
  drawerVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

const openEdit = async (row: PromptVO) => {
  const detail = await getPrompt(row.id)
  Object.assign(form, createForm(), detail || {}, {
    id: detail?.id || row.id,
    description: detail?.description || '',
    sysPrompt: detail?.sysPrompt || ''
  })
  drawerVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

const openPreview = async (row: PromptVO) => {
  previewPrompt.value = await getPrompt(row.id)
  previewVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload: PromptCreateReqVO | PromptUpdateReqVO = {
      id: form.id || undefined,
      promptName: form.promptName,
      description: form.description,
      sysPrompt: form.sysPrompt,
      version: form.version
    }
    if (form.id) {
      await updatePrompt(payload as PromptUpdateReqVO)
      ElMessage.success('系统提示词已保存，后续运行将使用新内容')
    } else {
      await createPrompt(payload as PromptCreateReqVO)
      ElMessage.success('系统提示词已创建')
    }
    drawerVisible.value = false
    await loadDashboard()
  } finally {
    submitting.value = false
  }
}

const removePrompt = async (row: PromptVO) => {
  try {
    await ElMessageBox.confirm(
      `删除"${row.promptName}"后不可恢复，引用它的智能体将按未配置系统提示词运行。确认继续吗？`,
      '删除系统提示词',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }
  await deletePrompt(row.id)
  ElMessage.success('系统提示词已删除')
  if (rows.value.length === 1 && query.current > 1) {
    query.current -= 1
  }
  await loadDashboard()
}

const bindingWidth = (count: number) =>
  `${Math.max((Number(count || 0) / maxBindingCount.value) * 100, Number(count || 0) > 0 ? 8 : 0)}%`

const formatNumber = (value?: number) => Number(value || 0).toLocaleString('zh-CN')

const formatDate = (value?: string) => {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(loadDashboard)
</script>

<template>
  <div v-loading="loading" class="ai-prompt">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="mb-16px">
      <el-col v-for="item in metricCards" :key="item.label" :xs="24" :sm="12" :md="6">
        <ContentWrap>
          <div class="flex items-center gap-16px">
            <div
              class="w-48px h-48px rounded-12px flex items-center justify-center flex-shrink-0"
              :class="{
                'bg-blue-100': item.tone === 'blue',
                'bg-violet-100': item.tone === 'violet',
                'bg-green-100': item.tone === 'green',
                'bg-amber-100': item.tone === 'amber'
              }"
            >
              <el-icon
                :size="22"
                :class="{
                  'text-blue-600': item.tone === 'blue',
                  'text-violet-600': item.tone === 'violet',
                  'text-green-600': item.tone === 'green',
                  'text-amber-600': item.tone === 'amber'
                }"
              >
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div>
              <small class="text-gray-500 text-13px">{{ item.label }}</small>
              <strong class="block text-26px font-500 mt-4px">{{ item.value }}</strong>
              <p class="text-gray-400 text-12px mt-4px">{{ item.note }}</p>
            </div>
          </div>
        </ContentWrap>
      </el-col>
    </el-row>

    <!-- 主内容区域 -->
    <el-row :gutter="16">
      <!-- 提示词列表 -->
      <el-col :xs="24" :lg="18">
        <ContentWrap>
          <template #header>
            <div class="flex justify-between items-center w-full flex-wrap gap-16px">
              <div>
                <h3 class="text-17px font-600">系统提示词</h3>
                <p class="text-gray-500 text-12px mt-4px">
                  共 {{ total }} 条，编辑后的内容会直接用于智能体后续运行
                </p>
              </div>
              <div class="flex items-center gap-10px flex-wrap">
                <el-input
                  v-model="query.keyword"
                  clearable
                  :prefix-icon="Search"
                  placeholder="搜索名称或描述"
                  class="w-220px"
                  @clear="search"
                  @keyup.enter="search"
                />
                <el-button :icon="Refresh" @click="loadDashboard">刷新</el-button>
                <el-button :icon="Plus" type="primary" @click="openCreate">新建提示词</el-button>
              </div>
            </div>
          </template>

          <div v-if="rows.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-14px">
            <div
              v-for="row in rows"
              :key="row.id"
              class="prompt-card p-18px border border-gray-200 rounded-16px bg-white hover:shadow-md transition-shadow"
            >
              <!-- 操作菜单 -->
              <el-dropdown class="card-menu" trigger="click">
                <button
                  class="absolute top-18px right-18px w-28px h-28px rounded-6px flex items-center justify-center hover:bg-gray-100"
                  type="button"
                  aria-label="提示词操作"
                >
                  <el-icon class="text-gray-400"><MoreFilled /></el-icon>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :icon="View" @click="openPreview(row)">查看内容</el-dropdown-item>
                    <el-dropdown-item :icon="Edit" @click="openEdit(row)">编辑</el-dropdown-item>
                    <el-dropdown-item
                      :icon="Delete"
                      divided
                      class="text-red-500"
                      @click="removePrompt(row)"
                    >
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>

              <!-- 头部 -->
              <div class="flex items-start gap-12px pr-32px mb-16px">
                <span
                  class="w-42px h-42px rounded-12px flex items-center justify-center flex-shrink-0 bg-blue-50 text-blue-500"
                >
                  <el-icon :size="20"><Document /></el-icon>
                </span>
                <div class="min-w-0 flex-1">
                  <h4 class="text-16px font-500 text-gray-800 truncate">{{ row.promptName }}</h4>
                  <p class="text-12px text-gray-500 mt-5px line-clamp-2">
                    {{ row.description || '暂无描述' }}
                  </p>
                </div>
              </div>

              <!-- 内容预览 -->
              <button
                class="w-full min-h-104px p-13px mb-14px border border-gray-100 rounded-12px bg-gray-50 text-left hover:border-blue-200 hover:bg-blue-50 transition-colors"
                type="button"
                @click="openPreview(row)"
              >
                <span class="block text-11px font-600 text-gray-500 mb-7px tracking-wide">内容预览</span>
                <p class="text-12px text-gray-600 font-mono line-clamp-3">
                  {{ row.contentPreview || '暂无内容' }}
                </p>
              </button>

              <!-- 统计信息 -->
              <div class="grid grid-cols-2 gap-10px mb-14px">
                <div class="p-10px rounded-10px bg-gray-50">
                  <small class="block text-11px text-gray-400 mb-3px">内容长度</small>
                  <b class="block text-13px text-gray-700">{{ formatNumber(row.contentLength) }} 字符</b>
                </div>
                <div class="p-10px rounded-10px bg-gray-50">
                  <small class="block text-11px text-gray-400 mb-3px">绑定智能体</small>
                  <b class="block text-13px text-gray-700">{{ formatNumber(row.bindingCount) }} 个</b>
                </div>
              </div>

              <!-- 底部 -->
              <div class="flex items-end justify-between gap-10px pt-14px border-t border-gray-100">
                <div class="min-w-0">
                  <small class="block text-10px text-gray-400 mb-3px">最近更新</small>
                  <b class="block text-11px font-500 text-gray-500">{{ formatDate(row.updatedAt) }}</b>
                </div>
                <div class="flex gap-8px">
                  <el-button size="small" :icon="View" @click="openPreview(row)">预览</el-button>
                  <el-button size="small" type="primary" plain :icon="Edit" @click="openEdit(row)">
                    编辑
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <el-empty v-else description="暂无符合条件的系统提示词">
            <el-button type="primary" @click="openCreate">新建提示词</el-button>
          </el-empty>

          <el-pagination
            v-model:current-page="query.current"
            v-model:page-size="query.size"
            class="mt-20px justify-end"
            layout="total, prev, pager, next, sizes"
            :page-sizes="[8, 16, 32]"
            :total="total"
            @current-change="loadPage"
            @size-change="search"
          />
        </ContentWrap>
      </el-col>

      <!-- 侧边栏 -->
      <el-col :xs="24" :lg="6">
        <div class="space-y-16px">
          <!-- 绑定排行 -->
          <ContentWrap>
            <template #header>
              <div>
                <h3 class="text-16px font-600">绑定排行</h3>
                <p class="text-gray-500 text-12px mt-4px">按当前有效智能体引用数统计</p>
              </div>
            </template>

            <div v-if="analytics.bindingRanking?.length" class="space-y-8px">
              <button
                v-for="(item, index) in analytics.bindingRanking"
                :key="item.id"
                type="button"
                class="w-full p-9px rounded-11px text-left hover:bg-gray-50 transition-colors flex items-center gap-10px"
                @click="openPreview(item as any)"
              >
                <span
                  class="w-25px h-25px rounded-8px flex items-center justify-center flex-shrink-0 text-11px font-700"
                  :class="index < 3 ? 'bg-blue-50 text-blue-600' : 'bg-gray-100 text-gray-500'"
                >
                  {{ index + 1 }}
                </span>
                <span class="flex-1 min-w-0">
                  <span class="flex justify-between gap-8px">
                    <b class="text-12px text-gray-700 truncate">{{ item.promptName }}</b>
                    <em class="text-12px text-gray-400 font-normal">{{ item.bindingCount }} 个</em>
                  </span>
                  <i class="block h-5px mt-7px rounded-full bg-gray-100 overflow-hidden">
                    <u
                      class="block h-full rounded-full bg-gradient-to-r from-blue-500 to-purple-500"
                      :style="{ width: bindingWidth(item.bindingCount) }"
                    />
                  </i>
                </span>
              </button>
            </div>
            <el-empty v-else description="暂无提示词数据" :image-size="68" />
          </ContentWrap>

          <!-- 最近更新 -->
          <ContentWrap>
            <template #header>
              <div>
                <h3 class="text-16px font-600">最近更新</h3>
                <p class="text-gray-500 text-12px mt-4px">最近变更内容的系统提示词</p>
              </div>
            </template>

            <div v-if="analytics.recentlyUpdated?.length" class="space-y-8px">
              <button
                v-for="item in analytics.recentlyUpdated"
                :key="item.id"
                type="button"
                class="w-full p-9px rounded-11px text-left hover:bg-gray-50 transition-colors flex items-center gap-10px"
                @click="openPreview(item as any)"
              >
                <span
                  class="w-34px h-34px rounded-10px flex items-center justify-center flex-shrink-0 bg-blue-50 text-blue-500"
                >
                  <el-icon :size="15"><Document /></el-icon>
                </span>
                <span class="flex-1 min-w-0">
                  <b class="block text-12px text-gray-700 truncate mb-3px">{{ item.promptName }}</b>
                  <small class="block text-10px text-gray-400 truncate">
                    {{ formatNumber(item.contentLength) }} 字符 · {{ formatDate(item.updatedAt) }}
                  </small>
                </span>
              </button>
            </div>
            <el-empty v-else description="暂无更新记录" :image-size="68" />
          </ContentWrap>
        </div>
      </el-col>
    </el-row>

    <!-- 编辑抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerTitle"
      size="min(820px, 96vw)"
      destroy-on-close
      class="prompt-drawer"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <!-- 基础信息 -->
        <div class="p-20px mb-16px border border-gray-200 rounded-15px bg-white">
          <div class="flex items-start gap-12px mb-18px">
            <span
              class="w-30px h-30px rounded-9px flex items-center justify-center flex-shrink-0 bg-blue-50 text-blue-600 text-11px font-700"
            >
              01
            </span>
            <div class="flex-1">
              <h4 class="text-15px font-500 text-gray-800">基础信息</h4>
              <p class="text-12px text-gray-500 mt-3px">
                同一租户内名称不可重复，名称用于智能体配置时识别。
              </p>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-16px">
            <el-form-item label="提示词名称" prop="promptName">
              <el-input
                v-model="form.promptName"
                maxlength="100"
                show-word-limit
                placeholder="例如：数据分析助手"
              />
            </el-form-item>
            <el-form-item label="绑定情况">
              <div class="flex items-center gap-8px text-13px text-gray-500 min-h-32px">
                <el-icon><Link /></el-icon>
                <span v-if="form.id">当前被 {{ form.bindingCount || 0 }} 个智能体引用</span>
                <span v-else>创建后可在智能体配置中选择</span>
              </div>
            </el-form-item>
            <el-form-item class="md:col-span-2" label="描述">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                placeholder="说明适用场景、职责或使用约束"
              />
            </el-form-item>
          </div>
        </div>

        <!-- 提示词内容 -->
        <div class="p-20px mb-16px border border-gray-200 rounded-15px bg-white">
          <div class="flex items-start justify-between gap-12px mb-18px">
            <div class="flex items-start gap-12px">
              <span
                class="w-30px h-30px rounded-9px flex items-center justify-center flex-shrink-0 bg-blue-50 text-blue-600 text-11px font-700"
              >
                02
              </span>
              <div class="flex-1">
                <h4 class="text-15px font-500 text-gray-800">提示词内容</h4>
                <p class="text-12px text-gray-500 mt-3px">
                  保存后会直接影响所有引用它的智能体之后的运行。
                </p>
              </div>
            </div>
            <strong class="text-12px text-gray-500">{{ formatNumber(contentLength) }} / 50,000</strong>
          </div>

          <el-form-item prop="sysPrompt" class="mb-0">
            <el-input
              v-model="form.sysPrompt"
              type="textarea"
              :rows="16"
              maxlength="50000"
              resize="vertical"
              placeholder="输入智能体的角色、目标、边界和行为要求……"
              class="content-textarea"
            />
          </el-form-item>
        </div>

        <!-- 只读预览 -->
        <div class="p-20px border border-gray-200 rounded-15px bg-white">
          <div class="flex items-start gap-12px mb-18px">
            <span
              class="w-30px h-30px rounded-9px flex items-center justify-center flex-shrink-0 bg-blue-50 text-blue-600 text-11px font-700"
            >
              03
            </span>
            <div class="flex-1">
              <h4 class="text-15px font-500 text-gray-800">只读预览</h4>
              <p class="text-12px text-gray-500 mt-3px">按智能体实际读取时的纯文本内容展示。</p>
            </div>
          </div>

          <pre
            class="min-h-130px max-h-320px p-16px border border-gray-200 rounded-12px bg-gray-50 text-13px text-gray-600 font-mono overflow-auto whitespace-pre-wrap break-words"
          >
{{ form.sysPrompt || '尚未输入提示词内容' }}</pre>
        </div>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-10px">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      width="min(820px, 94vw)"
      :title="previewPrompt?.promptName || '提示词预览'"
    >
      <div class="flex items-center gap-8px mb-14px flex-wrap">
        <span class="min-w-200px flex-1 text-13px text-gray-500">
          {{ previewPrompt?.description || '暂无描述' }}
        </span>
        <el-tag effect="plain">{{ formatNumber(previewPrompt?.contentLength) }} 字符</el-tag>
        <el-tag effect="plain" type="info">
          绑定 {{ formatNumber(previewPrompt?.bindingCount) }} 个智能体
        </el-tag>
      </div>
      <pre
        class="min-h-280px max-h-58vh p-18px border border-gray-200 rounded-12px bg-gray-50 text-13px text-gray-600 font-mono overflow-auto whitespace-pre-wrap break-words"
      >
{{ previewPrompt?.sysPrompt || '暂无内容' }}</pre>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button
          type="primary"
          :icon="Edit"
          @click="previewVisible = false; openEdit(previewPrompt!)"
        >
          编辑
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.ai-prompt {
  :deep(.el-card__header) {
    padding: 12px 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(.el-card__body) {
    padding: 16px;
  }

  .prompt-card {
    position: relative;
    min-height: 355px;
    display: flex;
    flex-direction: column;
  }

  .card-menu {
    position: absolute;
    top: 18px;
    right: 18px;
  }

  .line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .line-clamp-3 {
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .content-textarea :deep(.el-textarea__inner) {
    min-height: 330px !important;
    padding: 15px;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 13px;
    line-height: 1.75;
  }
}

:deep(.prompt-drawer .el-drawer__body) {
  padding: 18px 20px;
  background: #f7f9fc;
}

:deep(.prompt-drawer .el-drawer__footer) {
  padding: 14px 20px;
  border-top: 1px solid #edf0f5;
}
</style>
