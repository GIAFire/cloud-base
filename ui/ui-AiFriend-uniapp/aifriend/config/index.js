const envBaseUrl = import.meta.env.AIFRIEND_BASE_URL || 'http://127.0.0.1:8088';

let baseUrl = envBaseUrl;

// H5 本地开发走 Vite 代理；App、小程序和生产环境直接访问后端地址。
// #ifdef H5
if (import.meta.env.DEV && import.meta.env.AIFRIEND_H5_PROXY_PREFIX) {
  baseUrl = import.meta.env.AIFRIEND_H5_PROXY_PREFIX;
}
// #endif

export const requestConfig = {
  baseUrl,
  timeout: 10000,
};

export default {
  request: requestConfig,
};
