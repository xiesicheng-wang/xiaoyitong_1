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
        <el-button text @click="router.push('/realname')">实名认证</el-button>
        <el-button text @click="router.push('/me')">我的</el-button>
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

onMounted(load)
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
.brand { font-size: 20px; font-weight: 600; }
.search { flex: 1; max-width: 420px; }
.right { margin-left: auto; display: flex; }
.categories { margin-bottom: 16px; }
.cat { cursor: pointer; margin-right: 8px; }
.card-col { margin-bottom: 16px; }
.product-card { cursor: pointer; }
.cover { width: 100%; height: 160px; display: block; }
.cover-fallback {
  height: 160px; display: flex; align-items: center; justify-content: center;
  background: #f0f2f5; color: #909399; font-size: 13px;
}
.pt { margin-top: 10px; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pb { display: flex; justify-content: space-between; align-items: center; margin-top: 6px; }
.price { color: #e24b4a; font-weight: 600; }
.meta { color: #909399; font-size: 12px; }
.pager { display: flex; justify-content: center; margin-top: 8px; }
</style>
