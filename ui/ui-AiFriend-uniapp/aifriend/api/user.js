import request from '@/aifriend/request';

const UserApi = {
  getUserInfo: (custom = {}) =>
    request({
      url: '/member/user/get',
      method: 'GET',
      custom: {
        auth: true,
        showLoading: false,
        ...custom,
      },
    }),
};

export default UserApi;
