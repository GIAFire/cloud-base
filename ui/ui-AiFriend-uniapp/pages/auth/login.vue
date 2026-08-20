<template>
  <view class="auth-page">
    <view class="auth-page__hero">
      <text class="auth-page__brand">AI Friend</text>
      <text class="auth-page__title">欢迎回来</text>
      <text class="auth-page__subtitle">登录后开始规划你的 AI 朋友</text>
    </view>

    <view class="auth-page__form">
      <af-form-field v-model="form.username" label="账号" placeholder="请输入账号" />
      <af-form-field
        v-model="form.password"
        label="密码"
        placeholder="请输入密码"
        :password="true"
      />
      <af-primary-button @click="submit">登录</af-primary-button>

      <view class="auth-page__link" @click="router.register">
        <text>还没有账号？</text>
        <text class="auth-page__link-text">去注册</text>
      </view>

      <text class="auth-page__tip">当前为前端占位登录，填写任意非空账号和密码即可进入。</text>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app';
import { reactive } from 'vue';
import router from '@/aifriend/router';
import store from '@/aifriend/store';

const form = reactive({
  username: '',
  password: '',
});

onShow(() => {
  if (uni.getStorageSync('token')) {
    router.home();
  }
});

function submit() {
  if (!form.username.trim() || !form.password) {
    uni.showToast({
      title: '请输入账号和密码',
      icon: 'none',
    });
    return;
  }

  store('user').loginPlaceholder({
    username: form.username.trim(),
  });
  router.home();
}
</script>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  padding: calc(var(--status-bar-height) + 88rpx) 44rpx 60rpx;
  background: linear-gradient(160deg, #f0efff 0%, #f8f8fb 45%, #ffffff 100%);

  &__hero {
    margin-bottom: 64rpx;
  }

  &__brand,
  &__title,
  &__subtitle {
    display: block;
  }

  &__brand {
    margin-bottom: 36rpx;
    color: #665cf6;
    font-size: 30rpx;
    font-weight: 700;
    letter-spacing: 2rpx;
  }

  &__title {
    margin-bottom: 14rpx;
    font-size: 56rpx;
    font-weight: 750;
  }

  &__subtitle {
    color: #777b88;
    font-size: 28rpx;
  }

  &__form {
    padding: 40rpx;
    border: 2rpx solid rgba(255, 255, 255, 0.85);
    border-radius: 28rpx;
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 20rpx 64rpx rgba(62, 57, 133, 0.09);
  }

  &__link {
    display: flex;
    justify-content: center;
    margin-top: 32rpx;
    color: #737784;
    font-size: 26rpx;
  }

  &__link-text {
    margin-left: 8rpx;
    color: #665cf6;
    font-weight: 600;
  }

  &__tip {
    display: block;
    margin-top: 28rpx;
    color: #999daa;
    font-size: 22rpx;
    line-height: 1.6;
    text-align: center;
  }
}
</style>
