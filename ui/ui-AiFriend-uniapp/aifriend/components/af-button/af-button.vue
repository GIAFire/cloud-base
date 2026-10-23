<template>
  <button :role="role" :tabindex="disabled ? -1 : 0" :disabled="disabled" :aria-disabled="disabled" @click="activate"
          @keydown="keydown">
    <slot/>
  </button>
</template>
<script setup>
const props = defineProps({role: {type: String, default: 'button'}, disabled: Boolean});
const emit = defineEmits(['click']);

function activate(event) {
  if (!props.disabled) emit('click', event);
}

function keydown(event) {
  if (event.key !== 'Enter' && event.key !== ' ') return;
  event.preventDefault();
  activate(event);
}
</script>
