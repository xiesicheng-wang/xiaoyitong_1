<template>
  <div class="page">
    <el-header class="header">
      <div class="brand">友邻帮</div>
      <div class="right">
        <span class="welcome">你好，{{ store.user?.nickname || store.user?.username }}</span>
        <el-button text type="danger" @click="onLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-main class="main">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-card>
            <template #header>我的信息</template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户名">{{ store.user?.username }}</el-descriptions-item>
              <el-descriptions-item label="昵称">{{ store.user?.nickname }}</el-descriptions-item>
              <el-descriptions-item label="信誉分">
                <el-tag :type="creditType">{{ store.user?.creditScore }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="实名状态">
                <el-tag :type="realnameType">{{ realnameText }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
        <el-col :span="16">
          <el-card>
            <template #header>社区动态</template>
            <el-empty description="二手交易、技能市场、社区公告正在建设中，敬请期待" />
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()

const creditType = computed(() => {
  const s = store.user?.creditScore ?? 0
  return s >= 80 ? 'success' : s >= 60 ? 'warning' : 'danger'
})
const realnameType = computed(() => (store.user?.realnameStatus === 2 ? 'success' : 'info'))
const realnameText = computed(() => ({ 0: '未实名', 1: '待审核', 2: '已实名' }[store.user?.realnameStatus] || '未实名'))

// 刷新页面后拉取最新用户信息（也顺带验证 token 是否仍有效）
onMounted(async () => {
  try {
    await store.fetchMe()
  } catch {
    /* 401 已由拦截器统一处理跳转登录 */
  }
})

function onLogout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  margin-top: 16px;
  padding: 0 24px;
}
.brand {
  font-size: 20px;
  font-weight: 600;
}
.right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.welcome {
  color: #606266;
  font-size: 14px;
}
.main {
  padding: 16px 0;
}
</style>
