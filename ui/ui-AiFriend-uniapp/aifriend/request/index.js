import Request from 'luch-request';
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

function joinUrl(baseUrl, path) {
  if (/^https?:\/\//i.test(path)) return path;
  return `${baseUrl.replace(/\/$/, '')}/${path.replace(/^\//, '')}`;
}

function getRequestBaseUrl() {
  return joinUrl(requestConfig.baseUrl, requestConfig.apiPath);
}

function startLoading(config) {
  if (!config.custom.showLoading || config.__loadingStarted) return;

  config.__loadingStarted = true;
  loadingCount += 1;
  if (loadingCount === 1) {
    uni.showLoading({
      title: config.custom.loadingMsg,
      mask: true,
      fail: () => uni.hideLoading(),
    });
  }
}

function stopLoading(config) {
  if (!config?.__loadingStarted) return;

  config.__loadingStarted = false;
  loadingCount = Math.max(loadingCount - 1, 0);
  if (loadingCount === 0) {
    uni.hideLoading();
  }
}

function showError(message) {
  uni.showToast({
    title: message || '服务器开小差啦，请稍后再试',
    icon: 'none',
    mask: true,
  });
}

function createRequestError(message, detail, config) {
  const error = new Error(message);
  error.detail = detail;
  error.config = config;
  return error;
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

function getNetworkErrorMessage(error) {
  const statusCode = error?.statusCode;
  const statusMessages = {
    400: '请求错误',
    401: '登录已过期，请重新登录',
    403: '拒绝访问',
    404: '请求地址不存在',
    408: '请求超时',
    429: '请求频繁，请稍后再访问',
    500: '服务器开小差啦，请稍后再试',
    501: '服务未实现',
    502: '网络错误',
    503: '服务不可用',
    504: '网络超时',
    505: 'HTTP 版本不受支持',
  };

  if (error?.errMsg?.includes('timeout')) return '请求超时';

  // #ifdef H5
  if (error?.errMsg?.includes('Network')) {
    return window.navigator.onLine ? '服务器异常' : '请检查您的网络连接';
  }
  // #endif

  return error?.data?.msg || error?.message || statusMessages[statusCode] || '网络请求出错';
}

const http = new Request({
  baseURL: getRequestBaseUrl(),
  timeout: requestConfig.timeout,
  method: 'GET',
  header: {
    Accept: '*/*',
    'Content-Type': 'application/json;charset=UTF-8',
  },
  custom: defaultCustom,
  // #ifdef APP-PLUS
  sslVerify: false,
  // #endif
  // #ifdef H5
  withCredentials: false,
  // #endif
});

function refreshAccessToken() {
  if (refreshPromise) return refreshPromise;

  const refreshToken = getRefreshToken();
  if (!refreshToken) return Promise.resolve(false);

  refreshPromise = http
    .middleware({
      url: '/member/auth/refresh-token',
      method: 'POST',
      params: { refreshToken },
      custom: {
        isToken: false,
        showLoading: false,
        showError: false,
        skipRefresh: true,
      },
    })
    .then((body) => body?.code === 0 && Boolean(body?.data?.accessToken))
    .catch(() => false)
    .finally(() => {
      refreshPromise = null;
    });

  return refreshPromise;
}

async function retryAfterRefresh(config) {
  const refreshed = await refreshAccessToken();
  if (!refreshed) {
    redirectToLogin();
    throw createRequestError('登录已过期，请重新登录', null, config);
  }

  return http.middleware(config);
}

http.interceptors.request.use(
  (config) => {
    if (config.custom.auth && !getAccessToken()) {
      redirectToLogin();
      return Promise.reject(createRequestError('请先登录', null, config));
    }

    startLoading(config);

    const token = config.custom.isToken ? getAccessToken() : '';
    if (token) {
      config.header.Authorization = token;
    } else {
      delete config.header.Authorization;
    }

    config.header.terminal = getTerminal();
    config.header['tenant-id'] = uni.getStorageSync('tenant-id') || requestConfig.tenantId;
    return config;
  },
  (error) => Promise.reject(error),
);

http.interceptors.response.use(
  async (response) => {
    const { config } = response;
    const body = response.data;

    stopLoading(config);
    saveAuthToken(config.url, body);

    if (body?.code === 401) {
      if (config.custom.skipRefresh) {
        redirectToLogin();
        throw createRequestError(body.msg || '登录已过期，请重新登录', body, config);
      }
      return retryAfterRefresh(config);
    }

    if (body?.code !== undefined && body.code !== 0) {
      if (config.custom.showError) {
        showError(body.msg);
      }
      return body;
    }

    if (config.custom.showSuccess && config.custom.successMsg) {
      uni.showToast({
        title: config.custom.successMsg,
        icon: 'none',
      });
    }

    return body;
  },
  async (error) => {
    const config = error?.config;
    stopLoading(config);

    if (error?.statusCode === 401 && config && !config.custom.skipRefresh) {
      return retryAfterRefresh(config);
    }

    const message = getNetworkErrorMessage(error);
    if (config?.custom.showError) {
      showError(message);
    }

    return Promise.reject(createRequestError(message, error?.data || error, config));
  },
);

export default function request(config) {
  return http.middleware(config);
}
