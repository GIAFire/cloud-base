<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Collection, Delete, Document, MoreFilled, Plus, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import * as KnowledgeApi from '@/api/agent/knowledge'

defineOptions({ name: 'AiKnowledge' })

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const metrics = ref<any>({})

const query = reactive({
  keyword: '',
  status: '',
  current: 1,
  size: 8
})

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const dialogMode = ref('create')

const form = reactive({
  id: '',
  knowledgeName: '',
  description: '',
  modelUrl: '',
  apiKey: '',
  embeddingModelName: '',
  embeddingDimension: 1024,
  metricType: 'COSINE',
  topK: 5,
  scoreThreshold: 0.5,
  status: 1
})

const isEdit = computed(() => dialogMode.value === 'edit')
const dialogTitle = computed(() => isEdit.value ? '编辑知识库' : '创建知识库')

const formatNumber = (value: number) => Number(value || 0).toLocaleString('zh-CN')

const statusMeta = (status: number) => {
  const value = String(status ?? '').toUpperCase()
  if (value === '0' || value === 'DISABLED') return { text: '已停用', type: 'info' }
  return { text: '已启用', type: 'success' }
}

const formatTime = (row: any) => row.updatedAt || row.updateTime || row.createdAt || row.createTime || '-'

const knowledgeMetricCards = computed(() => [
  { label: '知识库总数', value: formatNumber(metrics.value.totalKnowledgeBases), note: `已启用 ${formatNumber(metrics.value.enabledKnowledgeBases)}`, icon: Collection, tone: 'blue' },
  { label: '文档总数', value: formatNumber(metrics.value.totalDocuments), note: `今日新增 ${formatNumber(metrics.value.newDocumentsToday)}`, icon: Document, tone: 'violet' }
])

const loadKnowledgeOverview = async () => {
  metrics.value = await KnowledgeApi.getKnowledgeMetrics() || {}
}

const loadRows = async () => {
  loading.value = true
  try {
    const data = await KnowledgeApi.listKnowledgeBases({
      current: query.current,
      size: query.size,
      keyword: query.keyword.trim() || undefined,
      status: query.status === '' ? undefined : query.status
    })
    rows.value = data?.records || []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

const loadPage = async () => {
  await Promise.all([loadRows(), loadKnowledgeOverview()])
}

const handleSearch = () => {
  query.current = 1
  loadRows()
}

const openCreate = () => {
  dialogMode.value = 'create'
  Object.assign(form, {
    id: '',
    knowledgeName: '',
    description: '',
    modelUrl: '',
    apiKey: '',
    embeddingModelName: '',
    embeddingDimension: 1024,
    metricType: 'COSINE',
    topK: 5,
    scoreThreshold: 0.5,
    status: 1
  })
  dialogVisible.value = true
}

const openEdit = async (row: any) => {
  dialogMode.value = 'edit'
  const data = await KnowledgeApi.getKnowledgeBase(row.id)
  Object.assign(form, {
    id: String(data.id),
    knowledgeName: data.knowledgeName || '',
    description: data.description || '',
    modelUrl: data.modelUrl || '',
    apiKey: '',
    embeddingModelName: data.embeddingModelName || '',
    embeddingDimension: Number(data.embeddingDimension || 1024),
    metricType: data.metricType || 'COSINE',
    topK: Number(data.topK || 5),
    scoreThreshold: Number(data.scoreThreshold ?? 0.5),
    status: Number(data.status ?? 1)
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  submitting.value = true
  try {
    if (isEdit.value) {
      await KnowledgeApi.updateKnowledgeBase(form.id, form)
      ElMessage.success('知识库已更新')
    } else {
      await KnowledgeApi.createKnowledgeBase(form)
      ElMessage.success('知识库创建成功')
    }
    dialogVisible.value = false
    await loadPage()
  } finally {
    submitting.value = false
  }
}

const openDocuments = (row: any) => {
  router.push(`/ai/knowledge/${row.id}/documents`)
}

const toggleStatus = async (row: any) => {
  const nextStatus = Number(row.status) === 1 ? 0 : 1
  const action = nextStatus === 1 ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`${action}知识库"${row.knowledgeName}"吗？`, `${action}确认`, { type: 'warning' })
  } catch {
    return
  }
  await KnowledgeApi.updateKnowledgeBase(row.id, { ...row, status: nextStatus })
  ElMessage.success(`知识库已${action}`)
  await loadPage()
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`删除知识库"${row.knowledgeName}"吗？`, '删除知识库', { type: 'warning' })
  } catch {
    return
  }
  await KnowledgeApi.deleteKnowledgeBase(row.id)
  ElMessage.success('知识库已删除')
  await loadPage()
}

onMounted(loadPage)
</script>

<template>
  <ContentWrap>
    <!-- 指标卡片 -->
    <div class="mb-4 grid grid-cols-4 gap-4">
      <el-card v-for="item in knowledgeMetricCards" :key="item.label" shadow="hover">
        <div class="flex items-center gap-3">
          <div class="flex h-12 w-12 items-center justify-center rounded-lg bg-blue-100 text-blue-600">
            <el-icon :size="24"><component :is="item.icon" /></el-icon>
          </div>
          <div>
            <div class="text-xs text-gray-500">{{ item.label }}</div>
            <div class="text-2xl font-bold">{{ item.value }}</div>
            <div class="text-xs text-gray-400">{{ item.note }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 知识库列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-bold">知识库列表</h3>
            <p class="text-sm text-gray-500">共 {{ total }} 个知识库</p>
          </div>
          <div class="flex gap-2">
            <el-input v-model="query.keyword" clearable :prefix-icon="Search" placeholder="知识库名称或描述" @keyup.enter="handleSearch" />
            <el-select v-model="query.status" clearable placeholder="全部状态" @change="handleSearch">
              <el-option label="已启用" :value="1" />
              <el-option label="已停用" :value="0" />
            </el-select>
            <el-button :icon="Refresh" @click="loadPage">刷新</el-button>
            <el-button type="primary" :icon="Plus" @click="openCreate">创建知识库</el-button>
          </div>
        </div>
      </template>

      <div v-if="rows.length" class="grid grid-cols-4 gap-4">
        <el-card v-for="row in rows" :key="row.id" shadow="hover">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-2">
                <h4 class="font-bold">{{ row.knowledgeName }}</h4>
                <el-tag :type="statusMeta(row.status).type" size="small">{{ statusMeta(row.status).text }}</el-tag>
              </div>
              <p class="mt-2 text-sm text-gray-600">{{ row.description || '暂无描述' }}</p>
            </div>
            <el-dropdown trigger="click">
              <el-button link><el-icon><MoreFilled /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="openDocuments(row)">文档管理</el-dropdown-item>
                  <el-dropdown-item @click="openEdit(row)">编辑</el-dropdown-item>
                  <el-dropdown-item @click="toggleStatus(row)">
                    {{ Number(row.status) === 1 ? '停用' : '启用' }}
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleDelete(row)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="mt-3 text-sm text-gray-500">
            <div>Embedding 模型: {{ row.embeddingModelName || '-' }}</div>
            <div>向量规格: {{ row.embeddingDimension }} 维 · {{ row.metricType }}</div>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无符合条件的知识库">
        <el-button type="primary" @click="openCreate">创建知识库</el-button>
      </el-empty>

      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        class="mt-4"
        :total="total"
        :page-sizes="[8, 16, 32]"
        layout="total, prev, pager, next, sizes"
        @current-change="loadRows"
        @size-change="handleSearch"
      />
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form label-position="top">
        <el-form-item label="知识库名称" required>
          <el-input v-model="form.knowledgeName" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="Embedding API 地址" required>
          <el-input v-model="form.modelUrl" placeholder="https://example.com/v1" />
        </el-form-item>
        <el-form-item label="API Key" :required="!isEdit">
          <el-input v-model="form.apiKey" type="password" show-password :placeholder="isEdit ? '留空表示继续使用原 API Key' : '请输入真实 API Key'" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="模型名称" required>
              <el-input v-model="form.embeddingModelName" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="向量维度" required>
              <el-input-number v-model="form.embeddingDimension" :disabled="isEdit" :min="1" :max="65535" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">{{ isEdit ? '保存修改' : '验证并创建' }}</el-button>
      </template>
    </el-dialog>
  </ContentWrap>
</template>

<style scoped lang="scss">
.grid-cols-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}
</style>
