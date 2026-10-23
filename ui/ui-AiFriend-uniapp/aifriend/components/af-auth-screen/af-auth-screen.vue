<template>
  <view class="auth-layout">
    <view class="auth-art">
      <view class="brand"><span class="logo">♡</span>织梦</view>
      <view class="auth-orb">♡</view>
      <view class="art-text">
        <view class="eyebrow">A LITTLE CLOSER TO YOU</view>
        <h1>在织梦，<br/>遇见有回应的心动。</h1>
        <p>把日常说给懂你的她听。<br/>从一句你好，开始属于你们的故事。</p></view>
      <small>XINYU · YOUR LITTLE UNIVERSE</small>
    </view>
    <view class="auth-side">
      <af-button class="auth-back" @click="store.cancelAuth()">‹ 返回探索</af-button>
      <view class="auth-panel">
        <view class="auth-mobile-brand">♡ 织梦</view>

          <view class="auth-kicker">{{ reset ? 'FIND YOUR WAY BACK' : 'YOUR STORY STARTS HERE' }}</view>
          <h1>{{ reset ? resetTitle : register ? '开启心动故事' : '欢迎回到织梦' }}</h1>
          <p class="sub">{{
              reset ? '验证邮箱，重新设置你的登录密码。' : register ? '创建一个账号，让每一次相遇被珍藏。' : '登录后，继续属于你们的故事。'
            }}</p>
          <label class="field" for="auth-email">邮箱地址</label>
          <view class="auth-input"><span>✉</span><input id="auth-email" v-model="form.email" aria-label="邮箱地址"
                                                        placeholder="name@example.com" :maxlength="100"
                                                        @confirm="submit"/></view>
          <template v-if="register || reset">
            <label class="field" for="auth-code">邮箱验证码</label>
            <view class="auth-input"><input id="auth-code" v-model="form.code" aria-label="邮箱验证码"
                                            placeholder="输入 6 位验证码" type="number" :maxlength="6"/>
              <af-button :disabled="cooldown > 0 || codeSending" @click="sendCode">{{
                  cooldown > 0 ? cooldown + '秒后重试' : '获取验证码'
                }}
              </af-button>
            </view>
          </template>
          <label class="field" for="auth-pass">{{ reset ? '新密码' : '密码' }}</label>
          <view class="auth-input"><span>♧</span><input id="auth-pass" v-model="form.password" aria-label="密码"
                                                        :password="!showPassword"
                                                        :placeholder="register || reset ? '输入 8–32 位密码' : '请输入密码'"
                                                        :maxlength="32" @confirm="submit"/>
            <af-button :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword">
              {{ showPassword ? '隐藏' : '显示' }}
            </af-button>
          </view>
          <template v-if="register || reset">
            <p class="auth-note">密码长度为 8–32 位</p><label class="field" for="auth-confirm">确认{{
              reset ? '新' : ''
            }}密码</label>
            <view class="auth-input"><input id="auth-confirm" v-model="form.confirm" aria-label="确认密码"
                                            :password="!showConfirm" placeholder="再次输入密码" :maxlength="32"
                                            @confirm="submit"/>
              <af-button aria-label="切换确认密码显示" @click="showConfirm = !showConfirm">
                {{ showConfirm ? '隐藏' : '显示' }}
              </af-button>
            </view>
          </template>
          <view v-else class="auth-options"><span>♡ 让陪伴从这里继续</span>
            <af-button class="auth-link" @click="switchMode('reset')">忘记密码？</af-button>
          </view>
          <view v-if="register" class="auth-check">
            <af-button class="consent-checkbox" role="checkbox" :aria-checked="form.agreed"
                       aria-label="同意协议并确认年满18周岁" @click="form.agreed = !form.agreed">
              {{ form.agreed ? '✓' : '' }}
            </af-button>
            <span>我已阅读并同意 <af-button class="auth-link" @click="legal = '用户协议'">用户协议</af-button> 与 <af-button
                class="auth-link" @click="legal = '隐私政策'">隐私政策</af-button>，并已年满 18 周岁</span></view>
          <view class="auth-error" role="alert" aria-live="polite">{{ error }}</view>
          <af-button class="primary auth-submit" :disabled="submitting" @click="submit">
            {{ reset ? '确认修改密码' : register ? '创建账号，开始相遇' : '登录织梦' }} →
          </af-button>
          <view class="auth-mini-links">
            <template v-if="reset">
              <af-button class="auth-link" @click="switchMode('login')">‹ 返回登录</af-button>
            </template>
            <template v-else><span>{{ register ? '已有账号？' : '还没有账号？' }}</span>
              <af-button class="auth-link" @click="switchMode(register ? 'login' : 'register')">
                {{ register ? '去登录' : '立即注册' }}
              </af-button>
            </template>
          </view>
      </view>
    </view>
    <af-modal :open="Boolean(legal)" :title="legal" @close="legal = ''"><p class="sub">相关条款将在服务开放前提供。</p><af-button class="primary" @click="legal = ''">我知道了</af-button></af-modal>
  </view>
</template>
<script setup>
import { computed, onUnmounted, reactive, ref, watch } from 'vue';
import { experienceService, validateAuth } from '@/aifriend/services/experience';
import { useExperienceStore, notify } from '@/aifriend/store/experience';
import router from '@/aifriend/router';

const props = defineProps({ mode: { type: String, default: 'login' }, initialEmail: { type: String, default: '' }, changePassword: Boolean });
const store = useExperienceStore();
const form = reactive({ email: '', password: '', confirm: '', code: '', agreed: false });
const register = computed(() => props.mode === 'register'), reset = computed(() => props.mode === 'reset');
const resetTitle = computed(() => props.changePassword ? '修改密码' : '找回密码');
const showPassword = ref(false), showConfirm = ref(false), error = ref(''), legal = ref('');
const cooldown = ref(0), codeSending = ref(false), submitting = ref(false);
let timer;
let disposed = false;
watch(() => props.initialEmail, (email) => { form.email = email; }, { immediate: true });
watch(() => form.email, () => { cooldown.value = 0; clearInterval(timer); });
async function sendCode() {
  const email = form.email.trim().toLowerCase();
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) || email.length > 100) { error.value = '请输入有效的邮箱地址（最多 100 位）'; return; }
  if (cooldown.value || codeSending.value) return;
  codeSending.value = true; error.value = '';
  try {
    await experienceService.sendEmailCode({ email, scene: reset.value ? 'reset' : 'register' });
    if (disposed || form.email.trim().toLowerCase() !== email) return;
    notify('请查收邮箱验证码');
    const expires = Date.now() + 60000;
    cooldown.value = 60;
    timer = setInterval(() => {
      cooldown.value = Math.max(0, Math.ceil((expires - Date.now()) / 1000));
      if (!cooldown.value) clearInterval(timer);
    }, 1000);
  } catch (cause) { error.value = cause.message; }
  finally { codeSending.value = false; }
}
async function submit() {
  if (submitting.value) return;
  error.value = validateAuth(form, props.mode);
  if (error.value) return;
  submitting.value = true;
  const credentials = { email: form.email.trim().toLowerCase(), password: form.password, code: form.code };
  try {
    if (!reset.value) {
      await store.authenticate(credentials, props.mode);
      notify(register.value ? '注册成功' : '登录成功');
    } else {
      await experienceService.resetPassword(credentials);
      store.identity = null;
      form.password = ''; form.confirm = ''; form.code = '';
      notify('密码已修改，请重新登录');
      switchMode('login');
    }
  } catch (cause) { error.value = cause.message; }
  finally { submitting.value = false; }
}
function switchMode(mode) {
  const route = mode === 'reset' ? router.routes.forgotPassword : router.routes[mode];
  router.go(route + '?returnTo=' + encodeURIComponent(store.returnRoute || router.routes.home) + '&email=' + encodeURIComponent(form.email), { replace: true });
}
onUnmounted(() => { disposed = true; clearInterval(timer); form.password = ''; form.confirm = ''; });
</script>
