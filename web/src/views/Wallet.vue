<template>
  <div class="page">
    <el-card class="balance-card">
      <div class="balance-row">
        <div>
          <div class="label">可用余额（元）</div>
          <div class="num">{{ wallet?.balance ?? '0.00' }}</div>
        </div>
        <div>
          <div class="label">冻结中（元）</div>
          <div class="num frozen">{{ wallet?.frozen ?? '0.00' }}</div>
        </div>
        <el-button type="primary" @click="rechargeVisible = true">充值（模拟）</el-button>
      </div>
      <div class="hint">冻结金额 = 进行中订单的预付款，完成结算给卖家，违约按比例扣除</div>
    </el-card>

    <el-card>
      <template #header>钱包流水</template>
      <el-table :data="logs" stripe>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">{{ typeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="110">
          <template #default="{ row }">
            <span :class="incomeTypes.includes(row.type) ? 'in' : 'out'">
              {{ incomeTypes.includes(row.type) ? '+' : '-' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="变动后余额" width="120" />
        <el-table-column prop="frozenAfter" label="变动后冻结" width="120" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
      <div class="pager">
        <el-pagination layout="prev, pager, next" :total="total" :page-size="size"
                       :current-page="page" @current-change="loadLogs" />
      </div>
    </el-card>

    <el-dialog v-model="rechargeVisible" title="充值（模拟支付）" width="360px">
      <el-input-number v-model="rechargeAmount" :min="0.01" :precision="2" :step="50" style="width: 100%" />
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="doRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const wallet = ref(null)
const logs = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const rechargeVisible = ref(false)
const rechargeAmount = ref(100)
const loading = ref(false)

const incomeTypes = ['RECHARGE', 'REFUND', 'PENALTY']
const typeNames = { RECHARGE: '充值', FREEZE: '冻结', SETTLE: '结算', REFUND: '退款', PENALTY: '违约金' }
function typeName(t) { return typeNames[t] || t }
function typeTag(t) { return incomeTypes.includes(t) ? 'success' : 'warning' }

async function loadWallet() {
  wallet.value = await request.get('/wallet')
}
async function loadLogs(p = 1) {
  page.value = p
  const data = await request.get('/wallet/logs', { params: { page, size } })
  logs.value = data.records
  total.value = Number(data.total)
}
async function doRecharge() {
  loading.value = true
  try {
    await request.post('/wallet/recharge', { amount: rechargeAmount.value })
    ElMessage.success('充值成功')
    rechargeVisible.value = false
    await Promise.all([loadWallet(), loadLogs(1)])
  } finally {
    loading.value = false
  }
}
onMounted(() => { loadWallet(); loadLogs() })
</script>

<style scoped>
.page { max-width: 900px; margin: 24px auto; display: flex; flex-direction: column; gap: 16px; }
.balance-row { display: flex; align-items: center; gap: 60px; }
.label { color: #909399; font-size: 13px; }
.num { font-size: 28px; font-weight: 600; }
.frozen { color: #ba7517; }
.hint { margin-top: 14px; color: #909399; font-size: 12px; }
.in { color: #639922; font-weight: 600; }
.out { color: #e24b4a; font-weight: 600; }
.pager { display: flex; justify-content: center; margin-top: 12px; }
</style>
