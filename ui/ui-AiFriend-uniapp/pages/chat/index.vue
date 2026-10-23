<template>
  <af-shell active="conversations">
    <template v-if="person">
      <view class="sectionline chat-heading">
        <af-button @click="router.go(router.routes.conversations, { reset: true })">‹　{{ person.name }} <small
            class="pink">· 专属陪伴</small></af-button>
        <af-button class="auth-link" @click="invite">▷ 视频邀请</af-button>
      </view>
      <view v-if="store.loggedIn" class="chatbox">
        <p class="chat-notice">✧ AI 陪伴演示 · {{
            store.pending[person.id] ? '她正在组织回复…' : '她正在认真听你说'
          }}</p>
        <scroll-view class="messages" scroll-y :scroll-into-view="lastMessageId" :scroll-with-animation="true">
          <view v-for="row in messages" :id="'message-' + row.id" :key="row.id" class="message-line"
                :class="{ 'message-line--me': row.me }">
            <img v-if="!row.me" class="message-avatar" :src="portrait(person, 100)" :alt="person.name"/>
            <view class="bubble" :class="{ me: row.me }">{{ row.text }}</view>
          </view>
          <view v-if="store.pending[person.id]" class="typing">她正在输入 · · ·</view>
          <view id="messages-end" class="messages-end"/>
        </scroll-view>
        <view class="composer">
          <input :value="draft" placeholder="想和她说些什么…" aria-label="聊天内容" :maxlength="2000"
                 confirm-type="send" @input="store.saveDraft('drafts', person.id, $event.detail.value)"
                 @confirm="store.send(person.id)"/>
          <af-button class="primary" :disabled="!draft.trim() || store.pending[person.id]"
                     @click="store.send(person.id)">发送 ↑
          </af-button>
        </view>
      </view>
      <p class="hint">预设回复 · 对话保存在当前浏览器 · 尚未连接大模型与长期记忆</p>
      <af-call :person="callOpen ? person : null" @close="callOpen = false"/>
    </template>
    <view v-else class="empty"><h2>这位角色还没有来到小岛</h2>
      <af-button class="primary" @click="router.home()">返回发现</af-button>
    </view>
  </af-shell>
</template>
<script setup>
import {computed, nextTick, ref, watch} from 'vue';
import {onLoad, onShow} from '@dcloudio/uni-app';
import {characterById, portrait} from '@/aifriend/domain/catalog';
import {useExperienceStore} from '@/aifriend/store/experience';
import router from '@/aifriend/router';

const store = useExperienceStore();
const id = ref('');
const person = computed(() => characterById(id.value));
const messages = computed(() => store.data.conversations[id.value] || []);
const draft = computed(() => store.data.drafts[id.value] || '');
const callOpen = ref(false), lastMessageId = ref('');
onLoad((options) => {
  id.value = options.id || '';
});
onShow(() => {
  if (person.value && store.requireIdentity()) store.startConversation(id.value);
});
watch(() => [messages.value.length, Boolean(store.pending[id.value])], async () => {
  lastMessageId.value = '';
  await nextTick();
  lastMessageId.value = 'messages-end';
}, {immediate: true});

function invite() {
  if (store.requireIdentity()) callOpen.value = true;
}
</script>
