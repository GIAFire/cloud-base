import { defineStore } from 'pinia';
import AuthApi from '@/aifriend/api/auth';
import UserApi from '@/aifriend/api/user';
import { clearToken, getAccessToken, setToken as persistToken } from '@/aifriend/helper/token';

const defaultUserInfo = {
  avatar: '',
  nickname: '',
  gender: 0,
  mobile: '',
  point: 0,
};

export const useUserStore = defineStore('user', {
  state: () => ({
    isLogin: Boolean(getAccessToken()),
    userInfo: { ...defaultUserInfo },
  }),
  actions: {
    setToken(accessToken = '', refreshToken = '') {
      if (!accessToken) {
        clearToken();
        this.isLogin = false;
        return false;
      }

      persistToken(accessToken, refreshToken);
      this.isLogin = true;
      return true;
    },
    syncLoginState() {
      this.isLogin = Boolean(getAccessToken());
      return this.isLogin;
    },
    async getInfo() {
      const response = await UserApi.getUserInfo();
      if (response.code === 0) {
        this.userInfo = response.data;
      }
      return response;
    },
    async loginAfter() {
      if (!this.syncLoginState()) return;
      await this.getInfo();
    },
    async logout() {
      try {
        if (this.isLogin) {
          await AuthApi.logout();
        }
      } finally {
        this.setToken();
        this.userInfo = { ...defaultUserInfo };
      }
      return !this.isLogin;
    },
  },
});

export default useUserStore;
