const envBaseUrl = import.meta.env.AIFRIEND_BASE_URL || 'http://localhost:8081';
const apiPath = import.meta.env.AIFRIEND_API_PATH || '/app-api';

let baseUrl = envBaseUrl;

// H5 本地开发使用同源的 /app-api 地址，由 Vite 代理到网关；
// App、小程序和生产环境直接访问网关地址。
// #ifdef H5
if (import.meta.env.DEV && import.meta.env.AIFRIEND_H5_PROXY_ENABLED === 'true') {
  baseUrl = '';
}
// #endif

export const requestConfig = {
  baseUrl,
  apiPath,
  timeout: 10000,
  tenantId: 1,
};

export default {
  request: requestConfig,
};
