<template>
  <view class="register-page">
    <view class="register-page__header">
      <text class="register-page__back" @click="back">‹</text>
      <text class="register-page__title">创建账号</text>
      <text class="register-page__subtitle">先准备基础入口，稍后再接入正式注册接口</text>
    </view>

    <view class="register-page__form">
      <af-form-field v-model="form.username" label="账号" placeholder="请输入账号" />
      <af-form-field
        v-model="form.password"
        label="密码"
        placeholder="请输入密码"
        :password="true"
      />
      <af-form-field
        v-model="form.confirmPassword"
        label="确认密码"
        placeholder="请再次输入密码"
        :password="true"
      />
      <af-primary-button @click="submit">注册</af-primary-button>
    </view>
  </view>
</template>

<script setup>
import { reactive } from 'vue';

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
});

function back() {
  uni.navigateBack();
}

function submit() {
  if (!form.username.trim() || !form.password || !form.confirmPassword) {
    uni.showToast({ title: '请完整填写注册信息', icon: 'none' });
    return;
  }

  if (form.password !== form.confirmPassword) {
    uni.showToast({ title: '两次输入的密码不一致', icon: 'none' });
    return;
  }

  uni.showToast({ title: '注册页面准备完成', icon: 'success' });
  setTimeout(() => uni.navigateBack(), 500);
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  padding: calc(var(--status-bar-height) + 36rpx) 44rpx 60rpx;
  background: #f7f7fb;

  &__header {
    margin-bottom: 48rpx;
  }

  &__back {
    display: block;
    width: 72rpx;
    height: 72rpx;
    margin-bottom: 36rpx;
    border-radius: 50%;
    background: #ffffff;
    color: #333642;
    font-size: 56rpx;
    line-height: 62rpx;
    text-align: center;
  }

  &__title,
  &__subtitle {
    display: block;
  }

  &__title {
    margin-bottom: 14rpx;
    font-size: 50rpx;
    font-weight: 750;
  }

  &__subtitle {
    color: #7b7f8d;
    font-size: 26rpx;
  }

  &__form {
    padding: 40rpx;
    border-radius: 28rpx;
    background: #ffffff;
  }
}
</style>
