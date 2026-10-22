<template>
  <view class="auth-page">
    <view class="auth-page__hero">
      <text class="auth-page__brand">AI Friend</text>
      <text class="auth-page__title">账号登录</text>
      <text class="auth-page__subtitle">使用邮箱和密码登录</text>
    </view>

    <view class="auth-page__form">
      <af-form-field
        v-model="form.email"
        label="邮箱"
        placeholder="请输入邮箱"
        type="text"
        :maxlength="100"
      />
      <af-form-field
        v-model="form.password"
        label="密码"
        placeholder="请输入密码"
        :password="true"
        :maxlength="32"
      />

      <view class="agreement" @click="agreed = !agreed">
        <view class="agreement__checkbox" :class="{ 'agreement__checkbox--checked': agreed }">
          {{ agreed ? '✓' : '' }}
        </view>
        <text>我已阅读并同意用户协议与隐私政策</text>
      </view>

      <af-primary-button :loading="submitting" @click="submit">登录</af-primary-button>

      <view class="auth-page__forgot" @click="router.forgotPassword">忘记密码？</view>

      <view class="auth-page__link" @click="router.register">
        <text>还没有账号？</text>
        <text class="auth-page__link-text">邮箱注册</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app';
import { reactive, ref } from 'vue';
import AuthApi from '@/aifriend/api/auth';
import { isEmail } from '@/aifriend/helper/test';
import router from '@/aifriend/router';
import store from '@/aifriend/store';

const form = reactive({
  email: '',
  password: '',
});
const agreed = ref(false);
const submitting = ref(false);

onShow(() => {
  if (uni.getStorageSync('token')) {
    router.home();
  }
});

async function submit() {
  if (!isEmail(form.email)) {
    uni.showToast({ title: '邮箱格式不正确', icon: 'none' });
    return;
  }
  if (!form.password || form.password.length > 32) {
    uni.showToast({ title: '请输入密码', icon: 'none' });
    return;
  }
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议与隐私政策', icon: 'none' });
    return;
  }

  submitting.value = true;
  try {
    const response = await AuthApi.emailLogin({ ...form, email: form.email.trim().toLowerCase() });
    if (response.code === 0) {
      const userStore = store('user');
      userStore.syncLoginState();
      userStore.loginAfter().catch(() => {});
      router.home();
    }
  } catch {
    // 请求层已经统一展示错误提示。
  } finally {
    submitting.value = false;
  }
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

  &__forgot {
    margin-top: 24rpx;
    color: #665cf6;
    font-size: 25rpx;
    text-align: right;
  }

  &__link-text {
    margin-left: 8rpx;
    color: #665cf6;
    font-weight: 600;
  }
}

.agreement {
  display: flex;
  align-items: center;
  margin: 4rpx 0 30rpx;
  color: #777b88;
  font-size: 23rpx;

  &__checkbox {
    width: 32rpx;
    height: 32rpx;
    margin-right: 12rpx;
    border: 2rpx solid #c9cbd3;
    border-radius: 8rpx;
    color: #ffffff;
    font-size: 22rpx;
    line-height: 28rpx;
    text-align: center;

    &--checked {
      border-color: #665cf6;
      background: #665cf6;
    }
  }
}
</style>
