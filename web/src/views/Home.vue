<template>
  <div class="page">
    <SiteHeader />

    <el-main class="main">
      <div class="center-title">个人中心</div>
      <!-- 个人信息卡 -->
      <el-card class="profile">
        <div class="avatar">{{ (store.user?.nickname || store.user?.username || '?').slice(0, 1) }}</div>
        <div class="info">
          <div class="name">{{ store.user?.nickname || store.user?.username }}
            <el-tag v-if="store.user?.role === 1" size="small" type="danger" effect="dark">管理员</el-tag>
          </div>
          <div class="sub">@{{ store.user?.username }}
            <span v-if="store.user?.phone"> · {{ store.user.phone.slice(0, 3) }}****{{ store.user.phone.slice(7) }}</span>
          </div>
          <div class="tags">
            <el-tag size="small" :type="realnameTag">实名：{{ realnameText }}</el-tag>
            <el-tag size="small" :type="creditTag">信誉分 {{ store.user?.creditScore ?? 100 }}</el-tag>
          </div>
        </div>
        <div class="quick">
          <el-button @click="router.push('/realname')">实名认证</el-button>
          <el-button @click="router.push('/orders')">我的订单</el-button>
          <el-button @click="router.push('/wallet')">钱包</el-button>
        </div>
      </el-card>

      <!-- 我的技能挂单（可上下架） -->
      <el-card style="margin-top: 14px">
        <template #header>
          <div class="card-head">
            <span>我的技能挂单</span>
            <el-button size="small" type="primary" plain @click="router.push('/skills/new')">+ 挂新技能</el-button>
          </div>
        </template>
        <el-table v-if="skills.length" :data="skills" stripe size="small">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column label="价格" width="110">
            <template #default="{ row }">¥{{ row.price }} / {{ row.priceUnit }}</template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览" width="70" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 0 ? 'success' : 'info'">
                {{ row.status === 0 ? '接单中' : '已下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110">
            <template #default="{ row }">
              <el-button size="small" text :type="row.status === 0 ? 'danger' : 'success'"
                         @click="toggle(row)">
                {{ row.status === 0 ? '停止接单' : '重新接单' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="还没有挂过技能，去挂一个赚零花钱吧" :image-size="60" />
      </el-card>

      <!-- 我的收藏 -->
      <el-card style="margin-top: 14px">
        <template #header>我的收藏</template>
        <el-table v-if="favorites.length" :data="favorites" stripe size="small">
          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.targetType === 'PRODUCT' ? 'success' : 'warning'">
                {{ row.targetType === 'PRODUCT' ? '商品' : '技能' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column label="价格" width="120">
            <template #default="{ row }">
              ¥{{ row.price }}<span v-if="row.priceUnit"> / {{ row.priceUnit }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" text type="primary"
                         @click="router.push(row.targetType === 'PRODUCT' ? `/products/${row.targetId}` : `/skills/${row.targetId}`)">
                去看看
              </el-button>
              <el-button size="small" text type="danger" @click="removeFav(row)">取消收藏</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="还没有收藏，去逛逛吧" :image-size="60" />
      </el-card>

      <!-- 我的商品 -->
      <el-card style="margin-top: 14px">
        <template #header>
          <div class="card-head">
            <span>我的在售商品</span>
            <el-button size="small" type="primary" plain @click="router.push('/products/new')">+ 发布商品</el-button>
          </div>
        </template>
        <el-table v-if="products.length" :data="products" stripe size="small">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="price" label="价格" width="90" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 0 ? 'success' : 'info'">
                {{ ['', '已下架', '已售出'][row.status] || '在售' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="还没有发布过商品" :image-size="60" />
      </el-card>
    </el-main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SiteHeader from '../components/SiteHeader.vue'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()
const skills = ref([])
const products = ref([])
const favorites = ref([])

const realnameText = computed(() =>
  ({ 0: '未实名', 1: '待审核', 2: '已实名' }[store.user?.realnameStatus] || '未实名'))
const realnameTag = computed(() =>
  ({ 0: 'info', 1: 'warning', 2: 'success' }[store.user?.realnameStatus] || 'info'))
const creditTag = computed(() =>
  (store.user?.creditScore ?? 100) >= 80 ? 'success'
    : (store.user?.creditScore ?? 100) >= 60 ? 'warning' : 'danger')

async function loadMine() {
  const s = await request.get('/skills/mine', { params: { page: 1, size: 20 } })
  skills.value = s.records
  const p = await request.get('/products', { params: { page: 1, size: 50 } })
  products.value = p.records.filter(x => x.userId === store.user?.id)
  favorites.value = await request.get('/favorites/my')
}

async function removeFav(row) {
  await request.post(`/favorites/${row.targetType}/${row.targetId}/toggle`)
  ElMessage.success('已取消收藏')
  loadMine()
}

async function toggle(row) {
  await request.post(`/skills/${row.id}/toggle`)
  ElMessage.success('操作成功')
  loadMine()
}

onMounted(loadMine)
</script>

<style scoped>
.page { min-height: 100vh; }
.main { max-width: 860px; margin: 12px auto; padding: 0 16px; }
.center-title { font-weight: 600; margin-bottom: 10px; }
.profile { display: flex; align-items: center; gap: 20px; }
.avatar {
  width: 64px; height: 64px; border-radius: 50%;
  background: linear-gradient(135deg, #ee7bab, #f4b3cb);
  color: #fff; font-size: 28px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.name { font-size: 18px; font-weight: 600; display: flex; gap: 8px; align-items: center; }
.sub { color: #909399; font-size: 13px; margin: 4px 0 8px; }
.tags { display: flex; gap: 8px; }
.quick { margin-left: auto; display: flex; flex-direction: column; gap: 8px; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
</style>
