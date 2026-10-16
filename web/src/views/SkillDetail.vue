<template>
  <SiteHeader />
  <div class="page" v-if="skill">
    <div class="wrap">
    <el-button text @click="router.back()">← 返回</el-button>
    <el-card class="detail">
      <el-tag>{{ skill.category }}</el-tag>
      <h2 class="title">{{ skill.title }}</h2>
      <div class="price-row">
        <span class="price">¥{{ skill.price }} <span class="unit">/ {{ skill.priceUnit }}</span></span>
        <el-button text :type="favorited ? 'warning' : 'default'" @click="toggleFav">
          {{ favorited ? '★ 已收藏' : '☆ 收藏' }}
        </el-button>
      </div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="服务提供者">{{ skill.sellerName || '邻居' }}</el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ skill.viewCount }}</el-descriptions-item>
        <el-descriptions-item label="挂单时间">{{ skill.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">服务描述</el-divider>
      <p class="desc">{{ skill.description || '（未填写描述，下单前请与对方沟通确认）' }}</p>
      <el-divider content-position="left">服务评价（{{ reviews.length }}）</el-divider>
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
      <el-button v-if="canBuy" type="danger" size="large" class="buy" :loading="buying" @click="onOrder">
        预约服务（冻结 ¥{{ skill.price }}）
      </el-button>
      <div v-if="canBuy" class="tip">预约即预冻结全款；服务完成后由对方确认结算，超时未完成将扣除违约金</div>
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
const skill = ref(null)
const buying = ref(false)
const favorited = ref(false)
const reviews = ref([])

const canBuy = computed(() => skill.value && store.user && skill.value.userId !== store.user.id)

async function onOrder() {
  await ElMessageBox.confirm(
    `预约将预冻结 ¥${skill.value.price}，超时未完成将扣除 20% 违约金，确认预约？`,
    '确认预约', { type: 'warning' }
  )
  buying.value = true
  try {
    await request.post('/orders/skill', { skillId: skill.value.id })
    ElMessage.success('预约成功，款项已冻结')
    router.push('/orders')
  } finally {
    buying.value = false
  }
}

async function toggleFav() {
  const data = await request.post(`/favorites/SKILL/${skill.value.id}/toggle`)
  favorited.value = data.favorited
  ElMessage.success(data.favorited ? '已收藏' : '已取消收藏')
}

onMounted(async () => {
  skill.value = await request.get(`/skills/${route.params.id}`)
  if (store.token) {
    try {
      const fav = await request.get(`/favorites/SKILL/${route.params.id}/status`)
      favorited.value = fav.favorited
    } catch { /* 忽略 */ }
  }
  reviews.value = await request.get('/reviews', {
    params: { targetType: 'SKILL', targetId: route.params.id }
  })
})
</script>

<style scoped>
.page { max-width: 760px; margin: 20px auto; padding: 0 16px; }
.wrap { margin-top: 4px; }
.title { margin: 12px 0 8px; }
.price-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.price { color: #e0557f; font-size: 28px; font-weight: 600; }
.unit { font-size: 13px; color: #909399; font-weight: 400; }
.review-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 8px; }
.review-item { background: #fdf3f7; border-radius: 8px; padding: 10px 12px; }
.review-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 4px; }
.review-name { font-size: 13px; font-weight: 600; color: #c94d7e; }
.review-content { font-size: 13px; color: #606266; }
.unit { font-size: 13px; color: #909399; font-weight: 400; }
.desc { white-space: pre-wrap; color: #606266; }
.buy { margin-top: 20px; width: 100%; }
.tip { margin-top: 10px; color: #909399; font-size: 12px; }
</style>
