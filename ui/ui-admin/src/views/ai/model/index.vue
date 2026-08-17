<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Refresh, Search, TrendCharts } from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import * as ModelConfigApi from '@/api/agent/modelConfig'
import ModelForm from './ModelForm.vue'

defineOptions({ name: 'AiModel' })

const { t } = useI18n()
const message = useMessage()

// 列表数据
const loading = ref(false)
const list = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  status: undefined
})

// 表单引用
const formRef = ref()

// 指标数据
const metricsData = ref({
  total: 0,
  enabled: 0,
  todayCalls: 0,
  callChangePercent: 0,
  successRate: 0,
  failedCalls: 0,
  averageDurationMs: 0,
  averageDurationChangeMs: 0
})

// 分析数据
const analytics = ref({
  trend: [],
  providerDistribution: []
})

// 调用日志弹窗
const logVisible = ref(false)
const logs = reactive({
  loading: false,
  model: null,
  records: [],
  total: 0,
  current: 1,
  size: 10,
  callSource: '',
  status: ''
})

// 工具函数
const formatNumber = (value?: number) => Number(value || 0).toLocaleString('zh-CN')

const formatDuration = (value?: number) => {
  if (value == null) return '--'
  const duration = Number(value)
  return duration >= 1000 ? `${(duration / 1000).toFixed(2)} s` : `${Math.round(duration)} ms`
}

const protocolLabel = (value: string) => {
  const protocols = [
    { value: 'openaiCompatible', label: 'OpenAI 兼容协议' },
    { value: 'dashscope', label: 'DashScope' },
    { value: 'anthropic', label: 'Anthropic' },
    { value: 'ollama', label: 'Ollama' }
  ]
  return protocols.find((item) => item.value === value)?.label || value || '--'
}

const statusType = (status: string) => {
  const typeMap = {
    SUCCESS: 'success',
    FAILED: 'danger',
    RUNNING: 'warning',
    CANCELLED: 'info'
  }
  return typeMap[status] || 'info'
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ModelConfigApi.pageModelConfig(queryParams)
    list.value = data?.records || []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

/** 查询指标 */
const getMetrics = async () => {
  try {
    const data = await ModelConfigApi.getModelMetrics()
    metricsData.value = data || {}
  } catch (error) {
    console.error('获取指标失败', error)
  }
}

/** 查询分析数据 */
const getAnalytics = async () => {
  try {
    const data = await ModelConfigApi.getModelAnalytics(7)
    analytics.value = data || { trend: [], providerDistribution: [] }
  } catch (error) {
    console.error('获取分析数据失败', error)
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.status = undefined
  handleQuery()
}

/** 新增按钮操作 */
const handleCreate = () => {
  formRef.value?.open('create')
}

/** 修改按钮操作 */
const handleUpdate = (row: any) => {
  formRef.value?.open('update', row.id)
}

/** 删除按钮操作 */
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确认删除模型配置"${row.configName}"吗？有关联记录时将保留历史调用快照。`,
      '删除模型配置',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }
  try {
    await ModelConfigApi.deleteModelConfig(row.id)
    message.success('模型配置已删除')
    await getList()
    await getMetrics()
  } catch (error) {
    console.error('删除失败', error)
  }
}

/** 测试模型配置 */
const testing = ref(false)
const handleTest = async (row: any) => {
  testing.value = true
  try {
    const result = await ModelConfigApi.testModelConfig({ id: row.id })
    if (result?.success) {
      ElMessage.success(`连接成功，耗时 ${formatDuration(result.durationMs)}`)
    } else {
      ElMessage.error(result?.errorMessage || '模型测试失败')
    }
    await getList()
    await getMetrics()
  } finally {
    testing.value = false
  }
}

/** 查看调用日志 */
const handleViewLogs = (row: any) => {
  logs.model = row
  logs.current = 1
  logs.callSource = ''
  logs.status = ''
  logVisible.value = true
  loadLogs()
}

/** 加载调用日志 */
const loadLogs = async () => {
  if (!logs.model?.id) return
  logs.loading = true
  try {
    const result = await ModelConfigApi.pageModelCallLogs({
      pageNo: logs.current,
      pageSize: logs.size,
      modelConfigId: logs.model.id,
      callSource: logs.callSource || undefined,
      status: logs.status || undefined
    })
    logs.records = result?.records || []
    logs.total = Number(result?.total || 0)
  } finally {
    logs.loading = false
  }
}

/** 初始化 */
onMounted(() => {
  getList()
  getMetrics()
  getAnalytics()
})
</script>

<template>
  <div class="ai-model">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="mb-16px">
      <el-col :xs="24" :sm="12" :md="6">
        <ContentWrap>
          <div class="flex items-center">
            <div class="w-48px h-48px rounded-8px flex items-center justify-center mr-16px bg-blue-100">
              <el-icon :size="24" class="text-blue-600">
                <TrendCharts />
              </el-icon>
            </div>
            <div>
              <span class="text-gray-500 text-14px">模型配置</span>
              <strong class="block text-20px font-600">{{ formatNumber(metricsData.total) }}</strong>
              <small class="text-gray-400 text-12px">已启用 {{ formatNumber(metricsData.enabled) }}</small>
            </div>
          </div>
        </ContentWrap>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <ContentWrap>
          <div class="flex items-center">
            <div class="w-48px h-48px rounded-8px flex items-center justify-center mr-16px bg-violet-100">
              <el-icon :size="24" class="text-violet-600">
                <TrendCharts />
              </el-icon>
            </div>
            <div>
              <span class="text-gray-500 text-14px">今日调用</span>
              <strong class="block text-20px font-600">{{ formatNumber(metricsData.todayCalls) }}</strong>
              <small class="text-gray-400 text-12px">
                较昨日同期 {{ metricsData.callChangePercent > 0 ? '+' : '' }}{{ metricsData.callChangePercent }}%
              </small>
            </div>
          </div>
        </ContentWrap>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <ContentWrap>
          <div class="flex items-center">
            <div class="w-48px h-48px rounded-8px flex items-center justify-center mr-16px bg-green-100">
              <el-icon :size="24" class="text-green-600">
                <TrendCharts />
              </el-icon>
            </div>
            <div>
              <span class="text-gray-500 text-14px">调用成功率</span>
              <strong class="block text-20px font-600">
                {{ metricsData.successRate == null ? '--' : `${metricsData.successRate}%` }}
              </strong>
              <small class="text-gray-400 text-12px">失败 {{ formatNumber(metricsData.failedCalls) }} 次</small>
            </div>
          </div>
        </ContentWrap>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <ContentWrap>
          <div class="flex items-center">
            <div class="w-48px h-48px rounded-8px flex items-center justify-center mr-16px bg-amber-100">
              <el-icon :size="24" class="text-amber-600">
                <TrendCharts />
              </el-icon>
            </div>
            <div>
              <span class="text-gray-500 text-14px">平均耗时</span>
              <strong class="block text-20px font-600">{{ formatDuration(metricsData.averageDurationMs) }}</strong>
              <small class="text-gray-400 text-12px">较昨日同期 {{ formatDuration(metricsData.averageDurationChangeMs) }}</small>
            </div>
          </div>
        </ContentWrap>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <ContentWrap>
      <template #header>
        <div class="flex justify-between items-center w-full">
          <div>
            <h3 class="text-16px font-600">模型配置</h3>
            <p class="text-gray-500 text-12px">共 {{ total }} 条配置，仅管理聊天与文本生成模型</p>
          </div>
          <el-button
            v-hasPermi="['ai:model:create']"
            type="primary"
            :icon="Plus"
            @click="handleCreate"
          >
            新建模型配置
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="queryParams" :inline="true" class="mb-16px">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索配置名、供应商或模型名"
            clearable
            :prefix-icon="Search"
            @keyup.enter="handleQuery"
            @clear="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable @change="handleQuery">
            <el-option label="已启用" :value="1" />
            <el-option label="已停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="配置名称" prop="configName" min-width="180" show-overflow-tooltip />
        <el-table-column label="供应商" prop="providerName" min-width="120" show-overflow-tooltip />
        <el-table-column label="接口协议" min-width="140">
          <template #default="{ row }">
            {{ protocolLabel(row.protocol) }}
          </template>
        </el-table-column>
        <el-table-column label="模型名称" prop="modelName" min-width="150" show-overflow-tooltip />
        <el-table-column label="最大输出" min-width="120">
          <template #default="{ row }">
            {{ formatNumber(row.maxTokens) }} Token
          </template>
        </el-table-column>
        <el-table-column label="今日调用" min-width="100">
          <template #default="{ row }">
            {{ formatNumber(row.todayCalls) }} 次
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'" effect="light">
              {{ Number(row.status) === 1 ? '已启用' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              v-hasPermi="['ai:model:update']"
              link
              type="primary"
              :icon="Edit"
              @click="handleUpdate(row)"
            >
              编辑
            </el-button>
            <el-button link type="primary" @click="handleTest(row)" :loading="testing">
              测试
            </el-button>
            <el-button link type="primary" :icon="TrendCharts" @click="handleViewLogs(row)">
              日志
            </el-button>
            <el-button
              v-hasPermi="['ai:model:delete']"
              link
              type="danger"
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        class="mt-16px"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="getList"
        @size-change="handleQuery"
      />
    </ContentWrap>

    <!-- 表单弹窗 -->
    <ModelForm ref="formRef" @success="getList" />

    <!-- 调用日志弹窗 -->
    <el-dialog
      v-model="logVisible"
      width="min(1120px, 94vw)"
      :title="`${logs.model?.configName || ''} · 调用日志`"
      append-to-body
      destroy-on-close
    >
      <div class="flex gap-8px mb-16px">
        <el-select v-model="logs.callSource" clearable placeholder="全部来源" @change="loadLogs">
          <el-option label="智能体运行" value="AGENT_RUN" />
          <el-option label="手动测试" value="MANUAL_TEST" />
        </el-select>
        <el-select v-model="logs.status" clearable placeholder="全部状态" @change="loadLogs">
          <el-option label="运行中" value="RUNNING" />
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button :icon="Refresh" @click="loadLogs">刷新</el-button>
      </div>
      <el-table v-loading="logs.loading" :data="logs.records" stripe>
        <el-table-column label="开始时间" prop="startedAt" min-width="170" />
        <el-table-column label="来源" min-width="120">
          <template #default="{ row }">
            {{ row.callSource === 'MANUAL_TEST' ? '手动测试' : row.sourcePath || '智能体运行' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="110">
          <template #default="{ row }">
            {{ row.durationMs == null ? '--' : formatDuration(row.durationMs) }}
          </template>
        </el-table-column>
        <el-table-column label="Token" width="120">
          <template #default="{ row }">
            {{ row.totalTokens == null ? '--' : formatNumber(row.totalTokens) }}
          </template>
        </el-table-column>
        <el-table-column label="错误" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.errorMessage || '--' }}
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="logs.current"
        v-model:page-size="logs.size"
        class="mt-16px"
        layout="total, prev, pager, next"
        :total="logs.total"
        @current-change="loadLogs"
      />
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.ai-model {
  :deep(.el-card__header) {
    padding: 12px 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(.el-card__body) {
    padding: 16px;
  }
}
</style>
