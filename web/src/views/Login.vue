<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2 class="title">友邻帮</h2>
      <p class="subtitle">社区互助服务平台</p>
      <el-form :model="form" label-position="top" @keyup.enter="onSubmit">
        <el-form-item label="用户名 / 手机号">
          <el-input v-model="form.username" placeholder="请输入用户名或手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">登 录</el-button>
        <div class="footer-link">
          还没有账号？<router-link to="/register">去注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function onSubmit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await store.login(form)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #fceaf1 0%, #fdf3f7 45%, #fef8fb 100%);
}
.auth-card {
  width: 380px;
  padding: 12px 8px;
}
.title {
  text-align: center;
  margin: 8px 0 0;
  color: #c94d7e;
  letter-spacing: 2px;
}
.subtitle {
  text-align: center;
  color: #909399;
  margin: 4px 0 16px;
  font-size: 13px;
}
.submit {
  width: 100%;
}
.footer-link {
  margin-top: 14px;
  text-align: center;
  font-size: 13px;
}
</style>
