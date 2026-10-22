import uniModule from '@dcloudio/vite-plugin-uni';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { defineConfig, loadEnv } from 'vite';

const uni = uniModule.default || uniModule;
const projectDir = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig(({ mode }) => {
  // HBuilderX 的工作目录不一定是项目目录，必须按配置文件位置读取 .env.development。
  const env = loadEnv(mode, projectDir, 'AIFRIEND_');
  const apiPath = env.AIFRIEND_API_PATH || '/app-api';
  const proxyEnabled = env.AIFRIEND_H5_PROXY_ENABLED === 'true';

  return {
    envPrefix: 'AIFRIEND_',
    plugins: [uni()],
    server: {
      host: '0.0.0.0',
      port: Number(env.AIFRIEND_DEV_PORT || 3001),
      proxy: proxyEnabled
        ? {
            [apiPath]: {
              target: env.AIFRIEND_BASE_URL,
              changeOrigin: true,
            },
          }
        : undefined,
    },
  };
});
