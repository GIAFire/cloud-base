<template>
  <af-auth-screen mode="reset" :initial-email="email" :change-password="changePassword"/>
</template>
<script setup>
import {ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {safeReturnRoute} from '@/aifriend/services/experience';
import {useExperienceStore} from '@/aifriend/store/experience';

const store = useExperienceStore(), email = ref(''), changePassword = ref(false);
onLoad((options) => {
  changePassword.value = options.mode === 'change';
  let route = options.returnTo || store.returnRoute;
  try {
    route = decodeURIComponent(route || '');
    email.value = decodeURIComponent(options.email || '');
  } catch {
    email.value = '';
  }
  store.returnRoute = safeReturnRoute(route);
});
</script>
