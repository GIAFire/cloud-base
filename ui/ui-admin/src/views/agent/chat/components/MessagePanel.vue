<template>
  <section class="message-panel">
    <header class="message-panel__header">
      <el-button v-if="compact" text circle @click="$emit('open-conversations')">
        <Icon icon="ep:chat-line-square" :size="20" />
      </el-button>
      <div class="message-panel__title">
        <strong>{{ agentName || 'Agent 对话' }}</strong>
        <span>{{ sessionTitle }}</span>
      </div>
      <div class="message-panel__header-actions">
        <el-tag v-if="streaming" type="success" effect="plain" round>
          <span class="stream-dot"></span>生成中
        </el-tag>
        <el-button v-if="compact" text circle @click="$emit('open-agent')">
          <Icon icon="ep:info-filled" :size="20" />
        </el-button>
      </div>
    </header>

    <el-scrollbar ref="scrollbarRef" class="message-panel__scroll" @scroll="handleScroll">
      <div class="message-panel__messages">
        <div v-if="loadingOlder" class="message-panel__loading">正在加载更早消息...</div>
        <button
          v-else-if="hasOlder && messages.length"
          class="message-panel__older"
          type="button"
          @click="requestOlder"
        >
          加载更早消息
        </button>

        <div v-if="loading && !messages.length" class="message-panel__empty">
          <el-icon class="is-loading" :size="24"><Loading /></el-icon>
          <span>正在加载消息</span>
        </div>

        <div v-else-if="!messages.length" class="message-panel__empty">
          <div class="message-panel__empty-icon"><Icon icon="ep:magic-stick" :size="32" /></div>
          <strong>开始一段新对话</strong>
          <span>输入你的问题，第一条消息发送后会自动创建会话。</span>
        </div>

        <article
          v-for="item in messages"
          :key="item.key"
          class="chat-message"
          :class="[`is-${item.role}`, { 'is-failed': item.status === 'failed' }]"
        >
          <div class="chat-message__avatar">
            <Icon :icon="item.role === 'user' ? 'ep:user' : 'ep:cpu'" :size="18" />
          </div>
          <div class="chat-message__main">
            <div class="chat-message__name">{{
              item.role === 'user' ? '我' : agentName || 'Agent'
            }}</div>
            <details v-if="item.role === 'assistant' && item.thinking" class="thinking-panel">
              <summary>思考过程</summary>
              <div>{{ item.thinking }}</div>
            </details>
            <div class="chat-message__bubble">
              <ChatMarkdown
                v-if="item.role === 'assistant' && item.content"
                :content="item.content"
              />
              <div v-else-if="item.content" class="chat-message__plain">{{ item.content }}</div>
              <span v-else-if="item.status === 'streaming'" class="typing-indicator">
                <i></i><i></i><i></i>
              </span>
              <span v-else class="chat-message__placeholder">未生成回复</span>
            </div>
            <div v-if="item.status === 'failed'" class="chat-message__error">
              <Icon icon="ep:warning" />
              {{ item.errorMessage || '本次运行失败，可重新发送消息' }}
            </div>
          </div>
        </article>
      </div>
    </el-scrollbar>

    <footer class="message-panel__composer">
      <div class="message-panel__input-wrap">
        <el-input
          v-model="inputContent"
          type="textarea"
          resize="none"
          :autosize="{ minRows: 2, maxRows: 6 }"
          maxlength="32768"
          placeholder="输入消息，Enter 发送，Shift + Enter 换行"
          @keydown="handleKeydown"
        />
        <div class="message-panel__composer-actions">
          <span>AI也会犯错,请核实重要信息</span>
          <el-button
            type="primary"
            :loading="streaming"
            :disabled="!inputContent.trim() || streaming"
            @click="send"
          >
            <Icon v-if="!streaming" icon="ep:promotion" class="mr-4px" />
            {{ streaming ? '生成中' : '发送' }}
          </el-button>
        </div>
      </div>
    </footer>
  </section>
</template>

<script setup lang="ts">
import type { ChatViewMessage } from '../types'
import ChatMarkdown from './ChatMarkdown.vue'

defineOptions({ name: 'AgentMessagePanel' })

const props = defineProps<{
  agentName?: string
  sessionTitle: string
  messages: ChatViewMessage[]
  loading: boolean
  loadingOlder: boolean
  hasOlder: boolean
  streaming: boolean
  compact: boolean
}>()

const emit = defineEmits<{
  (e: 'send', content: string): void
  (e: 'load-older'): void
  (e: 'open-conversations'): void
  (e: 'open-agent'): void
}>()

const inputContent = ref('')
const scrollbarRef = ref()
let prependAnchorHeight: number | undefined

const scrollToBottom = async () => {
  await nextTick()
  const wrap = scrollbarRef.value?.wrapRef as HTMLElement | undefined
  if (wrap) wrap.scrollTop = wrap.scrollHeight
}

const requestOlder = () => {
  if (props.loadingOlder || !props.hasOlder) return
  const wrap = scrollbarRef.value?.wrapRef as HTMLElement | undefined
  prependAnchorHeight = wrap?.scrollHeight
  emit('load-older')
}

const handleScroll = ({ scrollTop }: { scrollTop: number }) => {
  if (scrollTop <= 12) requestOlder()
}

const send = () => {
  const content = inputContent.value.trim()
  if (!content || props.streaming) return
  inputContent.value = ''
  emit('send', content)
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key !== 'Enter' || event.shiftKey || event.isComposing) return
  event.preventDefault()
  send()
}

watch(
  () => props.messages[0]?.key,
  async () => {
    if (prependAnchorHeight === undefined) return
    await nextTick()
    const wrap = scrollbarRef.value?.wrapRef as HTMLElement | undefined
    if (wrap) wrap.scrollTop += wrap.scrollHeight - prependAnchorHeight
    prependAnchorHeight = undefined
  }
)

watch(
  () => props.messages.at(-1)?.content,
  () => {
    if (props.streaming) scrollToBottom()
  }
)

defineExpose({ scrollToBottom })
</script>

<style scoped lang="scss">
.message-panel {
  display: flex;
  height: 100%;
  min-width: 0;
  background: var(--el-bg-color-page);
  flex-direction: column;
}

.message-panel__header {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
  min-height: 64px;
  padding: 0 18px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.message-panel__title {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;

  strong,
  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  span {
    margin-top: 3px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.message-panel__header-actions {
  display: flex;
  gap: 6px;
  align-items: center;
}

.stream-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  margin-right: 5px;
  background: var(--el-color-success);
  border-radius: 50%;
  animation: pulse 1.2s infinite;
}

.message-panel__scroll {
  flex: 1;
  min-height: 0;
}

.message-panel__messages {
  width: min(860px, calc(100% - 32px));
  min-height: 100%;
  padding: 24px 0 40px;
  margin: 0 auto;
}

.message-panel__loading,
.message-panel__older {
  display: block;
  padding: 8px 12px;
  margin: 0 auto 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: transparent;
  border: 0;
}

.message-panel__older {
  color: var(--el-color-primary);
  cursor: pointer;
}

.message-panel__empty {
  display: flex;
  min-height: 380px;
  color: var(--el-text-color-secondary);
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10px;
  text-align: center;

  strong {
    font-size: 18px;
    color: var(--el-text-color-primary);
  }
}

.message-panel__empty-icon {
  display: grid;
  width: 64px;
  height: 64px;
  margin-bottom: 4px;
  color: white;
  background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-success));
  border-radius: 20px;
  place-items: center;
}

.chat-message {
  display: flex;
  gap: 10px;
  margin-bottom: 22px;
}

.chat-message.is-user {
  flex-direction: row-reverse;
}

.chat-message__avatar {
  display: grid;
  flex: 0 0 32px;
  width: 32px;
  height: 32px;
  color: white;
  background: var(--el-color-primary);
  border-radius: 10px;
  place-items: center;
}

.chat-message.is-user .chat-message__avatar {
  background: var(--el-color-info);
}

.chat-message.is-user .chat-message__main {
  display: flex;
  align-items: flex-end;
  flex-direction: column;
}

.chat-message.is-user .chat-message__name {
  text-align: right;
}

.chat-message__main {
  max-width: calc(100% - 42px);
  min-width: 0;
}

.chat-message__name {
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.chat-message__bubble {
  display: inline-block;
  max-width: 100%;
  padding: 11px 14px;
  color: var(--el-text-color-primary);
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px 14px 14px;
  box-shadow: 0 4px 14px rgb(0 0 0 / 3%);
}

.chat-message.is-user .chat-message__bubble {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-7);
  border-radius: 14px 4px 14px 14px;
}

.chat-message__plain,
.thinking-panel div {
  line-height: 1.65;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.thinking-panel {
  max-width: 100%;
  padding: 8px 10px;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
  border-radius: 8px;

  summary {
    cursor: pointer;
    user-select: none;
  }

  div {
    padding-top: 8px;
  }
}

.chat-message__error {
  display: flex;
  gap: 5px;
  align-items: center;
  margin-top: 7px;
  font-size: 12px;
  color: var(--el-color-danger);
}

.chat-message__placeholder {
  color: var(--el-text-color-secondary);
}

.typing-indicator {
  display: inline-flex;
  gap: 4px;
  align-items: center;
  min-height: 20px;

  i {
    width: 5px;
    height: 5px;
    background: var(--el-text-color-secondary);
    border-radius: 50%;
    animation: bounce 1.1s infinite ease-in-out;
  }

  i:nth-child(2) {
    animation-delay: 0.15s;
  }

  i:nth-child(3) {
    animation-delay: 0.3s;
  }
}

.message-panel__composer {
  padding: 12px 18px 16px;
  background: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color-lighter);
}

.message-panel__input-wrap {
  width: min(860px, 100%);
  padding: 10px 12px;
  margin: 0 auto;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  box-shadow: 0 6px 24px rgb(0 0 0 / 5%);

  :deep(.el-textarea__inner) {
    padding: 2px;
    box-shadow: none;
  }
}

.message-panel__composer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 8px;

  span {
    font-size: 11px;
    color: var(--el-text-color-placeholder);
  }
}

@keyframes pulse {
  50% {
    opacity: 0.35;
  }
}

@keyframes bounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
  }

  30% {
    transform: translateY(-4px);
  }
}
</style>
