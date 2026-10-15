<template>
  <el-container class="admin-layout">
    <el-aside width="200px" class="aside">
      <div class="logo">友邻帮·管理后台</div>
      <el-menu :default-active="$route.path" router background-color="#fff" text-color="#6b5b62"
               active-text-color="#c94d7e">
        <el-menu-item index="/admin">数据概览</el-menu-item>
        <el-menu-item index="/admin/realnames">实名审核</el-menu-item>
        <el-menu-item index="/admin/products">商品管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <el-tag type="danger" effect="dark">管理员</el-tag>
        <span class="name">{{ store.user?.nickname || store.user?.username }}</span>
        <el-button text @click="router.push('/')">← 返回前台</el-button>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import request from '../../api/request'

const router = useRouter()
const store = useUserStore()

// 拉最新身份并校验角色（后端接口另有拦截器兜底）
onMounted(async () => {
  try {
    const user = await request.get('/user/me')
    store.user = user
    localStorage.setItem('user', JSON.stringify(user))
    if (user.role !== 1) {
      ElMessage.error('非管理员账号')
      router.push('/')
    }
  } catch {
    router.push('/login')
  }
})
</script>

<style scoped>
.admin-layout { min-height: 100vh; }
.aside { background: #fff; border-right: 1px solid #f6e2ea; }
.logo {
  height: 60px; line-height: 60px; text-align: center;
  font-weight: 600; color: #c94d7e; font-size: 15px;
  border-bottom: 1px solid #f6e2ea;
}
.el-menu { border-right: none; }
.header {
  display: flex; align-items: center; gap: 12px;
  background: #fff; border-bottom: 1px solid #f6e2ea;
}
.name { color: #6b5b62; font-size: 14px; margin-right: auto; }
.main { background: #fdf3f7; }
</style>
