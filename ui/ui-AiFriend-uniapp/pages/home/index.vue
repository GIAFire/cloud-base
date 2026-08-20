<template>
  <af-page title="首页">
    <af-api-result
      :status="connection.status"
      :result="connection.result"
      :error-message="connection.errorMessage"
      @retry="testConnection"
    />
  </af-page>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app';
import { reactive } from 'vue';
import TestApi from '@/aifriend/api/test';
import router from '@/aifriend/router';

const connection = reactive({
  status: 'idle',
  result: null,
  errorMessage: '',
});

onShow(() => {
  if (!router.ensureLogin()) return;
  testConnection();
});

async function testConnection() {
  connection.status = 'loading';
  connection.errorMessage = '';

  try {
    connection.result = await TestApi.get();
    connection.status = 'success';
  } catch (error) {
    connection.result = null;
    connection.status = 'error';
    connection.errorMessage = error.message || '接口调用失败';
  }
}
</script>
