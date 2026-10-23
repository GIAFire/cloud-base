<template>
  <af-shell active="home">
    <view class="hero">
      <img :src="portrait(characters[0], 1200)" alt="林晚"/>
      <view class="hero-copy">
        <view class="eyebrow">A CONNECTION THAT FEELS LIKE HOME</view>
        <h1>每一种心动，<br/>都值得被回应。</h1>
        <p>遇见懂你的她，让陪伴有了专属的温度。</p>
        <af-button class="primary" @click="selected = characters[0]">遇见今日心动　↗</af-button>
      </view>
      <span class="hero-index">01 ━ 02</span>
    </view>
    <view class="sectionline">
      <view><h2>发现你的心动 <span class="pink">✧</span></h2>
        <p>有趣的灵魂，正在等一个特别的你</p></view>
      <view class="search"><span>⌕</span><input v-model="store.discovery.query" aria-label="搜索角色"
                                                placeholder="搜索名字、性格或兴趣" :maxlength="100"/></view>
    </view>
    <view class="categories">
      <af-button v-for="category in categories" :key="category"
                 :class="{ active: store.discovery.category === category }"
                 @click="store.discovery.category = category">{{ category }}
      </af-button>
    </view>
    <view class="tags">
      <af-button v-for="tag in tags" :key="tag" :class="{ active: store.discovery.tag === tag }"
                 @click="store.discovery.tag = tag">{{ tag }}
      </af-button>
    </view>
    <view class="grid">
      <af-character-card v-for="person in people" :key="person.id" :person="person"
                         :favorite="store.data.favorites.includes(person.id)" @detail="selected = characterById($event)"
                         @favorite="store.interact('favorites', $event, 'favorite')"/>
      <view v-if="!people.length" class="empty"><p>还没有找到她，试试其他标签吧 ♡</p>
        <af-button class="auth-link" @click="reset">重置筛选</af-button>
      </view>
    </view>
    <p class="hint">每一次相遇，都是故事的开始 · 角色均为虚构成年 AI</p>
    <af-character-detail :person="selected" @close="selected = null"/>
  </af-shell>
</template>
<script setup>
import {computed, ref} from 'vue';
import {characters, characterById, portrait} from '@/aifriend/domain/catalog';
import {discoverCharacters} from '@/aifriend/services/experience';
import {useExperienceStore} from '@/aifriend/store/experience';

const store = useExperienceStore();
const selected = ref(null);
const categories = ['为你推荐', '人气角色', '最新相遇', '我的收藏'];
const tags = ['全部', '温柔治愈', '元气甜心', '知性姐姐', '二次元', '慢热', '倾听'];
const people = computed(() => discoverCharacters({...store.discovery, favorites: store.data.favorites}));

function reset() {
  store.discovery = {query: '', tag: '全部', category: '为你推荐'};
}
</script>
