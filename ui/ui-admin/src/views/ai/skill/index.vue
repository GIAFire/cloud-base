<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CircleCheck,
  Clock,
  DataLine,
  Delete,
  Document,
  Edit,
  Folder,
  MoreFilled,
  Plus,
  Refresh,
  Search,
  Setting,
  Upload,
  User
} from '@element-plus/icons-vue'
import {
  createSkill,
  createSkillPackageNode,
  deleteSkill,
  deleteSkillPackageFolder,
  deleteSkillPackageNode,
  getSkill,
  getSkillFileContent,
  getSkillMetrics,
  listRecentSkillLogs,
  listSkillFilesBySkill,
  pageSkillLogs,
  pageSkills,
  updateSkill,
  updateSkillPackageFile
} from '@/api/agent/skill'
import { getSimpleRoleList } from '@/api/system/role'

defineOptions({ name: 'AiSkill' })

const ALL_ROLES = '0'
const categories = [
  { value: 'data', label: '数据分析' },
  { value: 'report', label: '报告生成' },
  { value: 'document', label: '文档处理' },
  { value: 'code', label: '代码研发' },
  { value: 'research', label: '信息研究' },
  { value: 'file', label: '文件处理' },
  { value: 'rag', label: '知识检索' },
  { value: 'ops', label: '运维自动化' }
]
const riskOptions = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
const operationOptions = [
  { value: 'LOAD_SKILL', label: '加载技能' },
  { value: 'READ_REFERENCE', label: '读取资源' },
  { value: 'RUN_SCRIPT', label: '运行脚本' }
]

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const metrics = reactive({
  total: 0,
  enabled: 0,
  todayUses: 0,
  useChangePercent: null as number | null,
  successRate: null as number | null,
  failedUses: 0,
  averageDurationMs: null as number | null,
  averageDurationChangeMs: null as number | null
})
const filters = reactive({
  keyword: '',
  category: '',
  status: '',
  riskLevel: '',
  current: 1,
  size: 8
})

const recentLogs = ref<any[]>([])
const exceptionLogs = ref<any[]>([])
const roles = ref<any[]>([])

const formVisible = ref(false)
const formSaving = ref(false)
const formMode = ref('create')
const skillForm = reactive({
  id: null as number | null,
  skillCode: '',
  skillName: '',
  description: '',
  category: 'data',
  tags: [] as string[],
  riskLevel: 'LOW',
  status: 0,
  roleCodes: [ALL_ROLES] as string[],
  skillContent: ''
})

const editorVisible = ref(false)
const editorSaving = ref(false)
const editorSkill = ref<any>(null)
const resourceRows = ref<any[]>([])
const virtualFolders = ref<string[]>([])
const activeNode = ref({ type: 'main', path: 'SKILL.md', id: 'main' })
const editorContent = ref('')
const uploadInput = ref()

const resourceDialogVisible = ref(false)
const resourceSaving = ref(false)
const resourceForm = reactive({
  id: null as number | null,
  path: '',
  content: ''
})

const folderDialogVisible = ref(false)
const folderPath = ref('')

const logDialogVisible = ref(false)
const logLoading = ref(false)
const logRows = ref<any[]>([])
const logTotal = ref(0)
const logFilters = reactive({
  current: 1,
  size: 10,
  skillId: null as number | null,
  success: '',
  operation: ''
})

const roleOptions = computed(() => [
  { label: '所有角色', value: ALL_ROLES },
  ...roles.value
    .map((role) => ({
      label: role.name || role.roleName || role.roleCode,
      value: String(role.code || role.roleCode || '')
    }))
    .filter((role) => role.value && role.value !== ALL_ROLES)
])

const metricCards = computed(() => [
  {
    label: '技能总数',
    value: metrics.total,
    note: `${metrics.enabled} 个已启用`,
    icon: User,
    tone: 'blue'
  },
  {
    label: '今日使用',
    value: formatNumber(metrics.todayUses),
    note: changeText(metrics.useChangePercent, '%'),
    icon: DataLine,
    tone: 'cyan'
  },
  {
    label: '今日成功率',
    value: formatPercent(metrics.successRate),
    note: `失败 ${metrics.failedUses} 次`,
    icon: CircleCheck,
    tone: 'purple'
  },
  {
    label: '平均使用时长',
    value: formatDuration(metrics.averageDurationMs),
    note: changeText(metrics.averageDurationChangeMs, 'ms'),
    icon: Clock,
    tone: 'green'
  }
])

const resourceTree = computed(() => {
  const root: any[] = [
    {
      id: 'main',
      label: 'SKILL.md',
      path: 'SKILL.md',
      type: 'main',
      children: []
    }
  ]
  const folderMap = new Map<string, any>()
  const ensureFolder = (path: string): any => {
    if (!path) return null
    if (folderMap.has(path)) return folderMap.get(path)
    const parts = path.split('/')
    const parentPath = parts.slice(0, -1).join('/')
    const node = {
      id: `folder:${path}`,
      label: parts.at(-1),
      path,
      type: 'folder',
      children: []
    }
    folderMap.set(path, node)
    const parent = ensureFolder(parentPath)
    ;(parent ? parent.children : root).push(node)
    return node
  }

  virtualFolders.value.forEach(ensureFolder)
  resourceRows.value.forEach((resource) => {
    const path = resource.resourcePath || resource.relativePath
    const parts = String(path).split('/')
    const parent = ensureFolder(parts.slice(0, -1).join('/'))
    const node = {
      id: `resource:${resource.id}`,
      resourceId: resource.id,
      label: parts.at(-1),
      path,
      type: 'file',
      content: resource.resourceContent,
      loaded: resource.resourceContent !== undefined && resource.resourceContent !== null,
      children: []
    }
    ;(parent ? parent.children : root).push(node)
  })

  const sortNodes = (nodes: any[]) => {
    nodes.sort((left, right) => {
      if (left.type === 'main') return -1
      if (right.type === 'main') return 1
      if (left.type !== right.type) return left.type === 'folder' ? -1 : 1
      return left.label.localeCompare(right.label, 'zh-CN')
    })
    nodes.forEach((node) => sortNodes(node.children))
  }
  sortNodes(root)
  return root
})

function normalizeRoles(values: string[]) {
  const normalized = (values || []).map(String).filter(Boolean)
  if (normalized.includes(ALL_ROLES)) return [ALL_ROLES]
  return [...new Set(normalized)]
}

function handleRoleChange(values: string[]) {
  skillForm.roleCodes = normalizeRoles(values)
}

function categoryLabel(value: string) {
  return categories.find((item) => item.value === value)?.label || value || '未分类'
}

function riskType(value: string) {
  return {
    LOW: 'success',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  }[value] || 'info'
}

function formatNumber(value: number) {
  return new Intl.NumberFormat('zh-CN').format(Number(value || 0))
}

function formatPercent(value: number | null) {
  return value === null || value === undefined ? '--' : `${Number(value).toFixed(1)}%`
}

function formatDuration(value: number | null) {
  if (value === null || value === undefined) return '--'
  const duration = Number(value)
  return duration >= 1000 ? `${(duration / 1000).toFixed(1)}s` : `${Math.round(duration)}ms`
}

function changeText(value: number | null, unit: string) {
  if (value === null || value === undefined) return '暂无昨日同期数据'
  const number = Number(value)
  const prefix = number > 0 ? '+' : ''
  return `较昨日同期 ${prefix}${number.toFixed(1)}${unit}`
}

function operationLabel(value: string) {
  return operationOptions.find((item) => item.value === value)?.label || value || '-'
}

function generateCode(name: string) {
  const code = String(name || '')
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
  return /^[a-z]/.test(code) ? code.slice(0, 64).replace(/-+$/, '') : ''
}

function resetForm() {
  Object.assign(skillForm, {
    id: null,
    skillCode: '',
    skillName: '',
    description: '',
    category: 'data',
    tags: [],
    riskLevel: 'LOW',
    status: 0,
    roleCodes: [ALL_ROLES],
    skillContent: ''
  })
}

function buildSkillPayload(source = skillForm) {
  return {
    id: source.id,
    skillKey: source.skillCode,
    source: source.skillCode,
    skillName: source.skillName,
    name: source.skillName,
    description: source.description,
    category: source.category,
    tags: source.tags || [],
    riskLevel: source.riskLevel,
    status: Number(source.status),
    roleCodes: normalizeRoles(source.roleCodes),
    skillContent: source.skillContent,
    skillMdContent: source.skillContent
  }
}

async function loadDashboard() {
  loading.value = true
  try {
    const [metricData, pageData, recentData, failedData] = await Promise.all([
      getSkillMetrics(),
      pageSkills({
        current: filters.current,
        size: filters.size,
        keyword: filters.keyword || undefined,
        category: filters.category || undefined,
        status: filters.status === '' ? undefined : filters.status,
        riskLevel: filters.riskLevel || undefined
      }),
      listRecentSkillLogs({ limit: 6 }),
      listRecentSkillLogs({ limit: 4, success: 0 })
    ])
    Object.assign(metrics, metricData || {})
    rows.value = pageData?.records || []
    total.value = Number(pageData?.total || 0)
    recentLogs.value = recentData || []
    exceptionLogs.value = failedData || []
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  if (roles.value.length) return
  const data = await getSimpleRoleList()
  roles.value = Array.isArray(data) ? data.filter((role: any) => Number(role.status ?? 1) === 1) : []
}

function search() {
  filters.current = 1
  loadDashboard()
}

async function openCreate() {
  resetForm()
  formMode.value = 'create'
  await loadRoles()
  formVisible.value = true
}

async function openEdit(row: any) {
  const detail = await getSkill(row.id)
  resetForm()
  Object.assign(skillForm, detail)
  skillForm.tags = detail?.tags || []
  skillForm.roleCodes = normalizeRoles(detail?.roleCodes || detail?.roleIds)
  formMode.value = 'edit'
  await loadRoles()
  formVisible.value = true
}

function handleNameBlur() {
  if (formMode.value === 'create' && !skillForm.skillCode) {
    skillForm.skillCode = generateCode(skillForm.skillName)
  }
}

async function saveSkillForm() {
  if (!skillForm.skillName.trim() || !skillForm.skillCode.trim()) {
    ElMessage.warning('请填写技能名称和技能编码')
    return
  }
  if (!skillForm.description.trim()) {
    ElMessage.warning('请填写技能描述')
    return
  }
  if (!skillForm.roleCodes.length) {
    ElMessage.warning('请至少选择一个角色范围')
    return
  }
  formSaving.value = true
  try {
    if (formMode.value === 'create') {
      const created = await createSkill({
        ...buildSkillPayload(),
        status: 0
      })
      formVisible.value = false
      ElMessage.success('技能已创建，当前为停用状态')
      await loadDashboard()
      await openEditor(created)
    } else {
      await updateSkill(buildSkillPayload())
      formVisible.value = false
      ElMessage.success('技能配置已更新')
      await loadDashboard()
    }
  } finally {
    formSaving.value = false
  }
}

async function toggleStatus(row: any) {
  const detail = await getSkill(row.id)
  await updateSkill({
    ...buildSkillPayload(detail),
    status: Number(row.status) === 1 ? 0 : 1
  })
  ElMessage.success(Number(row.status) === 1 ? '技能已停用' : '技能已启用')
  await loadDashboard()
}

async function removeSkill(row: any) {
  try {
    await ElMessageBox.confirm(
      `确认删除技能"${row.skillName}"吗？有关联或使用历史时将执行逻辑删除。`,
      '删除技能',
      { type: 'warning' }
    )
  } catch {
    return
  }
  await deleteSkill(row.id)
  ElMessage.success('技能已删除')
  await loadDashboard()
}

async function openEditor(row: any) {
  const detail = row?.skillCode ? row : await getSkill(row.id)
  editorSkill.value = {
    ...detail,
    tags: detail.tags || [],
    roleCodes: normalizeRoles(detail.roleCodes || detail.roleIds)
  }
  resourceRows.value = await listSkillFilesBySkill(detail.id)
  virtualFolders.value = []
  activeNode.value = { type: 'main', path: 'SKILL.md', id: 'main' }
  editorContent.value = detail.skillContent || ''
  editorVisible.value = true
}

async function selectResource(node: any) {
  activeNode.value = node
  if (node.type === 'main') {
    editorContent.value = editorSkill.value?.skillContent || ''
    return
  }
  if (node.type === 'folder') {
    editorContent.value = ''
    return
  }
  if (!node.loaded) {
    node.content = await getSkillFileContent(node.resourceId)
    node.loaded = true
  }
  editorContent.value = node.content || ''
}

async function saveEditorContent() {
  if (!editorSkill.value || activeNode.value.type === 'folder') return
  editorSaving.value = true
  try {
    if (activeNode.value.type === 'main') {
      editorSkill.value.skillContent = editorContent.value
      const updated = await updateSkill(buildSkillPayload(editorSkill.value))
      editorSkill.value = { ...editorSkill.value, ...updated }
    } else {
      await updateSkillPackageFile({
        id: activeNode.value.resourceId,
        resourcePath: activeNode.value.path,
        resourceContent: editorContent.value
      })
      const resource = resourceRows.value.find(
        (item) => String(item.id) === String(activeNode.value.resourceId)
      )
      if (resource) resource.resourceContent = editorContent.value
      activeNode.value.content = editorContent.value
    }
    ElMessage.success('内容已保存')
    await loadDashboard()
  } finally {
    editorSaving.value = false
  }
}

function openNewResource(parentPath = '') {
  Object.assign(resourceForm, {
    id: null,
    path: parentPath ? `${parentPath}/` : '',
    content: ''
  })
  resourceDialogVisible.value = true
}

function openNewFolder(parentPath = '') {
  folderPath.value = parentPath ? `${parentPath}/` : ''
  folderDialogVisible.value = true
}

function createVirtualFolder() {
  const path = folderPath.value.trim().replace(/\/+$/, '')
  if (!path || path.includes('\\') || path.split('/').some((part) => !part || part === '.' || part === '..')) {
    ElMessage.warning('请输入有效的相对目录路径')
    return
  }
  if (!virtualFolders.value.includes(path)) virtualFolders.value.push(path)
  folderDialogVisible.value = false
}

async function saveResource() {
  if (!resourceForm.path.trim()) {
    ElMessage.warning('请填写资源相对路径')
    return
  }
  resourceSaving.value = true
  try {
    const created = await createSkillPackageNode({
      skillId: editorSkill.value!.id,
      resourcePath: resourceForm.path.trim(),
      resourceContent: resourceForm.content,
      directory: false
    })
    resourceRows.value.push(created)
    resourceDialogVisible.value = false
    ElMessage.success('资源文件已创建')
  } finally {
    resourceSaving.value = false
  }
}

async function deleteActiveNode() {
  await deleteTreeNode(activeNode.value)
}

async function deleteTreeNode(node: any) {
  if (!node || node.type === 'main') return
  try {
    await ElMessageBox.confirm(
      `确认删除"${node.path}"吗？`,
      node.type === 'folder' ? '删除目录' : '删除文件',
      { type: 'warning' }
    )
  } catch {
    return
  }
  if (node.type === 'folder') {
    await deleteSkillPackageFolder(editorSkill.value!.id, node.path)
    resourceRows.value = resourceRows.value.filter(
      (item) => !String(item.resourcePath).startsWith(`${node.path}/`)
    )
    virtualFolders.value = virtualFolders.value.filter(
      (path) => path !== node.path && !path.startsWith(`${node.path}/`)
    )
  } else {
    await deleteSkillPackageNode(node.resourceId)
    resourceRows.value = resourceRows.value.filter(
      (item) => String(item.id) !== String(node.resourceId)
    )
  }
  const activePath = activeNode.value?.path || ''
  const removedActiveNode = node.type === 'folder'
    ? activePath === node.path || activePath.startsWith(`${node.path}/`)
    : String(activeNode.value?.resourceId) === String(node.resourceId)
  if (removedActiveNode) {
    await selectResource({ type: 'main', path: 'SKILL.md', id: 'main' })
  }
  ElMessage.success('已删除')
}

async function handleTreeCommand(command: string, node: any) {
  if (command === 'new-file') {
    openNewResource(node.path)
    return
  }
  if (command === 'new-folder') {
    openNewFolder(node.path)
    return
  }
  if (command === 'delete') {
    await deleteTreeNode(node)
  }
}

function triggerUpload() {
  uploadInput.value?.click()
}

async function handleUpload(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  ;(event.target as HTMLInputElement).value = ''
  if (!file) return
  if (file.size > 1024 * 1024) {
    ElMessage.warning('资源文件不能超过 1 MiB')
    return
  }
  const parent = activeNode.value.type === 'folder' ? `${activeNode.value.path}/` : ''
  Object.assign(resourceForm, {
    path: `${parent}${file.name}`,
    content: await file.text()
  })
  await saveResource()
}

async function openLogs(options: { skillId?: number; success?: number; operation?: string } = {}) {
  Object.assign(logFilters, {
    current: 1,
    skillId: options.skillId ?? null,
    success: options.success ?? '',
    operation: options.operation ?? ''
  })
  logDialogVisible.value = true
  await loadLogs()
}

async function loadLogs() {
  logLoading.value = true
  try {
    const data = await pageSkillLogs({
      current: logFilters.current,
      size: logFilters.size,
      skillId: logFilters.skillId || undefined,
      success: logFilters.success === '' ? undefined : logFilters.success,
      operation: logFilters.operation || undefined
    })
    logRows.value = data?.records || []
    logTotal.value = Number(data?.total || 0)
  } finally {
    logLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadRoles(), loadDashboard()])
})
</script>

<template>
  <div class="skill-page">
    <!-- 指标卡片 -->
    <el-row :gutter="16" class="mb-16px">
      <el-col v-for="card in metricCards" :key="card.label" :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="flex items-center gap-14px">
            <div
              class="w-46px h-46px rounded-12px flex items-center justify-center"
              :class="{
                'bg-blue-100 text-blue-600': card.tone === 'blue',
                'bg-cyan-100 text-cyan-600': card.tone === 'cyan',
                'bg-purple-100 text-purple-600': card.tone === 'purple',
                'bg-green-100 text-green-600': card.tone === 'green'
              }"
            >
              <el-icon :size="22"><component :is="card.icon" /></el-icon>
            </div>
            <div>
              <small class="text-gray-500 text-12px">{{ card.label }}</small>
              <strong class="block text-24px font-500 mt-4px">{{ card.value }}</strong>
              <p class="text-gray-400 text-11px mt-4px">{{ card.note }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主内容区域 -->
    <el-row :gutter="16">
      <!-- 技能列表 -->
      <el-col :xs="24" :lg="18">
        <el-card shadow="never">
          <template #header>
            <div class="flex justify-between items-center w-full flex-wrap gap-16px">
              <div>
                <h3 class="text-17px font-600">技能列表</h3>
                <p class="text-gray-500 text-12px mt-4px">列表指标均来自当天真实使用日志。</p>
              </div>
              <div class="flex items-center gap-10px flex-wrap">
                <el-input
                  v-model="filters.keyword"
                  clearable
                  :prefix-icon="Search"
                  placeholder="搜索名称、编码或描述"
                  class="w-200px"
                  @clear="search"
                  @keyup.enter="search"
                />
                <el-select v-model="filters.category" clearable placeholder="全部分类" @change="search">
                  <el-option v-for="item in categories" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
                <el-select v-model="filters.status" clearable placeholder="全部状态" @change="search">
                  <el-option label="启用" :value="1" />
                  <el-option label="停用" :value="0" />
                </el-select>
                <el-select v-model="filters.riskLevel" clearable placeholder="全部风险" @change="search">
                  <el-option v-for="risk in riskOptions" :key="risk" :label="risk" :value="risk" />
                </el-select>
                <el-button :icon="Refresh" @click="loadDashboard">刷新</el-button>
                <el-button type="primary" :icon="Plus" @click="openCreate">新建技能</el-button>
              </div>
            </div>
          </template>

          <div v-if="rows.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-14px">
            <el-card v-for="row in rows" :key="row.id" shadow="hover" class="skill-card">
              <el-dropdown class="card-menu" trigger="click">
                <button
                  class="absolute top-14px right-14px w-28px h-28px rounded-6px flex items-center justify-center hover:bg-gray-100"
                  type="button"
                >
                  <el-icon class="text-gray-400"><MoreFilled /></el-icon>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="toggleStatus(row)">
                      {{ Number(row.status) === 1 ? '停用' : '启用' }}
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="removeSkill(row)">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>

              <div class="flex items-start gap-12px pr-32px mb-12px">
                <div class="w-38px h-38px rounded-10px flex items-center justify-center bg-blue-50 text-blue-500">
                  <el-icon :size="18"><Setting /></el-icon>
                </div>
                <div class="min-w-0 flex-1">
                  <div class="flex items-center gap-8px">
                    <h4 class="text-15px font-500 truncate">{{ row.skillName }}</h4>
                    <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'" size="small" effect="light">
                      {{ Number(row.status) === 1 ? '启用' : '停用' }}
                    </el-tag>
                  </div>
                  <code class="text-11px text-gray-500">{{ row.skillCode }}</code>
                </div>
              </div>

              <p class="text-13px text-gray-600 mb-10px line-clamp-2">{{ row.description }}</p>

              <div class="flex flex-wrap gap-6px mb-12px">
                <el-tag size="small">{{ categoryLabel(row.category) }}</el-tag>
                <el-tag size="small" :type="riskType(row.riskLevel)">{{ row.riskLevel }}</el-tag>
                <el-tag v-if="row.hasScripts" size="small" type="warning">含脚本</el-tag>
              </div>

              <div class="grid grid-cols-3 gap-8px mb-12px p-10px bg-gray-50 rounded-8px">
                <div class="text-center">
                  <strong class="block text-14px">{{ row.todayUses }}</strong>
                  <small class="text-10px text-gray-500">今日使用</small>
                </div>
                <div class="text-center">
                  <strong class="block text-14px">{{ row.boundAgents }}</strong>
                  <small class="text-10px text-gray-500">绑定Agent</small>
                </div>
                <div class="text-center">
                  <strong class="block text-14px">{{ formatPercent(row.successRate) }}</strong>
                  <small class="text-10px text-gray-500">成功率</small>
                </div>
              </div>

              <footer class="flex items-center gap-4px pt-10px border-t border-gray-100">
                <el-button link type="primary" @click="openEditor(row)">编辑内容</el-button>
                <el-button link @click="openEdit(row)">配置</el-button>
                <el-button link @click="openLogs({ skillId: row.id })">使用记录</el-button>
              </footer>
            </el-card>
          </div>

          <el-empty v-else description="暂无符合条件的技能">
            <el-button type="primary" @click="openCreate">新建技能</el-button>
          </el-empty>

          <el-pagination
            v-model:current-page="filters.current"
            v-model:page-size="filters.size"
            class="mt-16px justify-end"
            background
            layout="total, sizes, prev, pager, next"
            :page-sizes="[8, 16, 32]"
            :total="total"
            @change="loadDashboard"
          />
        </el-card>
      </el-col>

      <!-- 侧边栏 -->
      <el-col :xs="24" :lg="6">
        <div class="space-y-16px">
          <!-- 最近使用记录 -->
          <el-card shadow="never">
            <template #header>
              <div class="flex justify-between items-center">
                <div>
                  <h3 class="text-15px font-600">最近使用记录</h3>
                  <p class="text-gray-500 text-11px mt-4px">最近 6 条技能读取行为</p>
                </div>
                <el-button link type="primary" @click="openLogs()">查看全部</el-button>
              </div>
            </template>

            <div v-if="recentLogs.length" class="space-y-4px">
              <button
                v-for="log in recentLogs"
                :key="log.id"
                class="w-full p-9px rounded-8px text-left hover:bg-gray-50 flex items-center gap-10px"
                @click="openLogs({ skillId: log.skillId })"
              >
                <span
                  class="w-8px h-8px rounded-full flex-shrink-0"
                  :class="Number(log.success) === 0 ? 'bg-red-500' : 'bg-green-500'"
                />
                <span class="flex-1 min-w-0">
                  <strong class="block text-13px truncate">{{ log.skillName || log.skillCode || '已删除技能' }}</strong>
                  <small class="block text-11px text-gray-500 truncate">{{ log.agentName || `Agent #${log.agentId}` }} · {{ operationLabel(log.operation) }}</small>
                </span>
                <time class="text-11px text-gray-400">{{ formatDuration(log.durationMs) }}</time>
              </button>
            </div>
            <el-empty v-else :image-size="54" description="暂无使用记录" />
          </el-card>

          <!-- 异常使用 -->
          <el-card shadow="never">
            <template #header>
              <div class="flex justify-between items-center">
                <div>
                  <h3 class="text-15px font-600">异常使用</h3>
                  <p class="text-gray-500 text-11px mt-4px">最近 4 条失败记录</p>
                </div>
                <el-button link type="danger" @click="openLogs({ success: 0 })">查看全部</el-button>
              </div>
            </template>

            <div v-if="exceptionLogs.length" class="space-y-4px">
              <button
                v-for="log in exceptionLogs"
                :key="log.id"
                class="w-full p-9px rounded-8px text-left hover:bg-gray-50 flex items-center gap-10px"
                @click="openLogs({ skillId: log.skillId, success: 0 })"
              >
                <span class="w-8px h-8px rounded-full bg-red-500 flex-shrink-0" />
                <span class="flex-1 min-w-0">
                  <strong class="block text-13px truncate">{{ log.skillName || log.skillCode || '已删除技能' }}</strong>
                  <small class="block text-11px text-gray-500 truncate">{{ log.errorMessage || '技能读取失败' }}</small>
                </span>
                <time class="text-11px text-gray-400">{{ log.startedAt || '-' }}</time>
              </button>
            </div>
            <el-empty v-else :image-size="54" description="暂无异常记录" />
          </el-card>
        </div>
      </el-col>
    </el-row>

    <!-- 编辑配置弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="formMode === 'create' ? '新建技能' : '编辑技能配置'"
      width="680px"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="技能名称" required>
              <el-input
                v-model="skillForm.skillName"
                :disabled="formMode === 'edit'"
                maxlength="100"
                @blur="handleNameBlur"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="技能编码" required>
              <el-input
                v-model="skillForm.skillCode"
                :disabled="formMode === 'edit'"
                maxlength="64"
                placeholder="例如 data-analysis"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" required>
              <el-select v-model="skillForm.category" class="w-full">
                <el-option v-for="item in categories" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险等级">
              <el-select v-model="skillForm.riskLevel" class="w-full">
                <el-option v-for="risk in riskOptions" :key="risk" :label="risk" :value="risk" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="技能描述" required>
          <el-input
            v-model="skillForm.description"
            :disabled="formMode === 'edit'"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="skillForm.tags" multiple filterable allow-create default-first-option class="w-full">
            <el-option v-for="tag in skillForm.tags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
        <el-form-item label="可用角色" required>
          <el-select
            :model-value="skillForm.roleCodes"
            multiple
            class="w-full"
            @change="handleRoleChange"
          >
            <el-option v-for="role in roleOptions" :key="role.value" :label="role.label" :value="role.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formMode === 'edit'" label="状态">
          <el-switch v-model="skillForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-alert
          v-if="formMode === 'edit'"
          type="info"
          :closable="false"
          title="技能名称和描述以 SKILL.md 顶部元数据中的 name、description 为准，请在内容编辑器中修改。"
        />
        <el-alert
          v-else
          type="info"
          :closable="false"
          title="新技能默认停用。创建后将自动进入 SKILL.md 编辑器，完成内容后再启用。"
        />
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="formSaving" @click="saveSkillForm">
          {{ formMode === 'create' ? '创建并编辑内容' : '保存配置' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 内容编辑器 -->
    <el-drawer v-model="editorVisible" size="82%" :with-header="false" destroy-on-close>
      <div v-if="editorSkill" class="editor-shell h-full flex flex-col">
        <header class="flex items-center justify-between gap-16px p-16px border-b border-gray-200">
          <div>
            <span class="text-gray-500">技能内容编辑器</span>
            <h3 class="text-16px font-600 mt-5px">
              {{ editorSkill.skillName }}
              <code class="ml-8px text-12px text-gray-500">{{ editorSkill.skillCode }}</code>
            </h3>
          </div>
          <div class="flex gap-10px">
            <input ref="uploadInput" type="file" hidden @change="handleUpload">
            <el-button :icon="Upload" @click="triggerUpload">上传文本</el-button>
            <el-button type="primary" :loading="editorSaving" @click="saveEditorContent">保存当前文件</el-button>
            <el-button @click="editorVisible = false">关闭</el-button>
          </div>
        </header>
        <div class="flex flex-1 min-h-0">
          <aside class="w-280px overflow-auto p-14px bg-gray-50 border-r border-gray-200">
            <div class="flex gap-8px mb-12px">
              <el-button size="small" :icon="Plus" @click="openNewResource(activeNode.type === 'folder' ? activeNode.path : '')">文件</el-button>
              <el-button size="small" :icon="Folder" @click="openNewFolder(activeNode.type === 'folder' ? activeNode.path : '')">目录</el-button>
              <el-button
                size="small"
                type="danger"
                plain
                :icon="Delete"
                :disabled="activeNode.type === 'main'"
                @click="deleteActiveNode"
              />
            </div>
            <el-tree
              :data="resourceTree"
              node-key="id"
              default-expand-all
              highlight-current
              :expand-on-click-node="false"
              @node-click="selectResource"
            >
              <template #default="{ data }">
                <span class="flex w-full min-w-0 items-center justify-between gap-8px">
                  <span class="inline-flex min-w-0 items-center gap-6px">
                    <el-icon><Folder v-if="data.type === 'folder'" /><Document v-else /></el-icon>
                    <span class="truncate">{{ data.label }}</span>
                  </span>
                  <el-dropdown
                    v-if="data.type !== 'main'"
                    trigger="click"
                    @command="(command: string) => handleTreeCommand(command, data)"
                  >
                    <button
                      class="w-25px h-25px flex items-center justify-center rounded-6px hover:bg-blue-50 text-gray-400 hover:text-blue-600"
                      type="button"
                      @click.stop
                    >
                      <el-icon><MoreFilled /></el-icon>
                    </button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item v-if="data.type === 'folder'" command="new-file">新增文件</el-dropdown-item>
                        <el-dropdown-item v-if="data.type === 'folder'" command="new-folder">新增文件夹</el-dropdown-item>
                        <el-dropdown-item command="delete" :divided="data.type === 'folder'">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </span>
              </template>
            </el-tree>
          </aside>
          <main class="flex-1 min-w-0 flex flex-col">
            <div class="flex items-center justify-between gap-16px p-10px px-16px bg-gray-50 border-b border-gray-200 text-gray-600">
              <span>{{ activeNode.path }}</span>
              <small v-if="activeNode.type === 'folder'" class="text-gray-400">目录由资源路径自动推导，不单独入库</small>
            </div>
            <el-input
              v-if="activeNode.type !== 'folder'"
              v-model="editorContent"
              type="textarea"
              resize="none"
              spellcheck="false"
              placeholder="输入 UTF-8 文本内容"
              class="flex-1"
            />
            <el-empty v-else description="请选择文件，或在此目录下新建资源文件" class="flex-1" />
          </main>
        </div>
      </div>
    </el-drawer>

    <!-- 新建资源文件弹窗 -->
    <el-dialog v-model="resourceDialogVisible" title="新建资源文件" width="620px">
      <el-form label-position="top">
        <el-form-item label="相对路径" required>
          <el-input v-model="resourceForm.path" placeholder="例如 references/guide.md" />
        </el-form-item>
        <el-form-item label="文件内容">
          <el-input v-model="resourceForm.content" type="textarea" :rows="12" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resourceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resourceSaving" @click="saveResource">创建</el-button>
      </template>
    </el-dialog>

    <!-- 新建虚拟目录弹窗 -->
    <el-dialog v-model="folderDialogVisible" title="新建虚拟目录" width="500px">
      <el-input v-model="folderPath" placeholder="例如 references/design" />
      <template #footer>
        <el-button @click="folderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createVirtualFolder">创建</el-button>
      </template>
    </el-dialog>

    <!-- 使用记录弹窗 -->
    <el-dialog v-model="logDialogVisible" title="技能使用记录" width="1080px">
      <div class="flex gap-10px mb-14px">
        <el-select v-model="logFilters.success" clearable placeholder="全部结果" @change="loadLogs">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-select v-model="logFilters.operation" clearable placeholder="全部操作" @change="loadLogs">
          <el-option v-for="item in operationOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button v-if="logFilters.skillId" @click="logFilters.skillId = null; loadLogs()">清除技能筛选</el-button>
      </div>
      <el-table v-loading="logLoading" :data="logRows">
        <el-table-column label="技能" min-width="150">
          <template #default="{ row }">{{ row.skillName || row.skillCode || '已删除技能' }}</template>
        </el-table-column>
        <el-table-column label="Agent" min-width="140">
          <template #default="{ row }">{{ row.agentName || `Agent #${row.agentId}` }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">{{ operationLabel(row.operation) }}</template>
        </el-table-column>
        <el-table-column prop="resourcePath" label="资源路径" min-width="180" show-overflow-tooltip />
        <el-table-column label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="Number(row.success) === 1 ? 'success' : 'danger'" size="small">
              {{ Number(row.success) === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="90">
          <template #default="{ row }">{{ formatDuration(row.durationMs) }}</template>
        </el-table-column>
        <el-table-column prop="startedAt" label="开始时间" width="170" />
        <el-table-column prop="errorMessage" label="错误信息" min-width="180" show-overflow-tooltip />
      </el-table>
      <el-pagination
        v-model:current-page="logFilters.current"
        v-model:page-size="logFilters.size"
        class="mt-14px"
        background
        layout="total, sizes, prev, pager, next"
        :total="logTotal"
        @change="loadLogs"
      />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.skill-card {
  position: relative;
}

.card-menu {
  position: absolute;
  top: 14px;
  right: 14px;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

:deep(.el-textarea__inner) {
  height: 100% !important;
  padding: 20px;
  color: #d8dee9;
  background: #1f2430;
  border: 0;
  border-radius: 0;
  box-shadow: none;
  font: 14px/1.7 Consolas, Monaco, monospace;
}
</style>
