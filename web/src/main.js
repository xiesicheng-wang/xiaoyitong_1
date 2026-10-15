import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/theme.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')

/* ---------------- 前端错误上报：浏览器报错 → 后端 logs/frontend.log ---------------- */
let lastReportAt = 0
function reportError(kind, message, stack) {
  const now = Date.now()
  if (now - lastReportAt < 3000) return // 3 秒内只报一条，防止错误风暴刷爆日志
  lastReportAt = now
  try {
    fetch('/api/logs/frontend', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      keepalive: true,
      body: JSON.stringify({
        level: 'ERROR',
        kind,
        message: String(message || '').slice(0, 1000),
        stack: String(stack || '').slice(0, 2000),
        page: location.href,
        ua: navigator.userAgent
      })
    })
  } catch { /* 上报失败静默，别让排错本身报错 */ }
}
window.addEventListener('error', (e) => reportError('window', e.message, e.error?.stack))
window.addEventListener('unhandledrejection', (e) => reportError('promise', e.reason, e.reason?.stack))
