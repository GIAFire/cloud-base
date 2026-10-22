<template>
  <view class="reset-page">
    <view class="reset-page__header">
      <text class="reset-page__back" @click="back">‹</text>
      <text class="reset-page__title">重置密码</text>
      <text class="reset-page__subtitle">验证邮箱后设置新密码，已有登录会话将全部退出</text>
    </view>

    <view class="reset-page__form">
      <af-form-field v-model="form.email" label="邮箱" placeholder="请输入邮箱" :maxlength="100" />
      <button class="code-button" :disabled="countdown > 0 || sending" @click="sendCode">
        {{ countdown > 0 ? `${countdown} 秒后重试` : '获取验证码' }}
      </button>
      <af-form-field v-model="form.code" label="验证码" placeholder="请输入 6 位验证码"
        type="number" :maxlength="6" />
      <af-form-field v-model="form.password" label="新密码" placeholder="请输入 8-32 位密码"
        :password="true" :maxlength="32" />
      <af-primary-button :loading="submitting" @click="submit">重置密码</af-primary-button>
    </view>
  </view>
</template>

<script setup>
import { onUnmounted, reactive, ref } from 'vue';
import AuthApi from '@/aifriend/api/auth';
import { isEmail, isEmailCode, isPassword } from '@/aifriend/helper/test';
import router from '@/aifriend/router';

const form = reactive({ email: '', code: '', password: '' });
const sending = ref(false);
const submitting = ref(false);
const countdown = ref(0);
let countdownTimer;

onUnmounted(() => clearInterval(countdownTimer));

function back() {
  uni.navigateBack();
}

function startCountdown() {
  countdown.value = 60;
  clearInterval(countdownTimer);
  countdownTimer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) clearInterval(countdownTimer);
  }, 1000);
}

async function sendCode() {
  if (!isEmail(form.email)) {
    uni.showToast({ title: '邮箱格式不正确', icon: 'none' });
    return;
  }
  sending.value = true;
  try {
    const response = await AuthApi.sendEmailCode(form.email.trim().toLowerCase(), 2);
    if (response.code === 0) startCountdown();
  } catch {
    // 请求层已经统一展示错误提示。
  } finally {
    sending.value = false;
  }
}

async function submit() {
  if (!isEmail(form.email) || !isEmailCode(form.code) || !isPassword(form.password)) {
    uni.showToast({ title: '请检查邮箱、验证码和新密码', icon: 'none' });
    return;
  }
  submitting.value = true;
  try {
    const response = await AuthApi.resetPasswordByEmail({ ...form, email: form.email.trim().toLowerCase() });
    if (response.code === 0) router.login();
  } catch {
    // 请求层已经统一展示错误提示。
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped lang="scss">
.reset-page {
  min-height: 100vh;
  padding: calc(var(--status-bar-height) + 36rpx) 44rpx 60rpx;
  background: #f7f7fb;

  &__header { margin-bottom: 48rpx; }
  &__back {
    display: block; width: 72rpx; height: 72rpx; margin-bottom: 36rpx;
    border-radius: 50%; background: #fff; font-size: 56rpx; line-height: 62rpx; text-align: center;
  }
  &__title, &__subtitle { display: block; }
  &__title { margin-bottom: 14rpx; font-size: 50rpx; font-weight: 750; }
  &__subtitle { color: #7b7f8d; font-size: 26rpx; line-height: 1.6; }
  &__form { padding: 40rpx; border-radius: 28rpx; background: #fff; }
}
.code-button {
  height: 72rpx; margin: -12rpx 0 28rpx auto; padding: 0 24rpx;
  border-radius: 14rpx; color: #665cf6; background: #efedff; font-size: 24rpx; line-height: 72rpx;
  &[disabled] { color: #999daa; background: #f0f1f4; }
}
</style>
