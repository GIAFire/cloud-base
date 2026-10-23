<template>
  <af-shell active="video" feed>
    <view class="feed-layout" @wheel.prevent="wheel">
      <view class="feed-device">
        <view class="feed-top">
          <view class="feed-topline">
            <af-button class="feed-exit" aria-label="返回首页" @click="router.home()">‹</af-button>
            <view class="feed-tabs">
              <af-button v-for="tab in ['关注', '推荐']" :key="tab" :class="{ active: store.feedTab === tab }"
                         @click="changeTab(tab)">{{ tab }}
              </af-button>
            </view>
            <span class="feed-demo-label">动态封面演示</span>
          </view>
        </view>
        <swiper v-if="visibleClips.length" class="feed-stream" vertical :current="store.feedIndex" :duration="350"
                @change="changeIndex">
          <swiper-item v-for="(clip, index) in visibleClips" :key="clip.id">
            <view class="short-clip" :aria-hidden="index !== store.feedIndex"
                  :inert="index !== store.feedIndex ? true : undefined"
                  :class="{ 'is-active': index === store.feedIndex && active, 'is-paused': paused || Boolean(store.commentClip) || Boolean(selected) }">
              <img class="clip-cover" :src="portrait(characterById(clip.characterId), 1000)"
                   :alt="characterById(clip.characterId).name + '的动态封面'"/>
              <view class="clip-shade"/>
              <af-button class="clip-toggle" :aria-label="paused ? '播放封面动效' : '暂停封面动效'"
                         @click="paused = !paused" @dblclick="like(clip.id, true)"><span
                  class="pause-indicator">▷</span></af-button>
              <view class="clip-actions">
                <af-button class="clip-follow" :class="{ followed: store.data.followed.includes(clip.characterId) }"
                           :aria-label="(store.data.followed.includes(clip.characterId) ? '取消关注' : '关注') + characterById(clip.characterId).name"
                           :aria-pressed="store.data.followed.includes(clip.characterId)"
                           @click="store.interact('followed', clip.characterId, 'follow')"><img
                    :src="portrait(characterById(clip.characterId), 100)" alt=""/><span
                    class="follow-symbol">{{ store.data.followed.includes(clip.characterId) ? '✓' : '+' }}</span>
                </af-button>
                <af-button :class="{ 'is-liked': store.data.liked.includes(clip.id) }" aria-label="点赞"
                           :aria-pressed="store.data.liked.includes(clip.id)" @click="like(clip.id)">
                  <af-icon name="heart" :size="30"/>
                  <span class="action-count">{{
                      formatCount(clip.likes + Number(store.data.liked.includes(clip.id)))
                    }}</span></af-button>
                <af-button aria-label="打开留言" @click="store.commentClip = clip.id">
                  <af-icon name="comment" :size="30"/>
                  <span class="action-count">{{ 2 + (store.data.comments[clip.id]?.length || 0) }}</span></af-button>
                <af-button :class="{ 'is-saved': store.data.saved.includes(clip.id) }" aria-label="收藏动态"
                           :aria-pressed="store.data.saved.includes(clip.id)"
                           @click="store.interact('saved', clip.id, 'save')">
                  <af-icon name="save" :size="30"/>
                  <span class="action-count">{{
                      formatCount(clip.saves + Number(store.data.saved.includes(clip.id)))
                    }}</span></af-button>
                <af-button aria-label="查看角色详情" @click="selected = characterById(clip.characterId)">
                  <af-icon name="share" :size="28"/>
                  <span class="action-count">角色</span></af-button>
              </view>
              <view class="clip-info">
                <view class="clip-topic"><span>✧ {{ clip.topic }}</span><span>AI 角色</span></view>
                <view class="clip-name">
                  <af-button @click="selected = characterById(clip.characterId)">
                    @{{ characterById(clip.characterId).name }}
                  </af-button>
                  <small>{{ characterById(clip.characterId).mbti }}</small></view>
                <p class="clip-title">{{ clip.title }}</p>
                <p class="clip-subtitle">{{ clip.subtitle }}</p>
                <af-button class="clip-chat" @click="store.openChat(clip.characterId)">
                  <af-icon name="chat" :size="16"/>
                  和她聊聊 ↗
                </af-button>
                <view class="clip-music"><b>♪</b>{{ clip.music }} · 氛围文案</view>
              </view>
              <view class="clip-progress"><span/></view>
            </view>
          </swiper-item>
        </swiper>
        <view v-else class="feed-empty"><span class="empty-heart">♡</span>
          <h2>让喜欢，常常出现。</h2>
          <p>关注你心动的角色，<br/>她们的动态会出现在这里。</p>
          <af-button class="primary" @click="changeTab('推荐')">去发现心动 →</af-button>
        </view>
      </view>
      <view class="feed-side">
        <view class="eyebrow">LITTLE MOMENTS, BIG FEELINGS</view>
        <h1>在每个瞬间，<br/>遇见她的日常。</h1>
        <p>上滑，下一个心动。<br/>喜欢她，就开启一段对话。</p>
        <view class="feed-nav-buttons">
          <af-button aria-label="上一条" :disabled="store.feedIndex === 0 || !visibleClips.length" @click="move(-1)">
            <af-icon name="up"/>
          </af-button>
          <af-button aria-label="下一条" :disabled="store.feedIndex >= visibleClips.length - 1" @click="move(1)">
            <af-icon name="down"/>
          </af-button>
        </view>
        <view class="feed-position">{{ visibleClips.length ? String(store.feedIndex + 1).padStart(2, '0') : '00' }} /
          {{ String(visibleClips.length).padStart(2, '0') }}
        </view>
        <p class="feed-tip">滚轮 / ↑ ↓ 切换 · 点击画面暂停<br/>双击画面表达喜欢<br/><br/>当前为角色封面动效，后续可接真实短视频。
        </p>
      </view>
    </view>
    <af-character-detail :person="selected" @close="selected = null"/>
    <af-modal :open="Boolean(store.commentClip)" title="心动留言" @close="store.commentClip = ''">
      <scroll-view class="comments-list" scroll-y>
        <view v-for="(row, index) in commentRows" :key="row.id || index" class="comment-row">
          <view class="comment-avatar">{{ row.me ? '屿' : '♡' }}</view>
          <view class="comment-body"><b>{{ row.me ? '我' : row.name }}</b>
            <p>{{ row.text }}</p><small>{{ row.me ? '本地留言' : '示例留言' }}</small></view>
        </view>
      </scroll-view>
      <view class="comment-form"><input :value="store.data.commentDrafts[store.commentClip] || ''"
                                        placeholder="把这一刻的心动写下来…" aria-label="写下留言" :maxlength="200"
                                        @input="store.saveDraft('commentDrafts', store.commentClip, $event.detail.value)"
                                        @confirm="store.addComment(store.commentClip)"/>
        <af-button @click="store.addComment(store.commentClip)">发送 ↑</af-button>
      </view>
      <p class="comment-note">演示留言保存在当前浏览器。</p>
    </af-modal>
  </af-shell>
</template>
<script setup>
import {computed, onMounted, onUnmounted, ref, watch} from 'vue';
import {onShow, onHide} from '@dcloudio/uni-app';
import {clips, characterById, portrait, formatCount} from '@/aifriend/domain/catalog';
import {useExperienceStore} from '@/aifriend/store/experience';
import router from '@/aifriend/router';

const store = useExperienceStore(), selected = ref(null), paused = ref(false), active = ref(true);
const visibleClips = computed(() => store.feedTab === '关注' ? clips.filter((clip) => store.data.followed.includes(clip.characterId)) : clips);
const commentRows = computed(() => [
  {name: '路过的月亮', text: '好像真的被这一瞬间治愈了 ♡'}, {name: '小岛来信', text: '今天也想听你说一句晚安。'},
  ...(store.data.comments[store.commentClip] || []),
]);
watch(() => visibleClips.value.length, (length) => {
  store.feedIndex = Math.max(0, Math.min(store.feedIndex, length - 1));
});
onShow(() => {
  active.value = true;
});
onHide(() => {
  active.value = false;
});

function changeTab(tab) {
  store.feedTab = tab;
  store.feedIndex = 0;
  paused.value = false;
}

function changeIndex(event) {
  store.feedIndex = event.detail.current;
  paused.value = false;
}

function move(direction) {
  store.feedIndex = Math.max(0, Math.min(visibleClips.value.length - 1, store.feedIndex + direction));
  paused.value = false;
}

let lastWheel = 0;

function wheel(event) {
  if (store.commentClip || selected.value || Date.now() - lastWheel < 450 || Math.abs(event.deltaY) < 10) return;
  lastWheel = Date.now();
  move(event.deltaY > 0 ? 1 : -1);
}

function like(id, force = false) {
  if (force && store.data.liked.includes(id)) return;
  store.interact('liked', id, 'like');
}

function keydown(event) {
  if (!active.value || store.commentClip || selected.value || /INPUT|TEXTAREA/.test(event.target?.tagName)) return;
  if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
    event.preventDefault();
    move(event.key === 'ArrowDown' ? 1 : -1);
  }
  if (event.code === 'Space') {
    event.preventDefault();
    paused.value = !paused.value;
  }
}

onMounted(() => {
  // #ifdef H5
  document.addEventListener('keydown', keydown);
  // #endif
});
onUnmounted(() => {
  // #ifdef H5
  document.removeEventListener('keydown', keydown);
  // #endif
});
</script>
