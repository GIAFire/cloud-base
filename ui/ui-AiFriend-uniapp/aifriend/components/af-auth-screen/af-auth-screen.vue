<template>
  <view class="auth-layout">
    <view class="auth-art">
      <view class="brand"><span class="logo">♡</span>心屿</view>
      <view class="auth-orb">♡</view>
      <view class="art-text">
        <view class="eyebrow">A LITTLE CLOSER TO YOU</view>
        <h1>在心屿，<br/>遇见有回应的心动。</h1>
        <p>把日常说给懂你的她听。<br/>从一句你好，开始属于你们的故事。</p></view>
      <small>XINYU · YOUR LITTLE UNIVERSE</small>
    </view>
    <view class="auth-side">
      <af-button class="auth-back" @click="store.cancelAuth()">‹ 返回探索</af-button>
      <view class="auth-panel">
        <view class="auth-mobile-brand">♡ 心屿</view>
        <template v-if="success">
          <view class="auth-success">✓</view>
          <view class="auth-kicker">A NEW CHAPTER</view>
          <h1>{{ reset ? '密码重置流程完成' : register ? '故事，即将开始' : '欢迎回到心屿' }}</h1>
          <p class="sub">{{
              reset ? '已完成重置密码的界面演示，尚未修改真实账号。' : '演示流程已完成，继续属于你的心动故事。'
            }}</p>
          <af-button class="primary auth-submit" @click="finish">{{ reset ? '返回邮箱登录' : '进入心屿' }} →</af-button>
        </template>
        <template v-else>
          <view class="auth-kicker">{{ reset ? 'FIND YOUR WAY BACK' : 'YOUR STORY STARTS HERE' }}</view>
          <h1>{{ reset ? '找回密码' : register ? '开启心动故事' : '欢迎回到心屿' }}</h1>
          <p class="sub">{{
              reset ? '验证邮箱，重新设置你的登录密码。' : register ? '创建一个账号，让每一次相遇被珍藏。' : '登录后，继续属于你们的故事。'
            }}</p>
          <view v-if="!reset" class="auth-tabs">
            <af-button :class="{ active: !register }" @click="switchMode('login')">邮箱登录</af-button>
            <af-button :class="{ active: register }" @click="switchMode('register')">邮箱注册</af-button>
          </view>
          <label class="field" for="auth-email">邮箱地址</label>
          <view class="auth-input"><span>✉</span><input id="auth-email" v-model="form.email" aria-label="邮箱地址"
                                                        placeholder="name@example.com" :maxlength="254"
                                                        @confirm="submit"/></view>
          <template v-if="register || reset">
            <label class="field" for="auth-code">邮箱验证码</label>
            <view class="auth-input"><input id="auth-code" v-model="form.code" aria-label="邮箱验证码"
                                            placeholder="输入 6 位验证码" type="number" :maxlength="6"/>
              <af-button :disabled="cooldown > 0" @click="sendCode">{{
                  cooldown > 0 ? cooldown + '秒后重试' : '获取验证码'
                }}
              </af-button>
            </view>
            <p class="auth-note" aria-live="polite">
              {{ codeEmail ? '未发送邮件，请使用演示验证码 123456。' : '演示验证码：123456，点击获取后即可使用。' }}</p>
          </template>
          <label class="field" for="auth-pass">{{ reset ? '新密码' : '密码' }}</label>
          <view class="auth-input"><span>♧</span><input id="auth-pass" v-model="form.password" aria-label="密码"
                                                        :password="!showPassword"
                                                        :placeholder="register || reset ? '8–32 位，包含字母与数字' : '输入测试密码'"
                                                        :maxlength="32" @confirm="submit"/>
            <af-button :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword">
              {{ showPassword ? '隐藏' : '显示' }}
            </af-button>
          </view>
          <template v-if="register || reset">
            <p class="auth-note">8–32 位，至少包含一个字母和一个数字</p><label class="field" for="auth-confirm">确认{{
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
          <af-button class="primary auth-submit" @click="submit">
            {{ reset ? '确认重置密码' : register ? '创建账号，开始相遇' : '登录心屿' }} →
          </af-button>
          <af-button v-if="!register && !reset" class="secondary demo-fill" @click="fillDemo">填入演示身份</af-button>
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
        </template>
        <p class="auth-demo">交互演示 · 请使用虚构邮箱和测试密码<br/>不会发送邮件，不会保存或验证真实账号密码。</p>
      </view>
    </view>
    <af-modal :open="Boolean(legal)" :title="legal + ' · 演示说明'" @close="legal = ''"><p class="sub">
      当前仅演示产品流程。密码不会上传或保存，演示数据仅存于当前浏览器。正式上线前将补充完整协议与账号管理流程。</p>
      <af-button class="primary" @click="legal = ''">我知道了</af-button>
    </af-modal>
  </view>
</template>
<script setup>
import {computed, onUnmounted, reactive, ref, watch} from 'vue';
import {validateAuth} from '@/aifriend/services/experience';
import {useExperienceStore} from '@/aifriend/store/experience';
import router from '@/aifriend/router';

const props = defineProps({mode: {type: String, default: 'login'}, initialEmail: {type: String, default: ''}});
const store = useExperienceStore();
const form = reactive({email: '', password: '', confirm: '', code: '', agreed: false});
const register = computed(() => props.mode === 'register'), reset = computed(() => props.mode === 'reset');
const showPassword = ref(false), showConfirm = ref(false), success = ref(false), error = ref(''), legal = ref('');
const codeEmail = ref(''), cooldown = ref(0);
let timer;
watch(() => props.initialEmail, (email) => {
  form.email = email;
}, {immediate: true});
watch(() => form.email, () => {
  codeEmail.value = '';
  cooldown.value = 0;
  clearInterval(timer);
});

function sendCode() {
  const email = form.email.trim().toLowerCase();
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    error.value = '请输入有效的邮箱地址';
    return;
  }
  if (cooldown.value) return;
  codeEmail.value = email;
  cooldown.value = 60;
  error.value = '';
  const expires = Date.now() + 60000;
  timer = setInterval(() => {
    cooldown.value = Math.max(0, Math.ceil((expires - Date.now()) / 1000));
    if (!cooldown.value) clearInterval(timer);
  }, 1000);
}

function submit() {
  error.value = validateAuth(form, props.mode, codeEmail.value);
  if (error.value) return;
  form.password = '';
  form.confirm = '';
  form.code = '';
  clearInterval(timer);
  success.value = true;
}

function fillDemo() {
  form.email = 'traveler@example.com';
  form.password = 'Xinyu1234';
  error.value = '';
}

function switchMode(mode) {
  const route = mode === 'reset' ? router.routes.forgotPassword : router.routes[mode];
  router.go(route + '?returnTo=' + encodeURIComponent(store.returnRoute || router.routes.home) + '&email=' + encodeURIComponent(form.email), {replace: true});
}

function finish() {
  if (reset.value) switchMode('login'); else store.authenticate(form.email);
}

onUnmounted(() => {
  clearInterval(timer);
  form.password = '';
  form.confirm = '';
});
</script>
