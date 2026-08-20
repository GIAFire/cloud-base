<template>
  <view class="af-field">
    <text class="af-field__label">{{ label }}</text>
    <input
      class="af-field__input"
      :value="modelValue"
      :type="type"
      :password="password"
      :placeholder="placeholder"
      :maxlength="maxlength"
      @input="onInput"
    />
  </view>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  label: {
    type: String,
    required: true,
  },
  placeholder: {
    type: String,
    default: '',
  },
  type: {
    type: String,
    default: 'text',
  },
  password: {
    type: Boolean,
    default: false,
  },
  maxlength: {
    type: Number,
    default: 64,
  },
});

const emit = defineEmits(['update:modelValue']);

function onInput(event) {
  emit('update:modelValue', event.detail.value);
}
</script>

<style scoped lang="scss">
.af-field {
  margin-bottom: 28rpx;

  &__label {
    display: block;
    margin-bottom: 14rpx;
    color: #4c5060;
    font-size: 26rpx;
    font-weight: 600;
  }

  &__input {
    height: 92rpx;
    padding: 0 28rpx;
    border: 2rpx solid #e7e8ee;
    border-radius: 18rpx;
    background: #ffffff;
    font-size: 30rpx;
  }
}
</style>
