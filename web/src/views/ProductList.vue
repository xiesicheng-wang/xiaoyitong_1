<template>
  <div class="page">
    <SiteHeader />

    <el-main class="main">
      <!-- 欢迎横幅 -->
      <div class="hero">
        <div class="hero-text">
          <div class="hero-title">把闲置流转起来，让技能照亮邻里</div>
          <div class="hero-sub">二手好物 · 技能服务 · 实名可信 · 资金保障</div>
        </div>
        <el-button size="large" round type="primary" @click="router.push('/products/new')">+ 发布闲置</el-button>
      </div>

      <!-- 搜索 + 分类 -->
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索二手好物" clearable class="search"
                  @keyup.enter="load(1)">
          <template #append><el-button @click="load(1)">搜索</el-button></template>
        </el-input>
        <div class="cats">
          <el-tag v-for="c in categories" :key="c" :effect="category === c ? 'dark' : 'plain'"
                  class="cat" @click="pick(c)">{{ c }}</el-tag>
        </div>
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
              <span class="meta">{{ p.sellerName || '邻居' }} · {{ p.viewCount }} 浏览</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="没有找到商品" />

      <div class="pager">
        <el-pagination layout="prev, pager, next" :total="total" :page-size="size"
                       :current-page="page" @current-change="load" />
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SiteHeader from '../components/SiteHeader.vue'
import request from '../api/request'

const router = useRouter()

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

function pick(c) {
  category.value = c
  load(1)
}

onMounted(load)
</script>

<style scoped>
.main { max-width: 1180px; margin: 0 auto; padding: 16px 20px 40px; }
.hero {
  background: linear-gradient(120deg, #fce3ee 0%, #fbdde8 50%, #fdeef4 100%);
  border-radius: 16px; padding: 28px 34px; margin-bottom: 18px;
  display: flex; align-items: center; justify-content: space-between;
}
.hero-title { font-size: 22px; font-weight: 700; color: #8e3a5c; }
.hero-sub { color: #b97f97; font-size: 13px; margin-top: 8px; letter-spacing: 2px; }
.toolbar { display: flex; gap: 14px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
.search { width: 320px; }
.cats { display: flex; gap: 6px; flex-wrap: wrap; }
.cat { cursor: pointer; }
.card-col { margin-bottom: 16px; }
.product-card { cursor: pointer; transition: transform 0.2s; }
.product-card:hover { transform: translateY(-3px); }
.cover { width: 100%; height: 160px; display: block; border-radius: 8px; }
.cover-fallback {
  height: 160px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #fdf3f7, #fceaf1); color: #d9a3ba; font-size: 13px;
}
.pt { margin-top: 10px; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pb { display: flex; justify-content: space-between; align-items: center; margin-top: 6px; }
.price { color: #e0557f; font-weight: 600; }
.meta { color: #909399; font-size: 12px; }
.pager { display: flex; justify-content: center; margin-top: 8px; }
</style>
