<template>
  <view v-if="open" ref="overlay" class="modal" role="dialog" aria-modal="true" :aria-label="title"
        @click.self="$emit('close')">
    <view class="dialog" :class="{ 'dialog--wide': wide }">
      <af-button class="close" aria-label="关闭弹窗" @click="$emit('close')">✕</af-button>
      <slot name="cover"/>
      <view class="content"><h2 v-if="title">{{ title }}</h2>
        <slot/>
      </view>
    </view>
  </view>
</template>
<script setup>
import {onMounted, onUnmounted, ref, watch, nextTick} from 'vue';

const props = defineProps({open: Boolean, title: {type: String, default: ''}, wide: Boolean});
const emit = defineEmits(['close']);
const overlay = ref(null);
let previousFocus;

function keydown(event) {
  if (!props.open) return;
  if (event.key === 'Escape') emit('close');
  if (event.key !== 'Tab') return;
  const element = overlay.value?.$el || overlay.value;
  const focusable = element?.querySelectorAll('button, input, textarea, [tabindex="0"]');
  if (!focusable?.length) return;
  const first = focusable[0], last = focusable[focusable.length - 1];
  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault();
    last.focus();
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault();
    first.focus();
  }
}

watch(() => props.open, async (open) => {
  // #ifdef H5
  if (open) {
    previousFocus = document.activeElement;
    await nextTick();
    const element = overlay.value?.$el || overlay.value;
    element?.querySelector('[tabindex="0"], button')?.focus();
  } else previousFocus?.focus?.();
  // #endif
}, {immediate: true});
onMounted(() => {
  // #ifdef H5
  document.addEventListener('keydown', keydown);
  // #endif
});
onUnmounted(() => {
  // #ifdef H5
  document.removeEventListener('keydown', keydown);
  // #endif
});
</script>
