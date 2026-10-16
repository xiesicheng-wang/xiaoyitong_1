<template>
  <SiteHeader />
  <div class="page">
    <el-card>
      <template #header>
        <el-tabs v-model="tab" @tab-change="load">
          <el-tab-pane label="我买到的" name="bought" />
          <el-tab-pane label="我卖出的" name="sold" />
        </el-tabs>
      </template>

      <el-table :data="orders" stripe>
        <el-table-column prop="orderNo" label="单号" width="200" />
        <el-table-column prop="productTitle" label="商品" min-width="160" />
        <el-table-column prop="amount" label="金额" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusMeta[row.status]?.tag" size="small">{{ statusMeta[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="penalty" label="违约金" width="90" />
        <el-table-column prop="deadline" label="履约截止" width="170" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button v-if="tab === 'sold'" size="small" type="success" @click="complete(row)">确认完成</el-button>
              <el-button v-if="tab === 'bought'" size="small" type="danger" plain @click="cancel(row)">取消</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination layout="prev, pager, next" :total="total" :page-size="size"
                       :current-page="page" @current-change="load" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SiteHeader from '../components/SiteHeader.vue'
import request from '../api/request'

const tab = ref('bought')
const orders = ref([])
const total = ref(0)
const page = ref(1)
const size = 10

const statusMeta = {
  0: { text: '进行中(冻结)', tag: 'warning' },
  1: { text: '已完成', tag: 'success' },
  2: { text: '已取消', tag: 'info' },
  3: { text: '违约关闭', tag: 'danger' }
}

async function load(p = 1) {
  page.value = p
  const data = await request.get(`/orders/${tab.value}`, { params: { page, size } })
  orders.value = data.records
  total.value = Number(data.total)
}
async function complete(row) {
  await request.post(`/orders/${row.id}/complete`)
  ElMessage.success('已确认完成，货款已结算')
  load(page.value)
}
async function cancel(row) {
  await ElMessageBox.confirm('取消将全额退款给买家（你），确认取消？', '取消订单')
  await request.post(`/orders/${row.id}/cancel`)
  ElMessage.success('已取消并退款')
  load(page.value)
}
onMounted(load)
</script>

<style scoped>
.page { max-width: 1000px; margin: 24px auto; }
.pager { display: flex; justify-content: center; margin-top: 12px; }
</style>
