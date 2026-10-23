<template>
  <view class="app-shell">
    <view class="sidebar">
      <view class="brand"><span class="logo">♡</span>心屿</view>
      <p class="small">XINYU · AI COMPANION</p>
      <view class="desktop-nav">
        <af-button v-for="item in navigation" :key="item.key" :class="{ active: active === item.key }"
                   :aria-current="active === item.key ? 'page' : undefined" @click="navigate(item.key)"><i>{{
            item.icon
          }}</i>{{ item.label }}
        </af-button>
      </view>
      <view class="aside-bottom">✧　让陪伴，多一点可能<p>专属记忆 · 沉浸语音<br/>解锁属于你们的故事</p>
        <af-button @click="plusOpen = true">探索心屿 Plus ↗</af-button>
      </view>
    </view>
    <view class="workspace" :class="{ 'workspace--feed': feed }">
      <view v-if="!feed" class="workspace-header">
        <span class="eyebrow">YOUR LITTLE UNIVERSE</span>
        <view class="mobile-brand">♡ 心屿</view>
        <view class="account">
          <af-button v-if="!store.loggedIn" class="auth-link" @click="store.requireIdentity()">登录 / 注册</af-button>
          <af-button aria-label="每日心语" @click="notify('今天也要好好照顾自己 ♡')">✧</af-button>
          <af-button class="avatar" aria-label="我的小宇宙" @click="navigate('profile')">屿</af-button>
        </view>
      </view>
      <slot/>
    </view>
    <view class="mobile-nav">
      <af-button v-for="item in navigation" :key="item.key" :class="{ active: active === item.key }"
                 :aria-current="active === item.key ? 'page' : undefined" @click="navigate(item.key)"><i>{{
          item.icon
        }}</i>{{ item.label }}
      </af-button>
    </view>
    <af-modal :open="plusOpen" title="心屿 Plus ✧" @close="plusOpen = false">
      <p class="sub">让每一份在意，都被记住。</p>
      <p class="sub">专属长期记忆 · 自定义陪伴风格 · 更多语音时光</p>
      <af-button class="primary" @click="reserve">{{
          store.data.plusReserved ? '已加入体验候补名单' : '预约体验'
        }}
      </af-button>
      <p class="hint">原型展示，暂无付费或订阅。</p>
    </af-modal>
  </view>
</template>
<script setup>
import {ref} from 'vue';
import router from '@/aifriend/router';
import {useExperienceStore, notify} from '@/aifriend/store/experience';

defineProps({active: {type: String, default: 'home'}, feed: Boolean});
const store = useExperienceStore();
const plusOpen = ref(false);
const navigation = [
  {key: 'home', icon: '⌂', label: '首页'}, {key: 'conversations', icon: '☏', label: '对话'},
  {key: 'video', icon: '▷', label: '视频'}, {key: 'profile', icon: '♙', label: '我的'},
];

function navigate(key) {
  router.go(router.routes[key], {reset: true});
}

function reserve() {
  if (!store.requireIdentity({action: {type: 'plus'}})) return;
  store.data.plusReserved = true;
  store.persist();
  notify('已加入体验候补名单');
}
</script>
