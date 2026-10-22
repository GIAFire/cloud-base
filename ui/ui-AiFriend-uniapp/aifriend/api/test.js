import request from '@/aifriend/request';

const TestApi = {
  get: () =>
    request({
      url: '/agent/test/get',
      method: 'GET',
      custom: {
        auth: true,
        showLoading: false,
        showError: false,
      },
    }),
  getWithoutLogin: () =>
    request({
      url: '/agent/test/get',
      method: 'GET',
      custom: {
        auth: false,
        isToken: false,
        showLoading: false,
        showError: false,
        skipRefresh: true,
      },
    }),
};

export default TestApi;
