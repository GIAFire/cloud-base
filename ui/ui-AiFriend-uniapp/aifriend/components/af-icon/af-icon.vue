<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"
       stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
    <path :d="paths[name] || paths.chat"/>
  </svg>
</template>
<script setup>
defineProps({name: {type: String, default: 'heart'}, size: {type: Number, default: 24}});
const paths = {
  heart: 'M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 0 0 0-7.8Z',
  comment: 'M21 11.5a8.5 8.5 0 0 1-8.5 8.5H4l-2 2v-9.5A9 9 0 0 1 11.5 3 9 9 0 0 1 21 11.5Z M7 11h8 M7 15h5',
  save: 'M6 3h12a1 1 0 0 1 1 1v17l-7-5-7 5V4a1 1 0 0 1 1-1Z',
  share: 'm14 3 7 7-7 7v-4C7 13 4 15 3 21c-1-11 4-15 11-15Z',
  up: 'm6 15 6-6 6 6', down: 'm6 9 6 6 6-6',
  chat: 'M21 11a8 8 0 0 1-8 8H8l-5 3V11a9 9 0 0 1 18 0Z M8 10h8 M8 14h5',
};
</script>
