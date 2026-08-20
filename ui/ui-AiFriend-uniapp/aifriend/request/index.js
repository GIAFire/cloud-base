import { requestConfig } from '@/aifriend/config';

const defaultCustom = {
  auth: false,
  showLoading: true,
  showError: true,
  loadingText: '加载中',
};

let loadingCount = 0;

function startLoading(text) {
  loadingCount += 1;
  if (loadingCount === 1) {
    uni.showLoading({
      title: text,
      mask: true,
    });
  }
}

function stopLoading() {
  loadingCount = Math.max(loadingCount - 1, 0);
  if (loadingCount === 0) {
    uni.hideLoading();
  }
}

function createError(message, detail) {
  const error = new Error(message);
  error.detail = detail;
  return error;
}

function joinUrl(baseUrl, url) {
  if (/^https?:\/\//i.test(url)) return url;
  return `${baseUrl.replace(/\/$/, '')}/${url.replace(/^\//, '')}`;
}

export default function request(options) {
  const custom = {
    ...defaultCustom,
    ...options.custom,
  };
  const token = uni.getStorageSync('token');

  if (custom.auth && !token) {
    return Promise.reject(createError('请先登录'));
  }

  if (custom.showLoading) {
    startLoading(custom.loadingText);
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: joinUrl(requestConfig.baseUrl, options.url),
      method: options.method || 'GET',
      data: options.data,
      timeout: options.timeout || requestConfig.timeout,
      header: {
        Accept: 'application/json',
        'Content-Type': 'application/json;charset=UTF-8',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.header,
      },
      success: (response) => {
        if (response.statusCode >= 200 && response.statusCode < 300) {
          resolve(response.data);
          return;
        }

        const message = response.data?.msg || `请求失败（HTTP ${response.statusCode}）`;
        if (custom.showError) {
          uni.showToast({ title: message, icon: 'none' });
        }
        reject(createError(message, response.data));
      },
      fail: (detail) => {
        const message = detail.errMsg?.includes('timeout') ? '请求超时' : '无法连接到后端服务';
        if (custom.showError) {
          uni.showToast({ title: message, icon: 'none' });
        }
        reject(createError(message, detail));
      },
      complete: () => {
        if (custom.showLoading) {
          stopLoading();
        }
      },
    });
  });
}
