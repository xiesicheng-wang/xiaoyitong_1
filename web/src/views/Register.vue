<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2 class="title">注册账号</h2>
      <p class="subtitle">加入友邻帮，让闲置流转、技能互助</p>
      <el-form :model="form" label-position="top" @keyup.enter="onSubmit">
        <el-form-item label="用户名（4-32 位）">
          <el-input v-model="form.username" placeholder="登录用的账号" />
        </el-form-item>
        <el-form-item label="密码（6-32 位）">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirm" type="password" show-password placeholder="再输入一次" />
        </el-form-item>
        <el-form-item label="手机号（11 位，作为唯一识别键）">
          <el-input v-model="form.phone" placeholder="用于登录和社区联系" maxlength="11" />
        </el-form-item>
        <el-form-item label="昵称（选填，默认同用户名）">
          <el-input v-model="form.nickname" placeholder="别人看到的名字" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">注 册</el-button>
        <div class="footer-link">
          已有账号？<router-link to="/login">去登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '', confirm: '', phone: '', nickname: '' })

async function onSubmit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的 11 位手机号')
    return
  }
  if (form.password !== form.confirm) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  loading.value = true
  try {
    await request.post('/auth/register', {
      username: form.username,
      password: form.password,
      phone: form.phone,
      nickname: form.nickname || undefined
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
