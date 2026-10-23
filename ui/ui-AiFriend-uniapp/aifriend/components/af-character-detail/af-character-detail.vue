<template>
  <af-modal :open="Boolean(person)" :title="person?.name || ''" @close="$emit('close')">
    <template #cover><img v-if="person" class="detail-cover" :src="portrait(person)" :alt="person.name"/></template>
    <template v-if="person">
      <p class="detail-meta">{{ person.age }}岁 · {{ person.mbti }} · {{ person.type }}</p>
      <p class="sub">{{ person.intro }}</p>
      <view class="pills"><span v-for="tag in person.tags" :key="tag">{{ tag }}</span></view>
      <af-button class="primary" @click="store.openChat(person.id)">♡ 开始我们的故事</af-button>
    </template>
  </af-modal>
</template>
<script setup>
import {portrait} from '@/aifriend/domain/catalog';
import {useExperienceStore} from '@/aifriend/store/experience';

defineProps({person: {type: Object, default: null}});
defineEmits(['close']);
const store = useExperienceStore();
</script>
