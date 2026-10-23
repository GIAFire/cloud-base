<template>
  <view class="card">
    <view class="card-main" role="button" tabindex="0" :aria-label="'查看' + person.name"
          @click="$emit('detail', person.id)" @keydown.enter.self="$emit('detail', person.id)"
          @keydown.space.prevent.self="$emit('detail', person.id)">
      <view class="portrait"><img :src="portrait(person)" :alt="person.name" loading="lazy"/>
        <view class="name">{{ person.name }}</view>
      </view>
      <view class="cardbody">
        <p class="quote">“{{ person.quote }}”</p>
        <view class="pills"><span v-for="tag in person.tags" :key="tag">{{ tag }}</span></view>
      </view>
    </view>
    <view class="cardbody card-actions">
      <view class="cardfoot">
        <af-button class="favorite" :class="{ 'is-favorite': favorite }"
                   :aria-label="(favorite ? '取消收藏' : '收藏') + person.name" :aria-pressed="favorite"
                   @click.stop="$emit('favorite', person.id)"><span class="heart-symbol">{{
            favorite ? '♥' : '♡'
          }}</span><span class="favorite-count">{{ formatCount(person.favorites + (favorite ? 1 : 0)) }}</span>
        </af-button>
      </view>
    </view>
  </view>
</template>
<script setup>
import {portrait, formatCount} from '@/aifriend/domain/catalog';

defineProps({person: {type: Object, required: true}, favorite: Boolean});
defineEmits(['detail', 'favorite']);
</script>
