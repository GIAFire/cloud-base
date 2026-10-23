<template>
  <af-shell active="profile">
    <h1 class="page-title">织梦</h1>
    <p class="sub">编织你的每一个心动瞬间</p>
    <view class="row profile-identity">
      <view class="avatar profile-avatar">{{ store.loggedIn ? '梦' : '♡' }}</view>
      <view><h3>{{ store.loggedIn ? (store.identity?.nickname || '我的账号') : '登录，开启专属陪伴' }}</h3>
        <p>{{ store.loggedIn ? maskedEmail : '收藏心动角色，继续你们的故事。' }}</p></view>
      <span v-if="store.loggedIn" class="pink profile-badge">已登录</span>
      <af-button v-else class="primary" @click="store.requireIdentity()">登录 / 注册</af-button>
    </view>
    <view class="list">
      <af-button v-for="(item, index) in settings" :key="item" class="row setting-row" @click="openSetting(index)">
        {{ item }}<span>›</span></af-button>
    </view>
    <af-button v-if="store.loggedIn" class="auth-link logout-link" @click="store.logout()">退出登录</af-button>
    <p class="hint">织梦 v0.1 · 有回应的陪伴，有边界的温柔</p>
    <af-modal :open="panel === 'memory'" title="专属陪伴记忆" @close="panel = ''">
      <p class="sub">在这里管理你们的陪伴记忆。</p>
      <p class="sub">暂无陪伴记忆。</p>
    </af-modal>
    <af-modal :open="panel === 'preference'" title="偏好与设置" @close="panel = ''">
      <p class="sub">喜欢怎样的陪伴节奏？</p>
      <view class="preference-options">
        <af-button v-for="value in preferences" :key="value" :class="{ selected: store.data.preference === value }"
                   :aria-pressed="store.data.preference === value" @click="store.setPreference(value)">{{ value }}
          <span>{{ store.data.preference === value ? '✓' : '' }}</span></af-button>
      </view>
    </af-modal>
    <af-modal :open="panel === 'privacy'" title="隐私与数据" @close="panel = ''">
      <p class="sub">
        消息草稿、留言草稿与陪伴偏好保存在当前浏览器。你可以清除这些本地数据。</p>
      <p class="sub">清除本地数据不会删除账号、服务端对话或收藏。</p>
      <af-button class="primary" @click="panel = 'confirm'">清除本地数据</af-button>
    </af-modal>
    <af-modal :open="panel === 'confirm'" title="确认清除本地数据？" @close="panel = 'privacy'">
      <p class="sub">本机消息草稿、留言草稿和陪伴偏好将被移除，清除后无法恢复。</p>
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
const settings = computed(() => ['♡ 我的收藏', '✧ 专属陪伴记忆', '☷ 偏好与设置', '◇ 隐私与数据', ...(store.loggedIn ? ['♧ 修改密码'] : [])]);
const maskedEmail = computed(() => {
  const [name, domain] = (store.identity?.email || '').split('@');
  return domain ? name.slice(0, 2) + '***@' + domain : '账号已登录';
});

function openSetting(index) {
  if (index === 4) {
    if (store.requireIdentity()) router.go(router.routes.forgotPassword + '?mode=change&email=' + encodeURIComponent(store.identity?.email || '') + '&returnTo=' + encodeURIComponent(router.routes.profile));
    return;
  }
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
