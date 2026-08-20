<template>
  <af-page title="聊天" center />
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app';
import router from '@/aifriend/router';

onShow(() => router.ensureLogin());
</script>
