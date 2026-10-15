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
          <el-button v-if="canBuy" type="danger" size="large" class="buy" :loading="buying" @click="onBuy">
            立即下单（冻结 ¥{{ product.price }}）
          </el-button>
          <div v-if="canBuy" class="buy-tip">下单即预约服务，款项预冻结；完成交易后结算给卖家，超时未完成将扣除违约金</div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const product = ref(null)
const buying = ref(false)

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

onMounted(async () => {
  product.value = await request.get(`/products/${route.params.id}`)
})
</script>

<style scoped>
.page { max-width: 1000px; margin: 24px auto; }
.title { margin: 0 0 8px; }
.price { color: #e0557f; font-size: 28px; font-weight: 600; margin-bottom: 16px; }
.desc { margin-bottom: 8px; }
.desc-text { white-space: pre-wrap; color: #606266; }
</style>
