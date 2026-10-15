import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发环境把 /api 代理到后端，避免跨域；生产环境由 Nginx 做同样的事。
// 默认 8080，测试环境可用环境变量 VITE_API_TARGET 覆盖（如 http://127.0.0.1:18080）
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_TARGET || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
