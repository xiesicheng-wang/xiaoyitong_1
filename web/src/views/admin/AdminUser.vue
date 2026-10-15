<template>
  <el-card>
    <template #header>
      <div class="head">
        <span>用户管理</span>
        <el-input v-model="keyword" placeholder="用户名/昵称搜索" clearable style="width: 220px"
                  @keyup.enter="load(1)" @clear="load(1)">
          <template #append><el-button @click="load(1)">搜索</el-button></template>
        </el-input>
      </div>
    </template>
    <el-table :data="records" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="phoneMasked" label="手机号" width="130" />
      <el-table-column label="信誉分" width="90">
        <template #default="{ row }">
          <el-tag :type="row.creditScore >= 80 ? 'success' : row.creditScore >= 60 ? 'warning' : 'danger'" size="small">
            {{ row.creditScore }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="实名" width="90">
        <template #default="{ row }">
          <el-tag :type="row.realnameStatus === 2 ? 'success' : 'info'" size="small">
            {{ { 0: '未实名', 1: '待审核', 2: '已实名' }[row.realnameStatus] || '未实名' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="账号状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '正常' : '已封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.role !== 1" size="small" :type="row.status === 0 ? 'danger' : 'success'" plain
                     @click="ban(row)">
            {{ row.status === 0 ? '封禁' : '解封' }}
          </el-button>
          <el-tag v-else type="danger" effect="plain" size="small">管理员</el-tag>
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
  const data = await request.get('/admin/users', { params: { page, size, keyword: keyword.value || undefined } })
  records.value = data.records
  total.value = Number(data.total)
}

async function ban(row) {
  const banning = row.status === 0
  await ElMessageBox.confirm(
    banning ? `封禁「${row.username}」？封禁后将无法登录` : `解封「${row.username}」？`,
    '确认操作', { type: banning ? 'warning' : 'info' }
  )
  await request.post(`/admin/users/${row.id}/ban`, { ban: banning })
  ElMessage.success('操作成功')
  load(page.value)
}

onMounted(load)
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: center; }
.pager { display: flex; justify-content: center; margin-top: 12px; }
</style>
