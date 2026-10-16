<template>
  <SiteHeader />
  <div class="page" v-if="skill">
    <div class="wrap">
    <el-button text @click="router.back()">← 返回</el-button>
    <el-card class="detail">
      <el-tag>{{ skill.category }}</el-tag>
      <h2 class="title">{{ skill.title }}</h2>
      <div class="price">¥{{ skill.price }} <span class="unit">/ {{ skill.priceUnit }}</span></div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="服务提供者">{{ skill.sellerName || '邻居' }}</el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ skill.viewCount }}</el-descriptions-item>
        <el-descriptions-item label="挂单时间">{{ skill.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">服务描述</el-divider>
      <p class="desc">{{ skill.description || '（未填写描述，下单前请与对方沟通确认）' }}</p>
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

onMounted(async () => {
  skill.value = await request.get(`/skills/${route.params.id}`)
})
</script>

<style scoped>
.page { max-width: 760px; margin: 20px auto; padding: 0 16px; }
.wrap { margin-top: 4px; }
.title { margin: 12px 0 8px; }
.price { color: #e0557f; font-size: 28px; font-weight: 600; margin-bottom: 16px; }
.unit { font-size: 13px; color: #909399; font-weight: 400; }
.desc { white-space: pre-wrap; color: #606266; }
.buy { margin-top: 20px; width: 100%; }
.tip { margin-top: 10px; color: #909399; font-size: 12px; }
</style>
