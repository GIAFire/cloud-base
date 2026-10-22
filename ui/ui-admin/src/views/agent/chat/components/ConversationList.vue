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
      <div v-if="loading && !sessions.length" class="conversation-list__state">
        <el-icon class="is-loading"><Loading /></el-icon>
        正在加载会话
      </div>

      <div
        v-for="session in sessions"
        :key="session.id"
        class="conversation-item"
        :class="{
          'is-active': !draftSelected && session.id === activeSessionId,
          'is-deleting': deletingSessionId === session.id
        }"
      >
        <button
          type="button"
          class="conversation-item__main"
          :disabled="deletingSessionId === session.id"
          @click="$emit('select-session', session.id)"
        >
          <div class="conversation-item__title">{{ session.title || '未命名会话' }}</div>
          <div class="conversation-item__meta">
            {{ formatSessionTime(session.lastMessageAt) }}
          </div>
        </button>
        <el-tooltip content="删除会话" placement="top">
          <el-button
            class="conversation-item__delete"
            type="danger"
            text
            circle
            :aria-label="`删除会话：${session.title || '未命名会话'}`"
            :loading="deletingSessionId === session.id"
            :disabled="Boolean(deletingSessionId)"
            @click.stop="$emit('delete-session', session.id)"
          >
            <Icon v-if="deletingSessionId !== session.id" icon="ep:delete" />
          </el-button>
        </el-tooltip>
      </div>

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
  deletingSessionId?: string
}>()

const emit = defineEmits<{
  (e: 'new-session'): void
  (e: 'select-session', sessionId: string): void
  (e: 'delete-session', sessionId: string): void
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
  display: flex;
  align-items: center;
  width: calc(100% - 20px);
  margin: 10px;
  background: transparent;
  border-radius: 8px;
  transition: background 0.16s ease;

  &:hover {
    background: var(--el-fill-color-light);

    .conversation-item__delete {
      opacity: 1;
    }
  }

  &.is-active {
    background: var(--el-color-primary-light-9);

    .conversation-item__title {
      color: var(--el-color-primary);
    }

    .conversation-item__delete {
      opacity: 1;
    }
  }

  &.is-deleting .conversation-item__delete {
    opacity: 1;
  }
}

.conversation-item__main {
  flex: 1;
  min-width: 0;
  padding: 12px;
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: 0;

  &:disabled {
    cursor: default;
  }
}

.conversation-item__delete {
  flex: none;
  margin-right: 8px;
  opacity: 0;
  transition: opacity 0.16s ease;

  &:focus-visible {
    opacity: 1;
  }
}

@media (hover: none) {
  .conversation-item__delete {
    opacity: 0.72;
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
