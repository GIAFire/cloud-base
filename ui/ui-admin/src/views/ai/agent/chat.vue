<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bottom, ChatLineRound, Close, Delete, MoreFilled, Plus, Promotion, Search } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import {
  chatStream,
  createAgentSession,
  deleteAgentSession,
  pageAgentMessages,
  pageAgentSessions
} from '@/api/agent/chat'
import { ContentWrap } from '@/components/ContentWrap'

defineOptions({ name: 'AiAgentChat' })

const route = useRoute()
const markdownRenderer = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true
})
const defaultLinkOpenRenderer = markdownRenderer.renderer.rules.link_open || ((tokens, index, options, env, self) => self.renderToken(tokens, index, options))

markdownRenderer.renderer.rules.link_open = (tokens, index, options, env, self) => {
  tokens[index].attrSet('target', '_blank')
  tokens[index].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpenRenderer(tokens, index, options, env, self)
}

interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  createdAt: string
  streaming?: boolean
}

interface Session {
  id: string
  title: string
  messages: Message[]
  hasMore?: boolean
}

const sessions = ref<Session[]>([])
const activeSessionId = ref('')
const inputMessage = ref('')
const sessionKeyword = ref('')
const streaming = ref(false)
const sessionCreating = ref(false)
const autoScrollEnabled = ref(true)
const sessionListRef = ref()
const messageListRef = ref()
const abortController = ref<AbortController | null>(null)

let scrollFrame: number | null = null
let sessionSearchTimer: ReturnType<typeof setTimeout> | null = null

const SESSION_PAGE_SIZE = 20
const MESSAGE_PAGE_SIZE = 30

const sessionPaging = reactive({
  current: 0,
  size: SESSION_PAGE_SIZE,
  total: 0,
  loading: false,
  finished: false
})

const agentInfo = reactive({
  id: null as string | null,
  name: '',
  key: ''
})

const agentId = computed(() => {
  const value = route.params.agentId
  return Array.isArray(value) ? String(value[0] || '') : String(value || '')
})

const activeSession = computed(() => {
  return sessions.value.find((item) => item.id === activeSessionId.value) || null
})

const activeMessages = computed(() => {
  return activeSession.value?.messages || []
})

const canSend = computed(() => {
  return Boolean(inputMessage.value.trim()) && !streaming.value && !sessionCreating.value && Boolean(agentId.value)
})

const formatDateTime = (value: string) => value ? String(value).replace('T', ' ') : '--'

const renderMarkdown = (content: string) => {
  return markdownRenderer.render(content || '')
}

const scrollToBottom = (force = false) => {
  if (scrollFrame) {
    cancelAnimationFrame(scrollFrame)
  }
  scrollFrame = requestAnimationFrame(() => {
    const container = messageListRef.value
    if (!container) return
    if (force || autoScrollEnabled.value) {
      container.scrollTop = container.scrollHeight
    }
  })
}

const loadSessions = async (append = false) => {
  if (sessionPaging.loading || sessionPaging.finished) return

  sessionPaging.loading = true
  try {
    const params = {
      current: append ? sessionPaging.current + 1 : 1,
      size: SESSION_PAGE_SIZE,
      keyword: sessionKeyword.value || undefined
    }
    const data = await pageAgentSessions(params)
    const records = (data?.records || []).map((item: any) => ({
      id: String(item.id),
      title: item.title || item.sessionName || '新会话',
      messages: [],
      hasMore: false
    }))

    if (append) {
      sessions.value.push(...records)
    } else {
      sessions.value = records
    }

    sessionPaging.current = params.current
    sessionPaging.total = Number(data?.total || 0)
    sessionPaging.finished = sessions.value.length >= sessionPaging.total

    if (!activeSessionId.value && sessions.value.length) {
      await selectSession(sessions.value[0].id)
    }
  } finally {
    sessionPaging.loading = false
  }
}

const loadMessages = async (sessionId: string) => {
  const session = sessions.value.find((item) => item.id === sessionId)
  if (!session) return

  try {
    const data = await pageAgentMessages({
      current: 1,
      size: MESSAGE_PAGE_SIZE,
      sessionId
    })
    session.messages = (data?.records || []).map((item: any) => ({
      id: String(item.id),
      role: item.role || 'user',
      content: item.content || '',
      createdAt: item.createdAt || item.createTime || ''
    }))
    session.hasMore = session.messages.length < (data?.total || 0)
  } catch (error) {
    console.error('加载消息失败', error)
  }
}

const selectSession = async (sessionId: string) => {
  activeSessionId.value = sessionId
  await loadMessages(sessionId)
  nextTick(() => scrollToBottom(true))
}

const createSession = async () => {
  if (!agentId.value) return

  sessionCreating.value = true
  try {
    const data = await createAgentSession({
      agentId: agentId.value,
      title: '新会话'
    })
    const newSession: Session = {
      id: String(data.id),
      title: data.title || '新会话',
      messages: [],
      hasMore: false
    }
    sessions.value.unshift(newSession)
    activeSessionId.value = newSession.id
  } catch (error) {
    console.error('创建会话失败', error)
    ElMessage.error('创建会话失败')
  } finally {
    sessionCreating.value = false
  }
}

const removeSession = async (session: Session) => {
  try {
    await ElMessageBox.confirm('确定删除该会话吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  try {
    await deleteAgentSession(session.id)
    const index = sessions.value.findIndex((item) => item.id === session.id)
    if (index >= 0) {
      sessions.value.splice(index, 1)
      if (activeSessionId.value === session.id) {
        activeSessionId.value = sessions.value.length ? sessions.value[0].id : ''
        if (activeSessionId.value) {
          await loadMessages(activeSessionId.value)
        }
      }
    }
    ElMessage.success('会话已删除')
  } catch (error) {
    console.error('删除会话失败', error)
    ElMessage.error('删除会话失败')
  }
}

const sendMessage = async () => {
  if (!canSend.value || !activeSession.value) return

  const content = inputMessage.value.trim()
  inputMessage.value = ''

  const userMessage: Message = {
    id: `user-${Date.now()}`,
    role: 'user',
    content,
    createdAt: new Date().toISOString()
  }
  activeSession.value.messages.push(userMessage)
  nextTick(() => scrollToBottom(true))

  const assistantMessage: Message = {
    id: `assistant-${Date.now()}`,
    role: 'assistant',
    content: '',
    createdAt: new Date().toISOString(),
    streaming: true
  }
  activeSession.value.messages.push(assistantMessage)

  streaming.value = true
  abortController.value = new AbortController()

  try {
    await chatStream(
      {
        agentId: agentId.value,
        sessionId: activeSessionId.value,
        message: content
      },
      {
        onMessage: (delta) => {
          assistantMessage.content += delta
          nextTick(() => scrollToBottom())
        },
        onDone: () => {
          assistantMessage.streaming = false
          streaming.value = false
          abortController.value = null
        },
        onError: (error) => {
          assistantMessage.streaming = false
          assistantMessage.content += `\n\n**错误**: ${error}`
          streaming.value = false
          abortController.value = null
          ElMessage.error(error || '对话请求失败')
        }
      },
      {
        signal: abortController.value.signal
      }
    )
  } catch (error: any) {
    if (error.name !== 'AbortError') {
      assistantMessage.streaming = false
      assistantMessage.content += `\n\n**错误**: ${error.message || '对话请求失败'}`
      streaming.value = false
      abortController.value = null
      ElMessage.error(error.message || '对话请求失败')
    }
  }
}

const stopStreaming = () => {
  if (abortController.value) {
    abortController.value.abort()
    streaming.value = false
    abortController.value = null
  }
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const handleScroll = () => {
  const container = messageListRef.value
  if (!container) return
  const threshold = 64
  autoScrollEnabled.value = container.scrollHeight - container.scrollTop - container.clientHeight < threshold
}

const searchSessions = () => {
  if (sessionSearchTimer) {
    clearTimeout(sessionSearchTimer)
  }
  sessionSearchTimer = setTimeout(() => {
    sessionPaging.current = 0
    sessionPaging.finished = false
    loadSessions()
  }, 300)
}

onMounted(async () => {
  agentInfo.id = agentId.value
  agentInfo.name = String(route.query.agentName || '')
  agentInfo.key = String(route.query.agentKey || '')

  await loadSessions()
})

onBeforeUnmount(() => {
  if (scrollFrame) {
    cancelAnimationFrame(scrollFrame)
  }
  if (sessionSearchTimer) {
    clearTimeout(sessionSearchTimer)
  }
  stopStreaming()
})

watch(agentId, async (newId) => {
  if (newId) {
    agentInfo.id = newId
    sessions.value = []
    activeSessionId.value = ''
    sessionPaging.current = 0
    sessionPaging.finished = false
    await loadSessions()
  }
})
</script>

<template>
  <ContentWrap class="chat-page">
    <aside class="session-sidebar">
      <header class="sidebar-header">
        <div class="agent-info">
          <h3>{{ agentInfo.name || '智能体对话' }}</h3>
          <code>{{ agentInfo.key || agentId }}</code>
        </div>
        <el-button
          type="primary"
          :icon="Plus"
          size="small"
          :loading="sessionCreating"
          @click="createSession"
        >
          新会话
        </el-button>
      </header>

      <div class="session-search">
        <el-input
          v-model="sessionKeyword"
          :prefix-icon="Search"
          placeholder="搜索会话"
          clearable
          @input="searchSessions"
        />
      </div>

      <div ref="sessionListRef" class="session-list">
        <button
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: session.id === activeSessionId }"
          type="button"
          @click="selectSession(session.id)"
        >
          <span class="session-title">{{ session.title }}</span>
          <el-dropdown trigger="click" @click.stop>
            <button class="session-menu-button" type="button">
              <el-icon><MoreFilled /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="Delete" @click="removeSession(session)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </button>
        <el-empty v-if="!sessions.length" description="暂无会话" :image-size="64" />
      </div>
    </aside>

    <section class="chat-main">
      <div v-if="activeSession" class="chat-container">
        <header class="chat-header">
          <h3>{{ activeSession.title }}</h3>
        </header>

        <div ref="messageListRef" class="message-list" @scroll="handleScroll">
          <div
            v-for="message in activeMessages"
            :key="message.id"
            class="message-item"
            :class="message.role"
          >
            <div class="message-avatar">
              <el-icon v-if="message.role === 'user'"><ChatLineRound /></el-icon>
              <el-icon v-else><Promotion /></el-icon>
            </div>
            <div class="message-content">
              <div
                v-if="message.role === 'assistant'"
                class="message-text markdown-body"
                v-html="renderMarkdown(message.content)"
              />
              <div v-else class="message-text">{{ message.content }}</div>
              <div v-if="message.streaming" class="streaming-indicator">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>
          <el-empty
            v-if="!activeMessages.length"
            description="开始对话吧"
            :image-size="120"
          />
        </div>

        <footer class="chat-footer">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            :disabled="streaming"
            placeholder="输入消息，按 Enter 发送，Shift+Enter 换行"
            @keydown="handleKeydown"
          />
          <div class="footer-actions">
            <el-button v-if="streaming" type="danger" @click="stopStreaming">
              <el-icon><Close /></el-icon>
              停止
            </el-button>
            <el-button
              v-else
              type="primary"
              :icon="Promotion"
              :disabled="!canSend"
              @click="sendMessage"
            >
              发送
            </el-button>
          </div>
        </footer>
      </div>
      <el-empty v-else description="请选择或创建会话" :image-size="160">
        <el-button type="primary" :icon="Plus" @click="createSession">创建会话</el-button>
      </el-empty>
    </section>
  </ContentWrap>
</template>

<style scoped>
.chat-page {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 0;
  height: calc(100vh - 180px);
  min-height: 600px;
  padding: 0 !important;
  overflow: hidden;
}

.session-sidebar {
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e5eaf2;
  background: #f8fafc;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #e5eaf2;
}

.agent-info h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.agent-info code {
  display: block;
  margin-top: 4px;
  color: #6b7280;
  font-size: 11px;
}

.session-search {
  padding: 12px 16px;
  border-bottom: 1px solid #e5eaf2;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 10px 12px;
  border: 0;
  border-radius: 8px;
  color: inherit;
  text-align: left;
  background: transparent;
  cursor: pointer;
  transition: background 0.2s;
}

.session-item:hover {
  background: #eef2f7;
}

.session-item.active {
  background: #e0e7ff;
}

.session-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.session-menu-button {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  border: 0;
  border-radius: 4px;
  color: #6b7280;
  background: transparent;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.session-item:hover .session-menu-button {
  opacity: 1;
}

.chat-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #fff;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e5eaf2;
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  display: grid;
  width: 36px;
  height: 36px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  background: #4f46e5;
  font-size: 18px;
}

.message-item.user .message-avatar {
  background: #059669;
}

.message-content {
  min-width: 0;
  max-width: 80%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  background: #f3f4f6;
  font-size: 14px;
  line-height: 1.6;
}

.message-item.user .message-text {
  background: #dbeafe;
}

.message-item.assistant .message-text {
  background: #f3f4f6;
}

.markdown-body :deep(p) {
  margin: 0 0 8px;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(pre) {
  margin: 8px 0;
  padding: 12px;
  border-radius: 8px;
  background: #1f2937;
  color: #e5e7eb;
  overflow-x: auto;
}

.markdown-body :deep(code) {
  padding: 2px 6px;
  border-radius: 4px;
  background: #e5e7eb;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}

.markdown-body :deep(pre code) {
  padding: 0;
  background: transparent;
}

.streaming-indicator {
  display: flex;
  gap: 4px;
  margin-top: 8px;
}

.streaming-indicator .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #4f46e5;
  animation: bounce 1.4s infinite ease-in-out both;
}

.streaming-indicator .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.streaming-indicator .dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.chat-footer {
  padding: 16px 20px;
  border-top: 1px solid #e5eaf2;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

@media (max-width: 768px) {
  .chat-page {
    grid-template-columns: 1fr;
  }

  .session-sidebar {
    display: none;
  }
}
</style>
