<template>
  <af-shell active="conversations">
    <h1 class="page-title">对话</h1>
    <p class="sub">那些只想说给她听的小事</p>
    <view class="list">
      <af-button v-for="person in people" :key="person.id" class="row conversation-row"
                 @click="store.openChat(person.id)">
        <img :src="portrait(person, 150)" :alt="person.name"/>
        <view class="conversation-copy"><h3>{{ person.name }} </h3>
          <p>{{ preview(person) }}</p></view>
        <time>{{ store.data.conversations[person.id] ? '继续故事 ›' : '开始相遇 ›' }}</time>
      </af-button>
    </view>
    <view v-if="!Object.keys(store.data.conversations).length" class="gentle-note"><span>✧</span>
      <p>暂无对话<br/>发现喜欢的角色，开启新的故事。</p>
      <af-button class="auth-link" @click="router.home()">发现更多心动角色 ↗</af-button>
    </view>
  </af-shell>
</template>
<script setup>
import {computed} from 'vue';
import {portrait} from '@/aifriend/domain/catalog';
import {useExperienceStore} from '@/aifriend/store/experience';
import router from '@/aifriend/router';

const store = useExperienceStore();
const people = computed(() => store.characters.filter((person) => store.data.conversations[person.id]?.length));

function preview(person) {
  const messages = store.data.conversations[person.id];
  if (!messages?.length || !store.loggedIn) return person.quote;
  return messages[messages.length - 1].text;
}
</script>
