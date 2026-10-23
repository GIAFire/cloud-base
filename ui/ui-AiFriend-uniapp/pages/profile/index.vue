<template>
  <af-shell active="profile">
    <h1 class="page-title">我的小宇宙</h1>
    <p class="sub">珍藏属于你的每一个心动瞬间</p>
    <view class="row profile-identity">
      <view class="avatar profile-avatar">{{ store.loggedIn ? '屿' : '♡' }}</view>
      <view><h3>{{ store.loggedIn ? '心屿旅行家' : '登录，开启专属陪伴' }}</h3>
        <p>{{ store.loggedIn ? maskedEmail : '收藏心动角色，继续你们的故事。' }}</p></view>
      <span v-if="store.loggedIn" class="pink profile-badge">✧ 免费体验</span>
      <af-button v-else class="primary" @click="store.requireIdentity()">登录 / 注册</af-button>
    </view>
    <view class="stats">
      <view><b>{{ store.data.favorites.length }}</b>心动收藏</view>
      <view><b>{{ Object.keys(store.data.conversations).length }}</b>相遇故事</view>
      <view><b>{{ days }}</b>陪伴天数</view>
    </view>
    <view class="list">
      <af-button v-for="(item, index) in settings" :key="item" class="row setting-row" @click="openSetting(index)">
        {{ item }}<span>›</span></af-button>
    </view>
    <af-button v-if="store.loggedIn" class="auth-link logout-link" @click="store.logout()">退出演示登录</af-button>
    <p class="hint">心屿 v0.1 · 有回应的陪伴，有边界的温柔</p>
    <af-modal :open="panel === 'memory'" title="专属陪伴记忆" @close="panel = ''">
      <p class="sub">她记住的每一件小事，都可以由你决定。正式版可查看、编辑和删除记忆。</p>
      <p class="sub">当前为演示体验，暂无长期记忆数据。</p>
    </af-modal>
    <af-modal :open="panel === 'preference'" title="偏好与设置" @close="panel = ''">
      <p class="sub">喜欢怎样的陪伴节奏？</p>
      <view class="preference-options">
        <af-button v-for="value in preferences" :key="value" :class="{ selected: store.data.preference === value }"
                   :aria-pressed="store.data.preference === value" @click="store.setPreference(value)">{{ value }}
          <span>{{ store.data.preference === value ? '✓' : '' }}</span></af-button>
      </view>
      <p class="hint">偏好将影响下一条模拟回复的风格。</p>
    </af-modal>
    <af-modal :open="panel === 'privacy'" title="隐私与数据" @close="panel = ''">
      <p class="sub">
        演示身份、对话、草稿、角色收藏、视频互动与陪伴偏好仅保存在当前浏览器。本应用不会保存密码，不会访问摄像头或麦克风。</p>
      <p class="sub">退出登录会保留体验数据。“清除体验数据”会清除以上全部内容并退出演示身份。</p>
      <af-button class="primary" @click="panel = 'confirm'">清除体验数据</af-button>
    </af-modal>
    <af-modal :open="panel === 'confirm'" title="确认清除全部体验数据？" @close="panel = 'privacy'">
      <p class="sub">对话、草稿、收藏、视频点赞/关注/收藏/留言、偏好及演示身份都将移除，清除后无法恢复。</p>
      <view class="dialog-actions">
        <af-button class="secondary" @click="panel = 'privacy'">保留数据</af-button>
        <af-button class="primary" @click="clear">确认清除</af-button>
      </view>
    </af-modal>
  </af-shell>
</template>
<script setup>
import {computed} from 'vue';
import {preferences} from '@/aifriend/domain/catalog';
import {useExperienceStore} from '@/aifriend/store/experience';
import router from '@/aifriend/router';

const store = useExperienceStore();
const panel = computed({
  get: () => store.profilePanel, set: (value) => {
    store.profilePanel = value;
  }
});
const settings = ['♡ 我的收藏', '✧ 专属陪伴记忆', '☷ 偏好与设置', '◇ 隐私与数据'];
const maskedEmail = computed(() => {
  const [name, domain] = (store.data.identity?.email || '').split('@');
  return name.slice(0, 2) + '***@' + domain;
});
const days = computed(() => Math.max(1, Math.floor((Date.now() - store.data.joinedAt) / 86400000) + 1));

function openSetting(index) {
  if (index === 0) {
    store.discovery = {query: '', tag: '全部', category: '我的收藏'};
    router.go(router.routes.home, {reset: true});
    return;
  }
  panel.value = ['', 'memory', 'preference', 'privacy'][index];
}

function clear() {
  store.clear();
  panel.value = '';
}
</script>
