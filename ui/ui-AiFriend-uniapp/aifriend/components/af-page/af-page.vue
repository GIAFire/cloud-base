<template>
  <view class="af-page" :class="{ 'af-page--center': center }">
    <text class="af-page__title">{{ title }}</text>
    <view v-if="$slots.default" class="af-page__content">
      <slot />
    </view>
  </view>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  center: {
    type: Boolean,
    default: false,
  },
});
</script>

<style scoped lang="scss">
.af-page {
  min-height: calc(100vh - var(--window-top) - var(--window-bottom));
  padding: 48rpx 32rpx;

  &--center {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &--center &__title {
    margin-bottom: 0;
  }

  &__title {
    display: block;
    margin-bottom: 32rpx;
    color: #20222a;
    font-size: 48rpx;
    font-weight: 700;
  }

  &__content {
    width: 100%;
  }
}
</style>
