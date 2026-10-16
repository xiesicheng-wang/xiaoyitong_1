<template>
  <el-header class="site-header">
    <div class="brand" @click="router.push('/')">友邻帮<span class="slogan">让社区互助起来</span></div>

    <nav class="nav">
      <span v-for="item in navItems" :key="item.path"
            class="nav-item" :class="{ active: isActive(item.path) }"
            @click="router.push(item.path)">{{ item.label }}</span>
    </nav>

    <div class="right">
      <!-- 未登录 -->
      <template v-if="!store.token">
        <el-button text type="primary" @click="router.push('/login')">登录</el-button>
        <el-button type="primary" @click="router.push('/register')">注册</el-button>
      </template>

      <!-- 已登录 -->
      <template v-else>
        <!-- 通知铃铛 -->
        <el-popover placement="bottom" :width="340" trigger="click" @show="loadNoti">
          <template #reference>
            <el-badge :value="unread" :hidden="unread === 0" :max="99" class="bell">
              <el-button text>通知</el-button>
            </el-badge>
          </template>
          <div v-if="notiList.length">
            <div v-for="n in notiList" :key="n.id" class="noti-item" @click="goRelated(n)">
              <div class="noti-title">
                <span class="dot" v-if="!n.readFlag"></span>{{ n.title }}
              </div>
              <div class="noti-content">{{ n.content }}</div>
            </div>
            <el-button size="small" text type="primary" style="width: 100%" @click="readAll">全部已读</el-button>
          </div>
          <el-empty v-else description="暂无通知" :image-size="50" />
        </el-popover>

        <span v-for="item in userItems" :key="item.path"
              class="nav-item" :class="{ active: isActive(item.path) }"
              @click="router.push(item.path)">{{ item.label }}</span>

        <el-button v-if="store.user?.role === 1" text type="warning"
                   @click="router.push('/admin')">管理后台</el-button>
        <el-button text type="danger" @click="onLogout">退出</el-button>
      </template>
    </div>
  </el-header>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import request from '../api/request'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const navItems = [
  { path: '/', label: '二手市场' },
  { path: '/skills', label: '技能服务' },
  { path: '/chat', label: '社区群聊' },
  { path: '/nearby', label: '附近的人' }
]
const userItems = [
  { path: '/me', label: '个人中心' },
  { path: '/orders', label: '订单' },
  { path: '/wallet', label: '钱包' }
]

function isActive(path) {
  return path === '/' ? route.path === '/' : route.path.startsWith(path)
}

/* 通知 */
const unread = ref(0)
const notiList = ref([])

async function loadNoti() {
  const data = await request.get('/notifications', { params: { page: 1, size: 8 } })
  notiList.value = data.records
  unread.value = Number((await request.get('/notifications/unread-count')).count)
}

async function readAll() {
  await request.post('/notifications/read-all')
  unread.value = 0
  await loadNoti()
}

/* 通知点击 → 跳到相关页面 */
function goRelated(n) {
  if (!n.readFlag) {
    request.post(`/notifications/${n.id}/read`).finally(loadNoti)
  }
  const map = { ORDER: '/orders', REALNAME: '/realname', PRODUCT: '/', USER: '/me' }
  router.push(map[n.relatedType] || '/me')
}

function onLogout() {
  store.logout()
  router.push('/login')
}

onMounted(() => {
  if (store.token) loadNoti()
})
</script>

<style scoped>
.site-header {
  display: flex; align-items: center; gap: 18px;
  background: rgba(255, 255, 255, 0.92); backdrop-filter: blur(8px);
  border-bottom: 1px solid #f6e2ea;
  position: sticky; top: 0; z-index: 100;
  padding: 0 28px; height: 62px;
}
.brand {
  font-size: 21px; font-weight: 800; cursor: pointer;
  background: linear-gradient(120deg, #c94d7e, #ee7bab);
  -webkit-background-clip: text; background-clip: text; color: transparent;
  letter-spacing: 1px; white-space: nowrap;
}
.slogan {
  font-size: 11px; font-weight: 400; letter-spacing: 0;
  margin-left: 6px; color: #d9a3ba; -webkit-text-fill-color: #d9a3ba;
}
.nav { display: flex; gap: 4px; flex: 1; }
.nav-item {
  padding: 6px 14px; border-radius: 16px; cursor: pointer;
  font-size: 14px; color: #6b5b62; transition: all 0.2s; white-space: nowrap;
}
.nav-item:hover { color: #c94d7e; background: #fdf3f7; }
.nav-item.active { color: #fff; background: linear-gradient(120deg, #ee7bab, #f191b6); font-weight: 600; }
.right { display: flex; align-items: center; gap: 4px; }
.bell { cursor: pointer; }
.noti-item { padding: 7px 4px; border-bottom: 1px solid #f6e2ea; cursor: pointer; }
.noti-item:hover { background: #fdf3f7; border-radius: 6px; }
.noti-title { font-size: 13px; font-weight: 600; display: flex; align-items: center; gap: 6px; }
.dot { width: 7px; height: 7px; border-radius: 50%; background: #ee7bab; display: inline-block; }
.noti-content { font-size: 12px; color: #909399; margin-top: 2px; }
</style>
