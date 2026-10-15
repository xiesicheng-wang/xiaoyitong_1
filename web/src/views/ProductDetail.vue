<template>
  <div class="page" v-if="product">
    <el-button text @click="router.back()">← 返回</el-button>
    <el-card class="detail">
      <el-row :gutter="24">
        <el-col :span="12">
          <el-carousel v-if="product.images.length" height="360px" :autoplay="false" indicator-position="outside">
            <el-carousel-item v-for="img in product.images" :key="img">
              <el-image :src="img" fit="contain" style="width: 100%; height: 100%" />
            </el-carousel-item>
          </el-carousel>
          <el-empty v-else description="暂无图片" />
        </el-col>
        <el-col :span="12">
          <h2 class="title">{{ product.title }}</h2>
          <div class="price">¥{{ product.price }}</div>
          <el-descriptions :column="1" border class="desc">
            <el-descriptions-item label="分类">{{ product.category }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="['', 'info', 'warning'][product.status] || 'success'">
                {{ ['', '已下架', '已售出'][product.status] || '在售' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="浏览量">{{ product.viewCount }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ product.createTime }}</el-descriptions-item>
          </el-descriptions>
          <el-divider content-position="left">商品描述</el-divider>
          <p class="desc-text">{{ product.description || '（卖家很懒，没有写描述）' }}</p>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../api/request'

const route = useRoute()
const router = useRouter()
const product = ref(null)

onMounted(async () => {
  product.value = await request.get(`/products/${route.params.id}`)
})
</script>

<style scoped>
.page { max-width: 1000px; margin: 24px auto; }
.title { margin: 0 0 8px; }
.price { color: #e24b4a; font-size: 28px; font-weight: 600; margin-bottom: 16px; }
.desc { margin-bottom: 8px; }
.desc-text { white-space: pre-wrap; color: #606266; }
</style>
