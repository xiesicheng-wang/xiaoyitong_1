<template>
  <el-card>
    <template #header>
      <div class="head">
        <span>商品管理</span>
        <el-input v-model="keyword" placeholder="按标题搜索" clearable style="width: 220px"
                  @keyup.enter="load(1)" @clear="load(1)">
          <template #append><el-button @click="load(1)">搜索</el-button></template>
        </el-input>
      </div>
    </template>
    <el-table :data="records" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="price" label="价格" width="90" />
      <el-table-column prop="category" label="分类" width="90" />
      <el-table-column prop="sellerUsername" label="卖家" width="110" />
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">
            {{ ['', '已下架', '已售出'][row.status] || '在售' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130">
        <template #default="{ row }">
          <el-switch v-if="row.status !== 2" :model-value="row.status === 0"
                     active-text="在售" inactive-text="下架"
                     :before-change="() => toggle(row)" />
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="size"
                     :current-page="page" @current-change="load" />
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const records = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')

async function load(p = 1) {
  page.value = p
  const data = await request.get('/admin/products', { params: { page, size, keyword: keyword.value || undefined } })
  records.value = data.records
  total.value = Number(data.total)
}

async function toggle(row) {
  const onSale = row.status !== 0
  await ElMessageBox.confirm(
    onSale ? `重新上架「${row.title}」？` : `下架「${row.title}」？卖家将收到站内通知`,
    '确认操作'
  )
  await request.post(`/admin/products/${row.id}/status`, { onSale })
  ElMessage.success('操作成功')
  load(page.value)
  return true
}

onMounted(load)
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: center; }
.pager { display: flex; justify-content: center; margin-top: 12px; }
</style>
