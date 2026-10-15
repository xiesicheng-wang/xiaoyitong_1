<template>
  <el-card>
    <template #header>
      <div class="head">
        <span>订单管理</span>
        <el-radio-group v-model="status" @change="load(1)">
          <el-radio-button :value="undefined">全部</el-radio-button>
          <el-radio-button :value="0">进行中</el-radio-button>
          <el-radio-button :value="1">已完成</el-radio-button>
          <el-radio-button :value="2">已取消</el-radio-button>
          <el-radio-button :value="3">违约</el-radio-button>
        </el-radio-group>
      </div>
    </template>
    <el-table :data="records" stripe>
      <el-table-column prop="orderNo" label="单号" width="195" />
      <el-table-column prop="productTitle" label="商品/服务" min-width="150" />
      <el-table-column label="买家 → 卖家" min-width="150">
        <template #default="{ row }">{{ row.buyerUsername }} → {{ row.sellerUsername }}</template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="85" />
      <el-table-column prop="penalty" label="违约金" width="85" />
      <el-table-column label="状态" width="105">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.tag" size="small">{{ statusMap[row.status]?.text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deadline" label="履约截止" width="165" />
    </el-table>
    <div class="pager">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="size"
                     :current-page="page" @current-change="load" />
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '../../api/request'

const records = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const status = ref(undefined)

const statusMap = {
  0: { text: '进行中(冻结)', tag: 'warning' },
  1: { text: '已完成', tag: 'success' },
  2: { text: '已取消', tag: 'info' },
  3: { text: '违约关闭', tag: 'danger' }
}

async function load(p = 1) {
  page.value = p
  const data = await request.get('/admin/orders', { params: { page, size, status: status.value } })
  records.value = data.records
  total.value = Number(data.total)
}
onMounted(load)
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: center; }
.pager { display: flex; justify-content: center; margin-top: 12px; }
</style>
