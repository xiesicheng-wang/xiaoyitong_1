<template>
  <SiteHeader />
  <div class="page" v-if="product">
    <div class="wrap">
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
          <div class="price-row">
            <span class="price">¥{{ product.price }}</span>
            <el-button text :type="favorited ? 'warning' : 'default'" @click="toggleFav">
              {{ favorited ? '★ 已收藏' : '☆ 收藏' }}
            </el-button>
          </div>
          <el-descriptions :column="1" border class="desc">
            <el-descriptions-item label="分类">{{ product.category }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="['', 'info', 'warning'][product.status] || 'success'">
                {{ ['', '已下架', '已售出'][product.status] || '在售' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="卖家">{{ product.sellerName || '邻居' }}</el-descriptions-item>
            <el-descriptions-item label="浏览量">{{ product.viewCount }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ product.createTime }}</el-descriptions-item>
          </el-descriptions>
          <el-divider content-position="left">商品描述</el-divider>
          <p class="desc-text">{{ product.description || '（卖家很懒，没有写描述）' }}</p>
          <el-divider content-position="left">交易评价（{{ reviews.length }}）</el-divider>
          <div v-if="reviews.length" class="review-list">
            <div v-for="rv in reviews" :key="rv.id" class="review-item">
              <div class="review-head">
                <span class="review-name">{{ rv.reviewerName }}</span>
                <el-rate :model-value="rv.score" disabled size="small" />
              </div>
              <div class="review-content">{{ rv.content || '（未写评价）' }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无评价" :image-size="50" />
          <el-button v-if="canBuy" type="danger" size="large" class="buy" :loading="buying" @click="onBuy">
            立即下单（冻结 ¥{{ product.price }}）
          </el-button>
          <div v-if="canBuy" class="buy-tip">下单即预约服务，款项预冻结；完成交易后结算给卖家，超时未完成将扣除违约金</div>
        </el-col>
      </el-row>
    </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SiteHeader from '../components/SiteHeader.vue'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const product = ref(null)
const buying = ref(false)
const favorited = ref(false)
const reviews = ref([])

// 自己的商品不能买
const canBuy = computed(() => product.value && store.user && product.value.userId !== store.user.id)

async function onBuy() {
  await ElMessageBox.confirm(
    `下单将预冻结 ¥${product.value.price}，若超时未完成交易将扣除 ${20}% 违约金，确认下单？`,
    '确认下单', { type: 'warning' }
  )
  buying.value = true
  try {
    await request.post('/orders', { productId: product.value.id })
    ElMessage.success('下单成功，款项已冻结')
    router.push('/orders')
  } finally {
    buying.value = false
  }
}

async function toggleFav() {
  const data = await request.post(`/favorites/PRODUCT/${product.value.id}/toggle`)
  favorited.value = data.favorited
  ElMessage.success(data.favorited ? '已收藏' : '已取消收藏')
}

onMounted(async () => {
  product.value = await request.get(`/products/${route.params.id}`)
  // 收藏状态与评价列表（未登录静默跳过）
  if (store.token) {
    try {
      const fav = await request.get(`/favorites/PRODUCT/${route.params.id}/status`)
      favorited.value = fav.favorited
    } catch { /* 忽略 */ }
  }
  reviews.value = await request.get('/reviews', {
    params: { targetType: 'PRODUCT', targetId: route.params.id }
  })
})
</script>

<style scoped>
.page { max-width: 1000px; margin: 20px auto; padding: 0 16px; }
.wrap { margin-top: 4px; }
.title { margin: 0 0 8px; }
.price-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.price { color: #e0557f; font-size: 28px; font-weight: 600; }
.review-list { display: flex; flex-direction: column; gap: 10px; }
.review-item { background: #fdf3f7; border-radius: 8px; padding: 10px 12px; }
.review-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 4px; }
.review-name { font-size: 13px; font-weight: 600; color: #c94d7e; }
.review-content { font-size: 13px; color: #606266; }
.desc { margin-bottom: 8px; }
.desc-text { white-space: pre-wrap; color: #606266; }
</style>
