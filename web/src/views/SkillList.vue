<template>
  <div class="page">
    <el-header class="header">
      <div class="brand">友邻帮</div>
      <div class="search">
        <el-input v-model="keyword" placeholder="搜技能：修电脑 / 家教 / 代取..." clearable @keyup.enter="load(1)">
          <template #append><el-button @click="load(1)">搜索</el-button></template>
        </el-input>
      </div>
      <div class="right">
        <el-button type="primary" @click="router.push('/skills/new')">挂出我的技能</el-button>
        <el-button text @click="router.push('/products/new')">二手市场</el-button>
        <el-button text @click="router.push('/wallet')">钱包</el-button>
        <el-button text @click="router.push('/orders')">订单</el-button>
        <el-button text @click="router.push('/realname')">实名认证</el-button>
        <el-button text type="danger" @click="onLogout">退出</el-button>
      </div>
    </el-header>

    <el-main class="main">
      <div class="categories">
        <el-tag v-for="c in categories" :key="c" :effect="category === c ? 'dark' : 'plain'"
                class="cat" @click="pick(c)">{{ c }}</el-tag>
      </div>

      <el-row v-if="skills.length" :gutter="16">
        <el-col v-for="s in skills" :key="s.id" :span="8" class="card-col">
          <el-card shadow="hover" class="skill-card" @click="router.push(`/skills/${s.id}`)">
            <div class="cat-tag">{{ s.category }}</div>
            <div class="title">{{ s.title }}</div>
            <div class="desc">{{ s.description || '（点击查看详情）' }}</div>
            <div class="foot">
              <span class="price">¥{{ s.price }} <span class="unit">/ {{ s.priceUnit }}</span></span>
              <span class="meta">{{ s.viewCount }} 浏览</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="还没有这类技能挂单，快来抢占第一位" />

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
import request from '../api/request'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()

const categories = ['全部', '维修', '家教', '代取', '跑腿', '摄影', '其他']
const keyword = ref('')
const category = ref('')
const skills = ref([])
const total = ref(0)
const page = ref(1)
const size = 9

async function load(p = page.value) {
  page.value = p
  const data = await request.get('/skills', {
    params: {
      page, size,
      keyword: keyword.value || undefined,
      category: category.value && category.value !== '全部' ? category.value : undefined
    }
  })
  skills.value = data.records
  total.value = Number(data.total)
}

function pick(c) {
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
  display: flex; align-items: center; gap: 20px;
  background: #fff; border-radius: 8px; margin-top: 16px; padding: 0 24px; height: 64px;
}
.brand { font-size: 20px; font-weight: 600; color: #c94d7e; letter-spacing: 1px; }
.search { flex: 1; max-width: 420px; }
.right { margin-left: auto; display: flex; }
.categories { margin-bottom: 16px; }
.cat { cursor: pointer; margin-right: 8px; }
.card-col { margin-bottom: 16px; }
.skill-card { cursor: pointer; transition: transform 0.2s; }
.skill-card:hover { transform: translateY(-3px); }
.cat-tag { color: #c94d7e; font-size: 12px; margin-bottom: 6px; }
.title { font-size: 15px; font-weight: 600; margin-bottom: 6px; }
.desc {
  color: #909399; font-size: 13px; height: 38px; overflow: hidden;
  text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  margin-bottom: 10px;
}
.foot { display: flex; justify-content: space-between; align-items: center; }
.price { color: #e0557f; font-weight: 600; font-size: 17px; }
.unit { font-size: 12px; color: #909399; font-weight: 400; }
.meta { color: #909399; font-size: 12px; }
.pager { display: flex; justify-content: center; margin-top: 8px; }
</style>
