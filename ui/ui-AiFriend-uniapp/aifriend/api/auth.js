import request from '@/aifriend/request';

const AuthApi = {
  // 使用手机号 + 密码登录
  login: (data) =>
    request({
      url: '/member/auth/login',
      method: 'POST',
      data,
      custom: {
        showSuccess: true,
        loadingMsg: '登录中',
        successMsg: '登录成功',
      },
    }),

  // 使用手机号 + 验证码登录；手机号未注册时后端会自动注册
  smsLogin: (data) =>
    request({
      url: '/member/auth/sms-login',
      method: 'POST',
      data,
      custom: {
        showSuccess: true,
        loadingMsg: '登录中',
        successMsg: '登录成功',
      },
    }),

  sendSmsCode: (mobile, scene = 1) =>
    request({
      url: '/member/auth/send-sms-code',
      method: 'POST',
      data: {
        mobile,
        scene,
      },
      custom: {
        showSuccess: true,
        loadingMsg: '发送中',
        successMsg: '发送成功',
      },
    }),

  logout: () =>
    request({
      url: '/member/auth/logout',
      method: 'POST',
    }),

  refreshToken: (refreshToken) =>
    request({
      url: '/member/auth/refresh-token',
      method: 'POST',
      params: {
        refreshToken,
      },
      custom: {
        isToken: false,
        showLoading: false,
        showError: false,
        skipRefresh: true,
      },
    }),
};

export default AuthApi;
