<template>
  <af-shell active="conversations">
    <h1 class="page-title">对话</h1>
    <p class="sub">那些只想说给她听的小事</p>
    <view class="list">
      <af-button v-for="person in people" :key="person.id" class="row conversation-row"
                 @click="store.openChat(person.id)">
        <img :src="portrait(person, 150)" :alt="person.name"/>
        <view class="conversation-copy"><h3>{{ person.name }} <small class="online-dot">● 在线</small></h3>
          <p>{{ preview(person) }}</p></view>
        <time>{{ store.data.conversations[person.id] ? '继续故事 ›' : '开始相遇 ›' }}</time>
      </af-button>
    </view>
    <view v-if="!Object.keys(store.data.conversations).length" class="gentle-note"><span>✧</span>
      <p>从一句你好开始，<br/>把平凡的日常说给她听。</p>
      <af-button class="auth-link" @click="router.home()">发现更多心动角色 ↗</af-button>
    </view>
    <p class="hint">你的专属对话空间 · AI 回复为本地模拟</p>
  </af-shell>
</template>
<script setup>
import {computed} from 'vue';
import {characters, portrait} from '@/aifriend/domain/catalog';
import {useExperienceStore} from '@/aifriend/store/experience';
import router from '@/aifriend/router';

const store = useExperienceStore();
const people = computed(() => [
  ...characters.filter((person) => store.data.conversations[person.id]),
  ...characters.slice(0, 3).filter((person) => !store.data.conversations[person.id]),
]);

function preview(person) {
  const messages = store.data.conversations[person.id];
  if (!messages?.length || !store.loggedIn) return person.quote;
  return messages[messages.length - 1].text;
}
</script>
