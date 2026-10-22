<template>
  <view class="api-result">
    <view class="api-result__header">
      <text class="api-result__title">接口连通性</text>
      <text class="api-result__status" :class="`api-result__status--${status}`">
        {{ statusText }}
      </text>
    </view>

    <text class="api-result__endpoint">GET /app-api/agent/test/get</text>

    <view v-if="status === 'loading'" class="api-result__message">正在连接后端...</view>
    <view v-else-if="status === 'error'" class="api-result__message api-result__message--error">
      {{ errorMessage }}
    </view>
    <scroll-view v-else-if="status === 'success'" class="api-result__json" scroll-x>
      <text selectable>{{ formattedResult }}</text>
    </scroll-view>

    <button v-if="status !== 'loading'" class="api-result__retry" size="mini" @click="emit('retry')">
      重新测试
    </button>
  </view>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  status: {
    type: String,
    default: 'idle',
  },
  result: {
    type: [Object, Array, String, Number, Boolean],
    default: null,
  },
  errorMessage: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['retry']);

const statusText = computed(() => {
  const labels = {
    idle: '待测试',
    loading: '连接中',
    success: '连接成功',
    error: '连接失败',
  };
  return labels[props.status] || labels.idle;
});

const formattedResult = computed(() => {
  if (typeof props.result === 'string') return props.result;
  return JSON.stringify(props.result, null, 2);
});
</script>

<style scoped lang="scss">
.api-result {
  padding: 32rpx;
  border: 2rpx solid #ececf2;
  border-radius: 24rpx;
  background: #ffffff;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 18rpx;
  }

  &__title {
    font-size: 30rpx;
    font-weight: 700;
  }

  &__status {
    padding: 8rpx 16rpx;
    border-radius: 99rpx;
    color: #666b79;
    background: #f0f1f5;
    font-size: 22rpx;

    &--success {
      color: #137a50;
      background: #e8f7ef;
    }

    &--error {
      color: #b42318;
      background: #feeceb;
    }
  }

  &__endpoint {
    display: block;
    margin-bottom: 24rpx;
    color: #6d7280;
    font-family: monospace;
    font-size: 24rpx;
  }

  &__message,
  &__json {
    min-height: 120rpx;
    padding: 24rpx;
    border-radius: 16rpx;
    background: #f7f7fa;
    color: #454957;
    font-size: 24rpx;
  }

  &__message--error {
    color: #b42318;
  }

  &__json {
    width: 100%;
    white-space: pre;
  }

  &__retry {
    margin: 24rpx 0 0;
    color: #665cf6;
    background: #efedff;
  }
}
</style>
