<template>
  <af-modal :open="Boolean(person)" title="视频陪伴 · 演示模式" wide @close="end">
    <view v-if="person" class="call">
      <img :src="portrait(person, 1000)" :alt="person.name"/>
      <view class="call-info"><h2>{{ person.name }}</h2>
        <p>“很高兴，此刻能陪在你身边。”</p><span class="call-status">静态画面 · {{
            muted ? '已静音' : '麦克风未开启'
          }}</span>
        <view class="call-controls">
          <af-button :aria-label="muted ? '取消静音演示' : '静音演示'" :aria-pressed="muted" @click="muted = !muted">
            {{ muted ? '静音' : '♩' }}
          </af-button>
          <af-button class="end" aria-label="结束视频演示" @click="end">✕</af-button>
          <af-button aria-label="摄像头说明" @click="notify('演示模式，摄像头未开启')">▣</af-button>
        </view>
      </view>
    </view>
    <p class="hint">静态画面演示，不会开启摄像头或麦克风。</p>
  </af-modal>
</template>
<script setup>
import {ref} from 'vue';
import {portrait} from '@/aifriend/domain/catalog';
import {notify} from '@/aifriend/store/experience';

defineProps({person: {type: Object, default: null}});
const emit = defineEmits(['close']);
const muted = ref(false);

function end() {
  muted.value = false;
  emit('close');
}
</script>
