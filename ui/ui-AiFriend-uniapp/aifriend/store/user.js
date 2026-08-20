import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    isLogin: Boolean(uni.getStorageSync('token')),
    username: uni.getStorageSync('username') || '',
  }),
  actions: {
    loginPlaceholder({ username }) {
      // 当前仅提供可进入 Tab 页的前端占位流程，后续在这里调用正式登录 API。
      uni.setStorageSync('token', 'aifriend-placeholder-token');
      uni.setStorageSync('username', username);
      this.isLogin = true;
      this.username = username;
    },
    logout() {
      uni.removeStorageSync('token');
      uni.removeStorageSync('username');
      this.isLogin = false;
      this.username = '';
    },
  },
});

export default useUserStore;
