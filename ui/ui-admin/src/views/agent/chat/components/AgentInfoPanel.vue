<template>
  <section class="agent-panel" v-loading="loading">
    <div class="agent-panel__header">
      <div class="agent-panel__avatar">
        <Icon icon="ep:cpu" :size="24" />
      </div>
      <div class="agent-panel__identity">
        <strong>{{ agent?.agentName || 'Agent' }}</strong>
        <span>{{ agent?.agentCode || '-' }}</span>
      </div>
    </div>

    <el-scrollbar class="agent-panel__body">
      <p class="agent-panel__description">
        {{ agent?.description || '暂无描述' }}
      </p>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="Agent ID">{{ agent?.id || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模型 ID">{{ agent?.modelId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提示词 ID">{{
          agent?.sysPromptId || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="分类">
          <div v-if="agent?.categories?.length" class="agent-panel__taxonomy">
            <el-tag
              v-for="category in agent.categories"
              :key="category.id"
              effect="plain"
              size="small"
              :type="category.status === CommonStatusEnum.DISABLE ? 'warning' : 'primary'"
            >
              {{ category.name
              }}{{ category.status === CommonStatusEnum.DISABLE ? '（已禁用）' : '' }}
            </el-tag>
          </div>
          <div v-else-if="agent?.categoryIds?.length" class="agent-panel__taxonomy">
            <el-tag
              v-for="categoryId in agent.categoryIds"
              :key="categoryId"
              effect="plain"
              size="small"
            >
              #{{ categoryId }}
            </el-tag>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="标签">
          <div v-if="agent?.tags?.length" class="agent-panel__taxonomy">
            <el-tag
              v-for="tag in agent.tags"
              :key="tag.id"
              effect="plain"
              size="small"
              :type="tag.status === CommonStatusEnum.DISABLE ? 'warning' : 'info'"
            >
              {{ tag.name }}{{ tag.status === CommonStatusEnum.DISABLE ? '（已禁用）' : '' }}
            </el-tag>
          </div>
          <div v-else-if="agent?.tagIds?.length" class="agent-panel__taxonomy">
            <el-tag
              v-for="tagId in agent.tagIds"
              :key="tagId"
              effect="plain"
              size="small"
              type="info"
            >
              #{{ tagId }}
            </el-tag>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="最大循环">{{ agent?.maxIters ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="权限模式">
          {{ agent?.permissionMode || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="上下文压缩">
          {{ yesOrNo(agent?.compactionEnabled) }}
        </el-descriptions-item>
        <el-descriptions-item label="长期记忆">
          {{ yesOrNo(agent?.memoryEnable) }}
        </el-descriptions-item>
        <el-descriptions-item label="Plan Mode">
          {{ yesOrNo(agent?.planModeEnabled) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-scrollbar>
  </section>
</template>

<script setup lang="ts">
import type { Info } from '@/api/agent/agentInfo'
import { CommonStatusEnum } from '@/utils/constants'

defineOptions({ name: 'AgentInfoPanel' })

defineProps<{
  agent?: Info
  loading: boolean
}>()

const yesOrNo = (value?: boolean) => (value === undefined ? '-' : value ? '是' : '否')
</script>

<style scoped lang="scss">
.agent-panel {
  display: flex;
  height: 100%;
  min-width: 0;
  background: var(--el-bg-color);
  flex-direction: column;
}

.agent-panel__header {
  display: flex;
  gap: 12px;
  align-items: center;
  min-height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.agent-panel__avatar {
  display: grid;
  width: 40px;
  height: 40px;
  color: white;
  background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-success));
  border-radius: 12px;
  place-items: center;
}

.agent-panel__identity {
  display: flex;
  min-width: 0;
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

.agent-panel__body {
  flex: 1;
  min-height: 0;
  padding: 16px;
}

.agent-panel__description {
  padding: 12px;
  margin: 0 0 16px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.agent-panel__taxonomy {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
</style>
