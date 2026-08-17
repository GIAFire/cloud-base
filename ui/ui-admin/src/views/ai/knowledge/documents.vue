<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Delete, Document, Plus, Refresh, UploadFilled } from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import * as KnowledgeApi from '@/api/agent/knowledge'

defineOptions({ name: 'AiKnowledgeDocuments' })

const route = useRoute()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const knowledgeBase = ref<any>(null)

const current = ref(1)
const size = ref(10)

const query = reactive({
  keyword: '',
  parseStatus: ''
})

const knowledgeBaseId = computed(() => String(route.params.knowledgeBaseId || ''))

const loadKnowledgeBase = async () => {
  knowledgeBase.value = await KnowledgeApi.getKnowledgeBase(knowledgeBaseId.value)
}

const loadDocuments = async () => {
  loading.value = true
  try {
    const data = await KnowledgeApi.listKnowledgeDocuments(knowledgeBaseId.value, {
      current: current.value,
      size: size.value,
      keyword: query.keyword.trim() || undefined,
      parseStatus: query.parseStatus || undefined
    })
    rows.value = data?.records || []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

const parseStatusMeta = (status: string) => {
  const value = String(status || 'UPLOADED').toUpperCase()
  const map: Record<string, any> = {
    UPLOADED: { text: '已上传', type: 'info' },
    PENDING: { text: '等待处理', type: 'info' },
    PROCESSING: { text: '处理中', type: 'warning' },
    READY: { text: '已就绪', type: 'success' },
    FAILED: { text: '处理失败', type: 'danger' }
  }
  return map[value] || { text: value, type: 'info' }
}

const formatBytes = (bytes: number) => {
  const value = Number(bytes || 0)
  if (!value) return '0 B'
  if (value < 1024) return `${value} B`
  if (value < 1024 ** 2) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / 1024 ** 2).toFixed(1)} MB`
}

onMounted(async () => {
  await Promise.all([loadKnowledgeBase(), loadDocuments()])
})
</script>

<template>
  <ContentWrap>
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <el-button :icon="ArrowLeft" circle @click="$router.push('/ai/knowledge')" />
            <div>
              <h3 class="text-lg font-bold">{{ knowledgeBase?.knowledgeName || '知识库文档' }}</h3>
              <p class="text-sm text-gray-500">{{ knowledgeBase?.description || '上传源文档，并显式提交切片入库。' }}</p>
            </div>
          </div>
          <div class="flex gap-2">
            <el-button :icon="Refresh" @click="loadDocuments">刷新状态</el-button>
            <el-button type="primary" :icon="UploadFilled">上传文档</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column label="文档" min-width="260">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-icon class="text-blue-600"><Document /></el-icon>
              <div>
                <div class="font-bold">{{ row.documentName }}</div>
                <div class="text-xs text-gray-500">{{ formatBytes(row.sizeBytes) }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="125">
          <template #default="{ row }">
            <el-tag :type="parseStatusMeta(row.parseStatus).type">{{ parseStatusMeta(row.parseStatus).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chunkCount" label="切片数" width="105" align="center" />
        <el-table-column prop="createdAt" label="上传时间" min-width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary">切片入库</el-button>
            <el-button link type="danger" :icon="Delete" />
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="current"
        v-model:page-size="size"
        class="mt-4"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </el-card>
  </ContentWrap>
</template>
