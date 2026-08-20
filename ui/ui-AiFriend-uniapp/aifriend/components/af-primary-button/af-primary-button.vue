<template>
  <button class="af-button" :disabled="disabled || loading" :loading="loading" @click="emit('click')">
    <slot />
  </button>
</template>

<script setup>
defineProps({
  disabled: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['click']);
</script>

<style scoped lang="scss">
.af-button {
  height: 92rpx;
  border-radius: 18rpx;
  color: #ffffff;
  background: #665cf6;
  font-size: 30rpx;
  font-weight: 600;
  line-height: 92rpx;

  &[disabled] {
    color: rgba(255, 255, 255, 0.8);
    background: #a8a3fa;
  }
}
</style>
