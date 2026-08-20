import request from '@/aifriend/request';

const TestApi = {
  get: () =>
    request({
      url: '/AiFriend/test/get',
      method: 'GET',
      custom: {
        showLoading: false,
        showError: false,
      },
    }),
};

export default TestApi;
