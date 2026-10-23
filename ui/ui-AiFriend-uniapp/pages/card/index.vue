<template>
  <view/>
</template>
<script setup>
// Compatibility entry for old card links; discovery now lives on the home page.
import {onLoad} from '@dcloudio/uni-app';
import router from '@/aifriend/router';

onLoad(() => router.home());
</script>
