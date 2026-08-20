import uniModule from '@dcloudio/vite-plugin-uni';
import { defineConfig, loadEnv } from 'vite';

const uni = uniModule.default || uniModule;

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'AIFRIEND_');
  const proxyPrefix = env.AIFRIEND_H5_PROXY_PREFIX;

  return {
    envPrefix: 'AIFRIEND_',
    plugins: [uni()],
    server: {
      host: '0.0.0.0',
      port: Number(env.AIFRIEND_DEV_PORT || 3001),
      proxy: proxyPrefix
        ? {
            [proxyPrefix]: {
              target: env.AIFRIEND_BASE_URL,
              changeOrigin: true,
              rewrite: (path) => path.replace(new RegExp(`^${proxyPrefix}`), ''),
            },
          }
        : undefined,
    },
  };
});
