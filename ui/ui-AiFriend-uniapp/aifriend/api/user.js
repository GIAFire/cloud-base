import request from '@/aifriend/request';

const UserApi = {
  getUserInfo: () =>
    request({
      url: '/member/user/get',
      method: 'GET',
      custom: {
        auth: true,
        showLoading: false,
      },
    }),
};

export default UserApi;
