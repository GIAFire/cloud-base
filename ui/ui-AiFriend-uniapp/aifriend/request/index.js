import { requestConfig } from '@/aifriend/config';
import { getTerminal } from '@/aifriend/helper/const';
import {
  clearToken,
  getAccessToken,
  getRefreshToken,
  setToken,
} from '@/aifriend/helper/token';

const defaultCustom = {
  auth: false,
  isToken: true,
  showLoading: true,
  showError: true,
  showSuccess: false,
  loadingMsg: '加载中',
  successMsg: '',
  skipRefresh: false,
};

let loadingCount = 0;
let refreshPromise = null;

function startLoading(text) {
  loadingCount += 1;
  if (loadingCount === 1) {
    uni.showLoading({ title: text, mask: true });
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

function appendParams(url, params = {}) {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null)
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');

  if (!query) return url;
  return `${url}${url.includes('?') ? '&' : '?'}${query}`;
}

function joinUrl(baseUrl, path) {
  if (/^https?:\/\//i.test(path)) return path;
  return `${baseUrl.replace(/\/$/, '')}/${path.replace(/^\//, '')}`;
}

function getRequestBaseUrl() {
  return joinUrl(requestConfig.baseUrl, requestConfig.apiPath);
}

function send(options, custom, token = getAccessToken()) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: appendParams(joinUrl(getRequestBaseUrl(), options.url), options.params),
      method: options.method || 'GET',
      data: options.data,
      timeout: options.timeout || requestConfig.timeout,
      header: {
        Accept: '*/*',
        'Content-Type': 'application/json;charset=UTF-8',
        terminal: getTerminal(),
        'tenant-id': uni.getStorageSync('tenant-id') || requestConfig.tenantId,
        ...(custom.isToken && token ? { Authorization: token } : {}),
        ...options.header,
      },
      success: resolve,
      fail: (detail) => {
        const message = detail.errMsg?.includes('timeout') ? '请求超时' : '无法连接到后端服务';
        reject(createError(message, detail));
      },
    });
  });
}

function saveAuthToken(url, body) {
  if (url.includes('/member/auth/') && body?.data?.accessToken) {
    setToken(body.data.accessToken, body.data.refreshToken);
  }
}

function redirectToLogin() {
  clearToken();
  const pages = getCurrentPages();
  const currentRoute = pages[pages.length - 1]?.route;
  if (currentRoute !== 'pages/auth/login') {
    uni.reLaunch({ url: '/pages/auth/login' });
  }
}

async function refreshAccessToken() {
  if (refreshPromise) return refreshPromise;

  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;

  refreshPromise = send(
    {
      url: '/member/auth/refresh-token',
      method: 'POST',
      params: { refreshToken },
    },
    {
      ...defaultCustom,
      isToken: false,
      showLoading: false,
      showError: false,
      skipRefresh: true,
    },
    '',
  )
    .then((response) => {
      const body = response.data;
      if (response.statusCode >= 200 && response.statusCode < 300 && body?.code === 0) {
        setToken(body.data.accessToken, body.data.refreshToken);
        return true;
      }
      return false;
    })
    .catch(() => false)
    .finally(() => {
      refreshPromise = null;
    });

  return refreshPromise;
}

function showError(message) {
  uni.showToast({ title: message, icon: 'none', mask: true });
}

export default async function request(options) {
  const custom = {
    ...defaultCustom,
    ...options.custom,
  };

  if (custom.auth && !getAccessToken()) {
    redirectToLogin();
    return Promise.reject(createError('请先登录'));
  }

  if (custom.showLoading) {
    startLoading(custom.loadingMsg);
  }

  try {
    let response = await send(options, custom);
    let body = response.data;
    const unauthorized = response.statusCode === 401 || body?.code === 401;

    if (unauthorized && !custom.skipRefresh) {
      const refreshed = await refreshAccessToken();
      if (!refreshed) {
        redirectToLogin();
        throw createError('登录已过期，请重新登录', body);
      }
      response = await send(options, custom);
      body = response.data;
    }

    if (response.statusCode < 200 || response.statusCode >= 300) {
      const message = body?.msg || `请求失败（HTTP ${response.statusCode}）`;
      if (custom.showError) showError(message);
      throw createError(message, body);
    }

    saveAuthToken(options.url, body);

    if (body?.code !== undefined && body.code !== 0) {
      if (custom.showError) {
        showError(body.msg || '服务器开小差啦，请稍后再试');
      }
      return body;
    }

    if (custom.showSuccess && custom.successMsg) {
      uni.showToast({ title: custom.successMsg, icon: 'none' });
    }

    return body;
  } catch (error) {
    if (custom.showError && !error.detail?.code) {
      showError(error.message || '网络请求出错');
    }
    throw error;
  } finally {
    if (custom.showLoading) {
      stopLoading();
    }
  }
}
