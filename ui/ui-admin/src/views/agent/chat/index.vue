<template>
  <div class="agent-chat-page">
    <aside v-if="!compact" class="agent-chat-page__sessions">
      <ConversationList
        :sessions="sessions"
        :total="sessionTotal"
        :active-session-id="activeSessionId"
        :draft-selected="draftSelected"
        :loading="sessionLoading"
        :loading-more="sessionLoadingMore"
        :has-more="sessionHasMore"
        @new-session="createDraftSession"
        @select-session="selectSession"
        @load-more="loadMoreSessions"
      />
    </aside>

    <main class="agent-chat-page__messages">
      <MessagePanel
        ref="messagePanelRef"
        :agent-name="agentInfo?.agentName"
        :session-title="currentSessionTitle"
        :messages="messages"
        :loading="messageLoading"
        :loading-older="messageLoadingOlder"
        :has-older="messageHasMore"
        :streaming="streaming"
        :compact="compact"
        @send="sendMessage"
        @load-older="loadOlderMessages"
        @open-conversations="conversationDrawer = true"
        @open-agent="agentDrawer = true"
      />
    </main>

    <aside v-if="!compact" class="agent-chat-page__agent">
      <AgentInfoPanel :agent="agentInfo" :loading="agentLoading" />
    </aside>

    <el-drawer
      v-model="conversationDrawer"
      direction="ltr"
      size="min(86vw, 320px)"
      :with-header="false"
    >
      <ConversationList
        :sessions="sessions"
        :total="sessionTotal"
        :active-session-id="activeSessionId"
        :draft-selected="draftSelected"
        :loading="sessionLoading"
        :loading-more="sessionLoadingMore"
        :has-more="sessionHasMore"
        @new-session="createDraftFromDrawer"
        @select-session="selectSessionFromDrawer"
        @load-more="loadMoreSessions"
      />
    </el-drawer>

    <el-drawer v-model="agentDrawer" direction="rtl" size="min(90vw, 360px)" :with-header="false">
      <AgentInfoPanel :agent="agentInfo" :loading="agentLoading" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onBeforeRouteLeave, onBeforeRouteUpdate, useRoute } from 'vue-router'
import { InfoApi, type Info } from '@/api/agent/agentInfo'
import {
  createChatRequestId,
  getChatMessagePage,
  getChatSessionPage,
  streamChat,
  type ChatMessage,
  type ChatSession,
  type ChatStreamEvent
} from '@/api/agent/chat'
import ConversationList from './components/ConversationList.vue'
import MessagePanel from './components/MessagePanel.vue'
import AgentInfoPanel from './components/AgentInfoPanel.vue'
import type { ChatViewMessage } from './types'

defineOptions({ name: 'AgentChat' })

const route = useRoute()
const message = useMessage()
const agentId = computed(() => String(route.params.agentId || ''))

const agentInfo = ref<Info>()
const agentLoading = ref(false)
const sessions = ref<ChatSession[]>([])
const sessionTotal = ref(0)
const sessionPageNo = ref(1)
const sessionLoading = ref(false)
const sessionLoadingMore = ref(false)
const sessionHasMore = computed(() => sessions.value.length < sessionTotal.value)

const activeSessionId = ref<string>()
const draftSelected = ref(true)
const messages = ref<ChatViewMessage[]>([])
const messageLoading = ref(false)
const messageLoadingOlder = ref(false)
const messageHasMore = ref(false)
const messageBeforeSeq = ref<string>()
const messagePanelRef = ref<InstanceType<typeof MessagePanel>>()

const streaming = ref(false)
let activeAbortController: AbortController | undefined
let activeStreamTask: Promise<void> | undefined

const compact = ref(false)
const conversationDrawer = ref(false)
const agentDrawer = ref(false)

const currentSessionTitle = computed(() => {
  if (draftSelected.value) return '新对话'
  return sessions.value.find((item) => item.id === activeSessionId.value)?.title || '历史会话'
})

const updateBreakpoint = () => {
  compact.value = window.innerWidth < 1200
  if (!compact.value) {
    conversationDrawer.value = false
    agentDrawer.value = false
  }
}

const loadAgent = async () => {
  agentLoading.value = true
  try {
    agentInfo.value = await InfoApi.getInfo(agentId.value)
  } finally {
    agentLoading.value = false
  }
}

const loadSessions = async (reset = false) => {
  if (reset) {
    sessionPageNo.value = 1
    sessionLoading.value = true
  } else {
    if (sessionLoadingMore.value || !sessionHasMore.value) return
    sessionLoadingMore.value = true
  }
  try {
    const page = await getChatSessionPage({
      agentId: agentId.value,
      pageNo: sessionPageNo.value,
      pageSize: 20
    })
    sessionTotal.value = page.total
    sessions.value = reset ? page.list : mergeSessions(sessions.value, page.list)
  } finally {
    sessionLoading.value = false
    sessionLoadingMore.value = false
  }
}

const loadMoreSessions = async () => {
  if (!sessionHasMore.value || sessionLoadingMore.value) return
  sessionPageNo.value += 1
  try {
    await loadSessions(false)
  } catch {
    sessionPageNo.value -= 1
  }
}

const mergeSessions = (current: ChatSession[], incoming: ChatSession[]) => {
  const map = new Map(current.map((item) => [item.id, item]))
  incoming.forEach((item) => map.set(item.id, item))
  return Array.from(map.values())
}

const loadMessages = async (sessionId: string) => {
  messageLoading.value = true
  messages.value = []
  messageBeforeSeq.value = undefined
  messageHasMore.value = false
  try {
    const page = await getChatMessagePage({ sessionId, pageSize: 50 })
    messages.value = page.list.map(toViewMessage)
    messageBeforeSeq.value = page.nextBeforeSeq
    messageHasMore.value = page.hasMore
    await messagePanelRef.value?.scrollToBottom()
  } finally {
    messageLoading.value = false
  }
}

const loadOlderMessages = async () => {
  if (!activeSessionId.value || !messageHasMore.value || messageLoadingOlder.value) return
  messageLoadingOlder.value = true
  try {
    const page = await getChatMessagePage({
      sessionId: activeSessionId.value,
      beforeSeq: messageBeforeSeq.value,
      pageSize: 50
    })
    const known = new Set(messages.value.map((item) => item.id).filter(Boolean))
    const older = page.list.filter((item) => !known.has(item.id)).map(toViewMessage)
    messages.value = [...older, ...messages.value]
    messageBeforeSeq.value = page.nextBeforeSeq
    messageHasMore.value = page.hasMore
  } finally {
    messageLoadingOlder.value = false
  }
}

const toViewMessage = (item: ChatMessage): ChatViewMessage => ({
  key: `stored-${item.id}`,
  id: item.id,
  runId: item.runId,
  seq: item.seq,
  role: item.role === 'USER' ? 'user' : 'assistant',
  content: item.content,
  status: 'completed',
  createTime: item.createTime
})

const createDraftSession = async () => {
  if (!(await confirmAndAbortActiveRun())) return
  activeSessionId.value = undefined
  draftSelected.value = true
  messages.value = []
  messageBeforeSeq.value = undefined
  messageHasMore.value = false
}

const selectSession = async (sessionId: string) => {
  if (!draftSelected.value && activeSessionId.value === sessionId) return
  if (!(await confirmAndAbortActiveRun())) return
  activeSessionId.value = sessionId
  draftSelected.value = false
  await loadMessages(sessionId)
}

const createDraftFromDrawer = async () => {
  await createDraftSession()
  if (draftSelected.value) conversationDrawer.value = false
}

const selectSessionFromDrawer = async (sessionId: string) => {
  await selectSession(sessionId)
  if (activeSessionId.value === sessionId) conversationDrawer.value = false
}

const sendMessage = async (content: string) => {
  if (streaming.value) return
  const requestId = createChatRequestId()
  const userMessage: ChatViewMessage = {
    key: `user-${requestId}`,
    role: 'user',
    content,
    status: 'completed'
  }
  const assistantMessage = reactive<ChatViewMessage>({
    key: `assistant-${requestId}`,
    role: 'assistant',
    content: '',
    thinking: '',
    status: 'streaming'
  })
  messages.value.push(userMessage, assistantMessage)
  await messagePanelRef.value?.scrollToBottom()

  const controller = new AbortController()
  activeAbortController = controller
  streaming.value = true
  let terminalEventReceived = false

  const task = (async () => {
    try {
      await streamChat(
        {
          agentId: agentId.value,
          sessionId: activeSessionId.value,
          requestId,
          content
        },
        controller.signal,
        (event) => {
          handleStreamEvent(event, assistantMessage)
          if (event.type === 'run.completed' || event.type === 'run.failed') {
            terminalEventReceived = true
          }
        }
      )
      if (!terminalEventReceived) {
        assistantMessage.status = 'failed'
        assistantMessage.errorMessage = '连接提前结束，请重新发送消息'
      }
    } catch (error) {
      if (isAbortError(error)) {
        assistantMessage.status = 'failed'
        assistantMessage.errorMessage = '本次生成已取消'
      } else {
        assistantMessage.status = 'failed'
        assistantMessage.errorMessage = error instanceof Error ? error.message : '聊天请求失败'
      }
    } finally {
      streaming.value = false
      if (activeAbortController === controller) activeAbortController = undefined
      await loadSessions(true).catch(() => undefined)
    }
  })()

  activeStreamTask = task
  try {
    await task
  } finally {
    if (activeStreamTask === task) activeStreamTask = undefined
  }
}

const handleStreamEvent = (event: ChatStreamEvent, assistant: ChatViewMessage) => {
  assistant.runId = event.runId
  if (event.type === 'run.started') {
    if (!activeSessionId.value) {
      activeSessionId.value = event.sessionId
      draftSelected.value = false
    }
    return
  }
  if (event.type === 'message.thinking.delta') {
    assistant.thinking = (assistant.thinking || '') + (event.delta || '')
    return
  }
  if (event.type === 'message.delta') {
    assistant.content += event.delta || ''
    return
  }
  if (event.type === 'run.completed') {
    assistant.status = 'completed'
    return
  }
  if (event.type === 'run.failed') {
    assistant.status = 'failed'
    assistant.errorMessage = event.errorMessage || '本次运行失败，可重新发送消息'
  }
}

const confirmAndAbortActiveRun = async () => {
  if (!streaming.value || !activeAbortController) return true
  try {
    await message.confirm('当前回复仍在生成，切换后将取消本次运行。是否继续？', '取消当前生成')
  } catch {
    return false
  }
  activeAbortController.abort()
  await activeStreamTask?.catch(() => undefined)
  return true
}

const isAbortError = (error: unknown) =>
  error instanceof DOMException
    ? error.name === 'AbortError'
    : error instanceof Error && error.name === 'AbortError'

const initialize = async () => {
  agentInfo.value = undefined
  sessions.value = []
  sessionTotal.value = 0
  sessionPageNo.value = 1
  activeSessionId.value = undefined
  draftSelected.value = true
  messages.value = []
  await Promise.all([loadAgent(), loadSessions(true)])
}

onBeforeRouteLeave(async () => await confirmAndAbortActiveRun())
onBeforeRouteUpdate(async () => await confirmAndAbortActiveRun())

watch(agentId, () => initialize(), { immediate: true })

onMounted(() => {
  updateBreakpoint()
  window.addEventListener('resize', updateBreakpoint)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateBreakpoint)
  activeAbortController?.abort()
})
</script>

<style scoped lang="scss">
.agent-chat-page {
  display: grid;
  grid-template-columns: 280px minmax(360px, 1fr) 320px;
  width: 100%;
  height: calc(100vh - 105px);
  min-height: 600px;
  overflow: hidden;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  box-shadow: 0 8px 28px rgb(0 0 0 / 5%);
}

.agent-chat-page__sessions,
.agent-chat-page__agent,
.agent-chat-page__messages {
  min-width: 0;
  min-height: 0;
}

.agent-chat-page__sessions {
  border-right: 1px solid var(--el-border-color-lighter);
}

.agent-chat-page__agent {
  border-left: 1px solid var(--el-border-color-lighter);
}

:deep(.el-drawer__body) {
  padding: 0;
}

@media (width < 1200px) {
  .agent-chat-page {
    grid-template-columns: minmax(0, 1fr);
    height: calc(100vh - 92px);
    min-height: 520px;
  }
}
</style>
