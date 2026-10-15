<template>
  <div>
    <el-row :gutter="16">
      <el-col v-for="s in cards" :key="s.label" :span="6">
        <el-card class="stat">
          <div class="label">{{ s.label }}</div>
          <div class="num">{{ s.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top: 16px">
      <template #header>平台资金池</template>
      <el-row :gutter="16">
        <el-col :span="12">
          <div class="label">用户余额总额</div>
          <div class="num in">¥{{ stats?.walletBalance ?? '0.00' }}</div>
        </el-col>
        <el-col :span="12">
          <div class="label">冻结中总额</div>
          <div class="num frozen">¥{{ stats?.walletFrozen ?? '0.00' }}</div>
        </el-col>
      </el-row>
      <div class="tip">冻结总额 = 进行中订单的预付款，交易完成划给卖家，违约按比例扣除</div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import request from '../../api/request'

const stats = ref(null)
const cards = computed(() => [
  { label: '注册用户', value: stats.value?.userCount ?? '-' },
  { label: '已实名用户', value: stats.value?.realnamePassed ?? '-' },
  { label: '在售商品', value: stats.value?.productOnSale ?? '-' },
  { label: '订单总数', value: stats.value?.orderTotal ?? '-' },
  { label: '进行中订单', value: stats.value?.orderProcessing ?? '-' },
  { label: '违约订单', value: stats.value?.orderOverdue ?? '-' }
])

onMounted(async () => {
  stats.value = await request.get('/admin/stats')
})
</script>

<style scoped>
.stat { text-align: center; }
.label { color: #909399; font-size: 13px; margin-bottom: 8px; }
.num { font-size: 30px; font-weight: 700; color: #c94d7e; }
.in { color: #5fb878; }
.frozen { color: #ba7517; }
.tip { margin-top: 16px; color: #909399; font-size: 12px; }
</style>
