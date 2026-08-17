<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowRight, Box, Briefcase, DataLine, Document, Finished, Lock, MoreFilled, Refresh, Search, Setting } from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import * as ToolApi from '@/api/agent/tool'
import * as RoleApi from '@/api/system/role'

defineOptions({ name: 'AiTool' })

const loading = ref(false)
const tools = ref<any[]>([])
const groups = ref<any[]>([])
const logs = ref<any[]>([])
const permissions = ref<any[]>([])
const metricData = ref<any>(null)
const roles = ref<any[]>([])
const activeTool = ref<any>(null)

const queryParams = reactive({
  keyword: '',
  category: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 8
})

const permissionPagination = reactive({
  currentPage: 1,
  pageSize: 10
})

const logPagination = reactive({
  currentPage: 1,
  pageSize: 10
})

const logDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const permissionEditorVisible = ref(false)
const logTotal = ref(0)
const permissionTotal = ref(0)
const logFilterStatus = ref('')

const permissionForm = reactive({
  id: '',
  toolId: '',
  roleId: '',
  behavior: 'ASK',
  description: '',
  ruleContent: '',
  status: 1
})

const metrics = computed(() => {
  const data = metricData.value || {}
  const callChange = data.callChangePercent
  const successRate = data.successRate

  return [
    {
      label: '可用工具',
      value: data.availableTools ?? '--',
      sub: `${data.enabledTools ?? 0} 个已启用`,
      icon: Briefcase,
      tone: 'blue'
    },
    {
      label: '工具分组',
      value: data.enabledGroups ?? '--',
      sub: '已启用分组',
      icon: Box,
      tone: 'indigo'
    },
    {
      label: '今日调用',
      value: data.todayCalls == null ? '--' : formatCompact(data.todayCalls),
      sub: callChange == null ? '昨日同期无调用' : `较昨日同期 ${callChange >= 0 ? '+' : ''}${callChange}%`,
      icon: DataLine,
      tone: 'cyan'
    },
    {
      label: '成功率',
      value: successRate == null ? '--' : `${Number(successRate).toFixed(1)}%`,
      sub: `失败 ${data.failedCalls ?? 0} 次`,
      icon: Finished,
      tone: 'green'
    }
  ]
})

const toolTypeOptions = computed(() => {
  return [...new Set(tools.value.map((row: any) => row.toolType).filter(Boolean))]
})

const filteredTools = computed(() => {
  const keyword = queryParams.keyword.trim().toLowerCase()
  return tools.value.filter((row: any) => {
    const matchKeyword = !keyword || [row.toolName, row.toolNameExplain, row.description, row.toolType]
      .some((value) => String(value || '').toLowerCase().includes(keyword))
    const matchCategory = !queryParams.category || row.toolType === queryParams.category
    const matchStatus = !queryParams.status || row.status === queryParams.status
    return matchKeyword && matchCategory && matchStatus
  })
})

const pagedTools = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  return filteredTools.value.slice(start, start + pagination.pageSize)
})

const currentTabTotal = computed(() => filteredTools.value.length)

const failedLogs = computed(() => {
  return logs.value.filter((row: any) => String(row.successStatus).toUpperCase() === 'FAILED').slice(0, 4)
})

const loadDashboard = async () => {
  loading.value = true
  try {
    const [toolResult, groupResult, metricResult, logResult, permissionResult] = await Promise.all([
      ToolApi.listTools(),
      ToolApi.listToolGroups(),
      ToolApi.getToolMetrics(),
      ToolApi.pageToolCallLogs({ current: 1, size: 4, successStatus: 'FAILED' }),
      ToolApi.pageToolPermissions({ current: 1, size: 4 })
    ])
    tools.value = Array.isArray(toolResult) ? toolResult : []
    groups.value = Array.isArray(groupResult) ? groupResult : []
    metricData.value = metricResult || null
    logs.value = logResult?.records || []
    permissions.value = permissionResult?.records || []
  } finally {
    loading.value = false
  }
}

const loadPermissionPage = async () => {
  const result = await ToolApi.pageToolPermissions({
    current: permissionPagination.currentPage,
    size: permissionPagination.pageSize,
    toolId: activeTool.value?.id || undefined
  })
  permissions.value = result?.records || []
  permissionTotal.value = Number(result?.total || 0)
}

const openPermissionDialog = async () => {
  activeTool.value = null
  permissionPagination.currentPage = 1
  permissionDialogVisible.value = true
  await loadPermissionPage()
}

const loadLogPage = async () => {
  const result = await ToolApi.pageToolCallLogs({
    current: logPagination.currentPage,
    size: logPagination.pageSize,
    toolId: activeTool.value?.id || undefined,
    successStatus: logFilterStatus.value || undefined
  })
  logs.value = result?.records || []
  logTotal.value = Number(result?.total || 0)
}

const openToolCalls = async (tool: any = null, status = '') => {
  activeTool.value = tool
  logFilterStatus.value = status
  logPagination.currentPage = 1
  logDialogVisible.value = true
  await loadLogPage()
}

const riskClass = (riskLevel: string) => {
  const value = String(riskLevel || '').toUpperCase()
  if (value === 'HIGH') return 'high'
  if (value === 'MEDIUM') return 'medium'
  return 'low'
}

const behaviorType = (behavior: string) => {
  const value = String(behavior || '').toUpperCase()
  if (value === 'DENY') return 'danger'
  if (value === 'ASK') return 'warning'
  return 'success'
}

const formatNumber = (value: number) => Number(value || 0).toLocaleString('en-US')

const formatCompact = (value: number) => {
  const number = Number(value || 0)
  if (number >= 1000000) return `${(number / 1000000).toFixed(1)}M`
  if (number >= 1000) return `${(number / 1000).toFixed(number >= 10000 ? 1 : 0)}K`
  return String(number)
}

onMounted(loadDashboard)
</script>

<template>
  <ContentWrap>
    <!-- 指标卡片 -->
    <div class="mb-4 grid grid-cols-4 gap-4">
      <el-card v-for="item in metrics" :key="item.label" shadow="hover" class="text-center">
        <div class="flex items-center gap-3">
          <div class="flex h-12 w-12 items-center justify-center rounded-lg" :class="`bg-${item.tone}-100 text-${item.tone}-600`">
            <el-icon :size="24"><component :is="item.icon" /></el-icon>
          </div>
          <div class="text-left">
            <div class="text-xs text-gray-500">{{ item.label }}</div>
            <div class="text-2xl font-bold">{{ item.value }}</div>
            <div class="text-xs text-gray-400">{{ item.sub }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 工具列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-bold">工具列表</h3>
            <p class="text-sm text-gray-500">共 {{ filteredTools.length }} 个工具</p>
          </div>
          <div class="flex gap-2">
            <el-input v-model="queryParams.keyword" clearable :prefix-icon="Search" placeholder="搜索工具名称或编码..." style="width: 250px" />
            <el-select v-model="queryParams.category" clearable placeholder="全部分类">
              <el-option v-for="type in toolTypeOptions" :key="type" :label="type" :value="type" />
            </el-select>
            <el-button :icon="Refresh" @click="loadDashboard">刷新</el-button>
          </div>
        </div>
      </template>

      <div v-if="pagedTools.length" class="grid grid-cols-4 gap-4">
        <el-card v-for="tool in pagedTools" :key="tool.id" shadow="hover" class="cursor-pointer">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <h4 class="font-bold">{{ tool.toolName }}</h4>
              <p class="mt-1 text-sm text-gray-500">{{ tool.description }}</p>
            </div>
            <el-dropdown trigger="click">
              <el-button link><el-icon><MoreFilled /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="openToolCalls(tool)">查看调用</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="mt-3 grid grid-cols-3 gap-2 text-center text-sm">
            <div>
              <div class="text-gray-500">调用次数</div>
              <div class="font-bold">{{ formatNumber(tool.calls || 0) }}</div>
            </div>
            <div>
              <div class="text-gray-500">Agent</div>
              <div class="font-bold">{{ tool.agents || 0 }}</div>
            </div>
            <div>
              <div class="text-gray-500">平均耗时</div>
              <div class="font-bold">{{ tool.avgLatency || 0 }}ms</div>
            </div>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无符合条件的工具" />

      <div class="mt-4 flex items-center justify-between">
        <span class="text-sm text-gray-500">共 {{ currentTabTotal }} 项</span>
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          background
          layout="prev, pager, next, sizes"
          :page-sizes="[8, 16, 32]"
          :total="currentTabTotal"
        />
      </div>
    </el-card>

    <!-- 调用日志弹窗 -->
    <el-dialog v-model="logDialogVisible" title="调用日志" width="900px">
      <el-table :data="logs" stripe>
        <el-table-column prop="toolName" label="工具" min-width="150" />
        <el-table-column prop="agentName" label="Agent" min-width="150" />
        <el-table-column prop="permissionBehavior" label="权限" width="100">
          <template #default="{ row }">
            <el-tag :type="behaviorType(row.permissionBehavior)">{{ row.permissionBehavior }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="successStatus" label="状态" width="100" />
        <el-table-column prop="durationMs" label="耗时" width="100">
          <template #default="{ row }">{{ row.durationMs }}ms</template>
        </el-table-column>
        <el-table-column prop="startedAt" label="开始时间" min-width="170" />
      </el-table>
      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="logPagination.currentPage"
          v-model:page-size="logPagination.pageSize"
          background
          layout="total, prev, pager, next, sizes"
          :page-sizes="[10, 20, 50]"
          :total="logTotal"
        />
      </div>
    </el-dialog>

    <!-- 权限规则弹窗 -->
    <el-dialog v-model="permissionDialogVisible" title="权限规则" width="900px">
      <el-table :data="permissions" stripe>
        <el-table-column prop="toolName" label="工具" min-width="160" />
        <el-table-column prop="roleCode" label="角色" min-width="130" />
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="behavior" label="权限" width="110">
          <template #default="{ row }">
            <el-tag :type="behaviorType(row.behavior)">{{ row.behavior }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="变更时间" min-width="170" />
      </el-table>
      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="permissionPagination.currentPage"
          v-model:page-size="permissionPagination.pageSize"
          background
          layout="total, prev, pager, next, sizes"
          :page-sizes="[10, 20, 50]"
          :total="permissionTotal"
        />
      </div>
    </el-dialog>
  </ContentWrap>
</template>

<style scoped lang="scss">
.grid-cols-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}
</style>
