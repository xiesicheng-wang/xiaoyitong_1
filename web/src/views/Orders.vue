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
        <el-table-column label="操作" width="190">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button v-if="tab === 'sold'" size="small" type="success" @click="complete(row)">确认完成</el-button>
              <el-button v-if="tab === 'bought'" size="small" type="danger" plain @click="cancel(row)">取消</el-button>
            </template>
            <el-button v-if="row.status === 1 && !reviewedIds.has(row.id)" size="small"
                       type="warning" plain @click="openReview(row)">评价</el-button>
            <el-tag v-if="row.status === 1 && reviewedIds.has(row.id)" size="small" type="info">已评价</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination layout="prev, pager, next" :total="total" :page-size="size"
                       :current-page="page" @current-change="load" />
      </div>
    </el-card>

    <!-- 评价对话框 -->
    <el-dialog v-model="reviewVisible" title="交易评价" width="420px">
      <el-rate v-model="reviewForm.score" :texts="['很差', '较差', '一般', '满意', '超赞']" show-text />
      <el-input v-model="reviewForm.content" type="textarea" :rows="3" maxlength="500"
                placeholder="说说这次交易体验（选填）" style="margin-top: 12px" />
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SiteHeader from '../components/SiteHeader.vue'
import request from '../api/request'

const tab = ref('bought')
const orders = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const reviewedIds = ref(new Set())
const reviewVisible = ref(false)
const reviewLoading = ref(false)
const reviewForm = reactive({ orderId: null, score: 5, content: '' })

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
  loadReviewed()
}

/* 已评价的订单 id（后端一次性返回） */
async function loadReviewed() {
  const ids = await request.get('/reviews/my-reviewed-ids')
  reviewedIds.value = new Set(ids)
}

function openReview(row) {
  reviewForm.orderId = row.id
  reviewForm.score = 5
  reviewForm.content = ''
  reviewVisible.value = true
}

async function submitReview() {
  reviewLoading.value = true
  try {
    await request.post('/reviews', {
      orderId: reviewForm.orderId,
      score: reviewForm.score,
      content: reviewForm.content
    })
    ElMessage.success('评价成功，感谢反馈')
    reviewVisible.value = false
    reviewedIds.value = new Set([...reviewedIds.value, reviewForm.orderId])
  } finally {
    reviewLoading.value = false
  }
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
