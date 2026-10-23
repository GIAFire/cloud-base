<template>
  <af-shell active="video" feed>
    <view class="feed-layout">
      <view class="feed-device">
        <view class="feed-top"><view class="feed-topline">
          <af-button class="feed-exit" aria-label="返回首页" @click="router.home()">‹</af-button>
          <view class="feed-tabs"><af-button v-for="tab in ['关注', '推荐']" :key="tab" :class="{ active: store.feedTab === tab }" @click="changeTab(tab)">{{ tab }}</af-button></view>
        </view></view>
        <swiper v-if="visibleClips.length" class="feed-stream" vertical :current="store.feedIndex" @change="store.feedIndex = $event.detail.current">
          <swiper-item v-for="(clip, index) in visibleClips" :key="clip.id">
            <view class="short-clip" :aria-hidden="index !== store.feedIndex" :inert="index !== store.feedIndex ? true : undefined">
              <video v-if="clip.videoUrl" class="clip-cover" :src="clip.videoUrl" :poster="clip.coverUrl" controls />
              <img v-else-if="clip.coverUrl" class="clip-cover" :src="clip.coverUrl" :alt="clip.title" />
              <view class="clip-shade" />
              <view class="clip-actions">
                <af-button aria-label="关注角色" @click="store.interact('followed', clip.characterId, 'follow')"><af-icon name="chat" /></af-button>
                <af-button aria-label="点赞" @click="store.interact('liked', clip.id, 'like')"><af-icon name="heart" /><span>{{ formatCount(clip.likes || 0) }}</span></af-button>
                <af-button aria-label="打开留言" @click="store.commentClip = clip.id"><af-icon name="comment" /><span>{{ clip.commentCount || 0 }}</span></af-button>
                <af-button aria-label="收藏动态" @click="store.interact('saved', clip.id, 'save')"><af-icon name="save" /><span>{{ formatCount(clip.saves || 0) }}</span></af-button>
              </view>
              <view class="clip-info"><view class="clip-name"><af-button @click="selected = store.characterById(clip.characterId)">@{{ store.characterById(clip.characterId)?.name }}</af-button></view><p class="clip-title">{{ clip.title }}</p><p class="clip-subtitle">{{ clip.subtitle }}</p><af-button class="clip-chat" @click="store.openChat(clip.characterId)">和她聊聊 ↗</af-button></view>
            </view>
          </swiper-item>
        </swiper>
        <view v-else class="feed-empty"><span class="empty-heart">♡</span><h2>{{ store.feedTab === '关注' ? '暂无关注动态' : '暂无视频动态' }}</h2><p>新的心动瞬间，敬请期待。</p><af-button v-if="store.feedTab === '关注'" class="primary" @click="changeTab('推荐')">看看推荐 →</af-button></view>
      </view>
      <view class="feed-side"><view class="eyebrow">LITTLE MOMENTS, BIG FEELINGS</view><h1>在每个瞬间，<br />遇见她的日常。</h1><p>喜欢她，就开启一段对话。</p><view v-if="visibleClips.length" class="feed-nav-buttons"><af-button aria-label="上一条" :disabled="store.feedIndex === 0" @click="move(-1)"><af-icon name="up" /></af-button><af-button aria-label="下一条" :disabled="store.feedIndex >= visibleClips.length - 1" @click="move(1)"><af-icon name="down" /></af-button></view></view>
    </view>
    <af-character-detail :person="selected" @close="selected = null" />
    <af-modal :open="Boolean(store.commentClip)" title="心动留言" @close="store.commentClip = ''">
      <scroll-view class="comments-list" scroll-y><view v-if="!commentRows.length" class="empty">暂无留言</view><view v-for="row in commentRows" :key="row.id" class="comment-row"><view class="comment-body"><b>{{ row.name }}</b><p>{{ row.text }}</p></view></view></scroll-view>
      <view class="comment-form"><input :value="store.data.commentDrafts[store.commentClip] || ''" placeholder="把这一刻的心动写下来…" aria-label="写下留言" :maxlength="200" @input="store.saveDraft('commentDrafts', store.commentClip, $event.detail.value)" @confirm="store.addComment(store.commentClip)" /><af-button @click="store.addComment(store.commentClip)">发送 ↑</af-button></view>
    </af-modal>
  </af-shell>
</template>
<script setup>
import { computed, ref, watch } from 'vue';
import { formatCount } from '@/aifriend/domain/catalog';
import { useExperienceStore } from '@/aifriend/store/experience';
import router from '@/aifriend/router';
const store = useExperienceStore(), selected = ref(null);
const visibleClips = computed(() => store.feedTab === '关注' ? store.clips.filter((clip) => store.data.followed.includes(clip.characterId)) : store.clips);
const commentRows = computed(() => store.data.comments[store.commentClip] || []);
watch(() => visibleClips.value.length, (length) => { store.feedIndex = Math.max(0, Math.min(store.feedIndex, length - 1)); });
function changeTab(tab) { store.feedTab = tab; store.feedIndex = 0; }
function move(delta) { store.feedIndex = Math.max(0, Math.min(visibleClips.value.length - 1, store.feedIndex + delta)); }
</script>
