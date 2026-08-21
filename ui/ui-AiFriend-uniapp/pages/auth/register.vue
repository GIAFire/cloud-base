<template>
  <view class="register-page">
    <view class="register-page__header">
      <text class="register-page__back" @click="back">‹</text>
      <text class="register-page__title">短信登录/注册</text>
      <text class="register-page__subtitle">未注册的手机号验证成功后会自动创建会员账号</text>
    </view>

    <view class="register-page__form">
      <af-form-field
        v-model="form.mobile"
        label="手机号"
        placeholder="请输入手机号"
        type="number"
        :maxlength="11"
      />
      <button class="code-button" :disabled="countdown > 0 || sending" @click="sendCode">
        {{ countdown > 0 ? `${countdown} 秒后重试` : '获取验证码' }}
      </button>
      <af-form-field
        v-model="form.code"
        label="验证码"
        placeholder="请输入验证码"
        type="number"
        :maxlength="6"
      />

      <view class="agreement" @click="agreed = !agreed">
        <view class="agreement__checkbox" :class="{ 'agreement__checkbox--checked': agreed }">
          {{ agreed ? '✓' : '' }}
        </view>
        <text>我已阅读并同意用户协议与隐私政策</text>
      </view>

      <af-primary-button :loading="submitting" @click="submit">登录/注册</af-primary-button>
    </view>
  </view>
</template>

<script setup>
import { onUnmounted, reactive, ref } from 'vue';
import AuthApi from '@/aifriend/api/auth';
import { isMobile, isSmsCode } from '@/aifriend/helper/test';
import router from '@/aifriend/router';
import store from '@/aifriend/store';

const form = reactive({
  mobile: '',
  code: '',
});
const agreed = ref(false);
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
    if (countdown.value <= 0) {
      clearInterval(countdownTimer);
    }
  }, 1000);
}

async function sendCode() {
  if (!isMobile(form.mobile)) {
    uni.showToast({ title: '手机号码格式不正确', icon: 'none' });
    return;
  }
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议与隐私政策', icon: 'none' });
    return;
  }

  sending.value = true;
  try {
    const response = await AuthApi.sendSmsCode(form.mobile, 1);
    if (response.code === 0) {
      startCountdown();
    }
  } catch {
    // 请求层已经统一展示错误提示。
  } finally {
    sending.value = false;
  }
}

async function submit() {
  if (!isMobile(form.mobile)) {
    uni.showToast({ title: '手机号码格式不正确', icon: 'none' });
    return;
  }
  if (!isSmsCode(form.code)) {
    uni.showToast({ title: '请输入 4-6 位数字验证码', icon: 'none' });
    return;
  }
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议与隐私政策', icon: 'none' });
    return;
  }

  submitting.value = true;
  try {
    const response = await AuthApi.smsLogin(form);
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
    line-height: 1.6;
  }

  &__form {
    padding: 40rpx;
    border-radius: 28rpx;
    background: #ffffff;
  }
}

.code-button {
  height: 72rpx;
  margin: -12rpx 0 28rpx auto;
  padding: 0 24rpx;
  border-radius: 14rpx;
  color: #665cf6;
  background: #efedff;
  font-size: 24rpx;
  line-height: 72rpx;

  &[disabled] {
    color: #999daa;
    background: #f0f1f4;
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
