<template>
  <div class="page">
    <el-header class="header">
      <div class="brand">友邻帮</div>
      <div class="search">
        <el-input v-model="keyword" placeholder="搜索二手好物" clearable @keyup.enter="load(1)">
          <template #append>
            <el-button @click="load(1)">搜索</el-button>
          </template>
        </el-input>
      </div>
      <div class="right">
        <el-button type="primary" @click="router.push('/products/new')">发布商品</el-button>
        <el-button text type="warning" @click="router.push('/skills')">技能服务</el-button>
        <el-button text @click="router.push('/chat')">群聊</el-button>
        <el-button text @click="router.push('/nearby')">附近</el-button>
        <el-popover placement="bottom" :width="340" trigger="click" @show="loadNoti">
          <template #reference>
            <el-badge :value="unread" :hidden="unread === 0" :max="99">
              <el-button text>通知</el-button>
            </el-badge>
          </template>
          <div v-if="notiList.length">
            <div v-for="n in notiList" :key="n.id" class="noti-item">
              <div class="noti-title">{{ n.title }}</div>
              <div class="noti-content">{{ n.content }}</div>
            </div>
            <el-button size="small" text type="primary" style="width: 100%" @click="readAll">全部已读</el-button>
          </div>
          <el-empty v-else description="暂无通知" :image-size="50" />
        </el-popover>
        <el-button text @click="router.push('/me')">我的</el-button>
        <el-button v-if="store.user?.role === 1" text type="warning" @click="router.push('/admin')">管理后台</el-button>
        <el-button text type="danger" @click="onLogout">退出</el-button>
      </div>
    </el-header>

    <el-main class="main">
      <div class="categories">
        <el-tag
          v-for="c in categories" :key="c"
          :effect="category === c ? 'dark' : 'plain'"
          class="cat" @click="pickCategory(c)">{{ c }}</el-tag>
      </div>

      <el-row v-if="products.length" :gutter="16">
        <el-col v-for="p in products" :key="p.id" :span="6" class="card-col">
          <el-card shadow="hover" class="product-card" @click="router.push(`/products/${p.id}`)">
            <el-image :src="p.images[0]" fit="cover" class="cover">
              <template #error>
                <div class="cover-fallback">暂无图片</div>
              </template>
            </el-image>
            <div class="pt">{{ p.title }}</div>
            <div class="pb">
              <span class="price">¥{{ p.price }}</span>
              <span class="meta">{{ p.category }} · {{ p.viewCount }} 浏览</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="没有找到商品" />

      <div class="pager">
        <el-pagination
          layout="prev, pager, next" :total="total" :page-size="size"
          :current-page="page" @current-change="load" />
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()

const categories = ['全部', '数码', '图书', '家居', '服饰', '运动', '其他']
const keyword = ref('')
const category = ref('')
const products = ref([])
const total = ref(0)
const page = ref(1)
const size = 12

async function load(p = page.value) {
  page.value = p
  const data = await request.get('/products', {
    params: {
      page, size,
      keyword: keyword.value || undefined,
      category: category.value && category.value !== '全部' ? category.value : undefined
    }
  })
  products.value = data.records
  total.value = Number(data.total)
}

function pickCategory(c) {
  category.value = c
  load(1)
}

function onLogout() {
  store.logout()
  router.push('/login')
}

/* 通知：未读数 + 最近列表 */
const unread = ref(0)
const notiList = ref([])

async function loadNoti() {
  const data = await request.get('/notifications', { params: { page: 1, size: 6 } })
  notiList.value = data.records
  unread.value = Number((await request.get('/notifications/unread-count')).count)
}

async function readAll() {
  await request.post('/notifications/read-all')
  unread.value = 0
  await loadNoti()
}

onMounted(() => { load(); loadNoti() })
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #fff;
  border-radius: 8px;
  margin-top: 16px;
  padding: 0 24px;
  height: 64px;
}
.brand { font-size: 20px; font-weight: 600; color: #c94d7e; letter-spacing: 1px; }
.search { flex: 1; max-width: 420px; }
.right { margin-left: auto; display: flex; }
.categories { margin-bottom: 16px; }
.cat { cursor: pointer; margin-right: 8px; }
.card-col { margin-bottom: 16px; }
.product-card { cursor: pointer; transition: transform 0.2s; }
.product-card:hover { transform: translateY(-3px); }
.cover { width: 100%; height: 160px; display: block; }
.cover-fallback {
  height: 160px; display: flex; align-items: center; justify-content: center;
  background: #f0f2f5; color: #909399; font-size: 13px;
}
.pt { margin-top: 10px; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pb { display: flex; justify-content: space-between; align-items: center; margin-top: 6px; }
.price { color: #e0557f; font-weight: 600; }
.meta { color: #909399; font-size: 12px; }
.pager { display: flex; justify-content: center; margin-top: 8px; }
.noti-item { padding: 6px 0; border-bottom: 1px solid #f6e2ea; }
.noti-title { font-size: 13px; font-weight: 600; }
.noti-content { font-size: 12px; color: #909399; margin-top: 2px; }
</style>
