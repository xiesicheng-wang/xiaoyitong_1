<template>
  <div class="page">
    <SiteHeader />

    <el-main class="main">
      <div class="nearby-title">附近的人 <el-tag size="small">基于 Redis GEO</el-tag></div>
      <el-card>
        <div class="toolbar">
          <el-button type="primary" :loading="locating" @click="locateAndReport">
            {{ reported ? '刷新我的位置' : '打开定位并出现' }}
          </el-button>
          <el-select v-model="radiusKm" style="width: 130px" @change="loadNearby">
            <el-option :value="1" label="半径 1 km" />
            <el-option :value="2" label="半径 2 km" />
            <el-option :value="5" label="半径 5 km" />
          </el-select>
          <el-button v-if="reported" plain type="info" @click="leave">隐身（清除位置）</el-button>
        </div>
        <div class="tip">基于浏览器定位（可拒绝），仅上报经纬度，用于展示附近邻居；点「隐身」立即从列表消失</div>
      </el-card>

      <el-card style="margin-top: 12px">
        <template #header>附近邻居</template>
        <el-table v-if="list.length" :data="list" stripe>
          <el-table-column prop="nickname" label="昵称" min-width="140" />
          <el-table-column label="信誉分" width="100">
            <template #default="{ row }">
              <el-tag :type="row.creditScore >= 80 ? 'success' : 'warning'" size="small">{{ row.creditScore }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="距离" width="110">
            <template #default="{ row }">{{ row.distanceKm }} km</template>
          </el-table-column>
        </el-table>
        <el-empty v-else :description="reported ? '附近暂无其他人，等邻居上线吧' : '先打开定位，看看附近有谁'" />
      </el-card>
    </el-main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import SiteHeader from '../components/SiteHeader.vue'
import request from '../api/request'

const locating = ref(false)
const reported = ref(false)
const list = ref([])
const radiusKm = ref(2)

function locateAndReport() {
  if (!navigator.geolocation) {
    ElMessage.error('浏览器不支持定位')
    return
  }
  locating.value = true
  navigator.geolocation.getCurrentPosition(async (pos) => {
    try {
      await request.post('/nearby/report', {
        lng: pos.coords.longitude,
        lat: pos.coords.latitude
      })
      reported.value = true
      ElMessage.success('定位成功')
      await loadNearby()
    } finally {
      locating.value = false
    }
  }, (err) => {
    locating.value = false
    ElMessage.warning(err.code === 1 ? '你拒绝了定位授权，无法展示附近的人' : '定位失败，请稍后再试')
  })
}

async function loadNearby() {
  if (!reported.value) return
  list.value = await request.get('/nearby', { params: { radiusKm: radiusKm.value } })
}

async function leave() {
  await request.post('/nearby/leave')
  reported.value = false
  list.value = []
  ElMessage.success('已隐身')
}
</script>

<style scoped>
.page { min-height: 100vh; }
.main { max-width: 760px; margin: 12px auto; padding: 0 16px; }
.nearby-title { font-weight: 600; margin-bottom: 10px; }
.toolbar { display: flex; gap: 12px; align-items: center; }
.tip { margin-top: 10px; color: #909399; font-size: 12px; }
</style>
