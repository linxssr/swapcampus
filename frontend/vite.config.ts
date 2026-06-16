import { fileURLToPath, URL } from 'node:url';
import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      host: '0.0.0.0',
      port: 5173,
      proxy: {
        '/api': {
          target: env.VITE_API_PROXY_TARGET || 'http://localhost:8080',
          changeOrigin: true,
        },
        '/ws': {
          target: env.VITE_WS_PROXY_TARGET || 'ws://localhost:8080',
          ws: true,
          changeOrigin: true,
        },
        '/minio': {
          target: env.VITE_MINIO_PROXY_TARGET || 'http://121.41.92.121:9000',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/minio/, ''),
        },
      },
    },
  };
});
