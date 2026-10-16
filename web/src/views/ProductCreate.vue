<template>
  <SiteHeader />
  <div class="page">
    <el-card class="form-card">
      <template #header>发布二手商品</template>
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="64" show-word-limit placeholder="一句话描述你的宝贝" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" />
          <span class="unit">元</span>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="2000" show-word-limit
                    placeholder="成色、购买时间、交易方式等" />
        </el-form-item>
        <el-form-item label="图片（最多9张）">
          <el-upload
            v-model:file-list="fileList"
            list-type="picture-card"
            :http-request="doUpload"
            :on-remove="syncUrls"
            accept="image/jpeg,image/png,image/webp,image/gif"
            :limit="9">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">发布</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import SiteHeader from '../components/SiteHeader.vue'
import request from '../api/request'

const router = useRouter()
const categories = ['数码', '图书', '家居', '服饰', '运动', '其他']
const loading = ref(false)
const form = reactive({ title: '', category: '其他', price: null, description: '' })
const fileList = ref([])

// 自定义上传：把文件 POST 到后端，后端转存 MinIO，返回 URL
async function doUpload({ file, onSuccess, onError }) {
  try {
    const fd = new FormData()
    fd.append('file', file)
    const data = await request.post('/files/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    // 把 MinIO URL 挂到 file 对象上，提交时收集
    file.minioUrl = data.url
    onSuccess(data)
  } catch (e) {
    onError(e)
  }
}

function syncUrls() {
  // v-model:file-list 已同步；urls 在提交时统一收集
}

async function onSubmit() {
  if (!form.title) { ElMessage.warning('请填写标题'); return }
  if (!form.price) { ElMessage.warning('请填写价格'); return }
  const images = fileList.value
    .map(f => f.raw?.minioUrl || f.minioUrl)
    .filter(Boolean)
  loading.value = true
  try {
    await request.post('/products', { ...form, images })
    ElMessage.success('发布成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { max-width: 720px; margin: 24px auto; }
.unit { margin-left: 8px; color: #909399; }
</style>
