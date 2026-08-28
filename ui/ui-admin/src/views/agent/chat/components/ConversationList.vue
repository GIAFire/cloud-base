<template>
  <section class="conversation-list">
    <div class="conversation-list__header">
      <div>
        <strong>会话</strong>
        <span>{{ total }}</span>
      </div>
      <el-button type="primary" circle @click="$emit('new-session')">
        <Icon icon="ep:plus" />
      </el-button>
    </div>

    <el-scrollbar ref="scrollbarRef" class="conversation-list__scroll" @scroll="handleScroll">
      <div v-if="draftSelected" class="conversation-item is-active" @click="$emit('new-session')">
        <div class="conversation-item__title">新对话</div>
        <div class="conversation-item__meta">发送第一条消息后保存</div>
      </div>

      <div v-if="loading && !sessions.length" class="conversation-list__state">
        <el-icon class="is-loading"><Loading /></el-icon>
        正在加载会话
      </div>

      <button
        v-for="session in sessions"
        :key="session.id"
        type="button"
        class="conversation-item"
        :class="{ 'is-active': !draftSelected && session.id === activeSessionId }"
        @click="$emit('select-session', session.id)"
      >
        <div class="conversation-item__title">{{ session.title || '未命名会话' }}</div>
        <div class="conversation-item__meta">
          {{ formatSessionTime(session.lastMessageAt) }}
        </div>
      </button>

      <div v-if="loadingMore" class="conversation-list__state">加载更多...</div>
      <div v-else-if="sessions.length && !hasMore" class="conversation-list__end">
        没有更多会话了
      </div>
      <el-empty
        v-if="!loading && !sessions.length && !draftSelected"
        :image-size="72"
        description="还没有历史会话"
      />
    </el-scrollbar>
  </section>
</template>

<script setup lang="ts">
import type { ChatSession } from '@/api/agent/chat'
import { formatPast } from '@/utils/formatTime'

defineOptions({ name: 'AgentConversationList' })

const props = defineProps<{
  sessions: ChatSession[]
  total: number
  activeSessionId?: string
  draftSelected: boolean
  loading: boolean
  loadingMore: boolean
  hasMore: boolean
}>()

const emit = defineEmits<{
  (e: 'new-session'): void
  (e: 'select-session', sessionId: string): void
  (e: 'load-more'): void
}>()

const scrollbarRef = ref()

const handleScroll = ({ scrollTop }: { scrollTop: number }) => {
  const scroller = scrollbarRef.value?.wrapRef as HTMLElement | undefined
  if (!scroller || props.loadingMore || !props.hasMore) return
  if (scroller.scrollHeight - scrollTop - scroller.clientHeight < 80) emit('load-more')
}

const formatSessionTime = (value?: number | string) => {
  if (!value) return '暂无消息'
  const date = typeof value === 'number' || /^\d+$/.test(value) ? new Date(Number(value)) : value
  return formatPast(date)
}
</script>

<style scoped lang="scss">
.conversation-list {
  display: flex;
  height: 100%;
  min-width: 0;
  background: var(--el-bg-color);
  flex-direction: column;
}

.conversation-list__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  strong {
    font-size: 16px;
  }

  span {
    margin-left: 8px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.conversation-list__scroll {
  flex: 1;
  min-height: 0;
}

.conversation-item {
  display: block;
  width: calc(100% - 20px);
  padding: 12px;
  margin: 10px;
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 8px;
  transition: background 0.16s ease;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.is-active {
    background: var(--el-color-primary-light-9);

    .conversation-item__title {
      color: var(--el-color-primary);
    }
  }
}

.conversation-item__title {
  overflow: hidden;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-item__meta {
  margin-top: 6px;
  overflow: hidden;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-list__state,
.conversation-list__end {
  padding: 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: center;
}
</style>
