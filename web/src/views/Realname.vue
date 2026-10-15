<template>
  <div class="page">
    <el-card class="card">
      <template #header>实名认证</template>

      <!-- 已通过 -->
      <el-result v-if="info && info.status === 2" icon="success" title="已通过实名认证"
                 :sub-title="`${info.realName}（${info.idCardMasked}）`" />

      <!-- 待审核 -->
      <el-result v-else-if="info && info.status === 1" icon="info" title="实名申请审核中"
                 sub-title="管理员会在 1-2 个工作日内完成审核" />

      <!-- 已驳回：显示原因并允许重新提交 -->
      <el-alert v-if="info && info.status === 3" type="error" :closable="false"
                :title="`上次申请被驳回：${info.rejectReason}`" style="margin-bottom: 16px" />

      <!-- 表单（未实名 / 被驳回时可提交） -->
      <el-form v-if="!info || info.status === 0 || info.status === 3 || !info.status" :model="form" label-width="90px">
        <el-form-item label="真实姓名" required>
          <el-input v-model="form.realName" maxlength="32" placeholder="与身份证一致" />
        </el-form-item>
        <el-form-item label="身份证号" required>
          <el-input v-model="form.idCard" maxlength="18" placeholder="18 位，仅用于核验，不存储原文" />
        </el-form-item>
        <el-form-item label="证件照" required>
          <el-upload
            :http-request="doUpload"
            :show-file-list="false"
            accept="image/jpeg,image/png,image/webp"
            :limit="1">
            <el-button type="primary" plain>上传证件照</el-button>
          </el-upload>
          <el-image v-if="form.photoUrl" :src="form.photoUrl" fit="contain"
                    style="width: 220px; height: 140px; margin-top: 8px; border: 1px solid #eee" />
          <div class="tip">照片仅管理员可见，用于人工复核</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">提交认证</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const info = ref(null)
const loading = ref(false)
const form = reactive({ realName: '', idCard: '', photoUrl: '' })

async function load() {
  info.value = await request.get('/realname/me')
}

async function doUpload({ file, onSuccess, onError }) {
  try {
    const fd = new FormData()
    fd.append('file', file)
    const data = await request.post('/files/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.photoUrl = data.url
    onSuccess(data)
  } catch (e) {
    onError(e)
  }
}

async function onSubmit() {
  if (!form.realName || !form.idCard) {
    ElMessage.warning('请填写姓名和身份证号')
    return
  }
  if (!/^1[1-5]\d{4}(19|20)\d{2}(0[1-9]|1[0-2])([0-2]\d|3[01])\d{3}[\dXx]$/.test(form.idCard)) {
    ElMessage.warning('身份证号格式不正确')
    return
  }
  if (!form.photoUrl) {
    ElMessage.warning('请上传证件照')
    return
  }
  loading.value = true
  try {
    info.value = await request.post('/realname/submit', {
      realName: form.realName,
      idCard: form.idCard,
      photoUrl: form.photoUrl
    })
    ElMessage.success('提交成功，等待审核')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 640px; margin: 24px auto; }
.tip { color: #909399; font-size: 12px; margin-top: 4px; }
</style>
