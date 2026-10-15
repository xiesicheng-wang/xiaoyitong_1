<template>
  <el-card>
    <template #header>实名审核</template>
    <el-table :data="records" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="realName" label="姓名" width="110" />
      <el-table-column prop="idCardMasked" label="身份证（掩码）" width="190" />
      <el-table-column label="证件照" width="90">
        <template #default="{ row }">
          <el-image :src="row.photoUrl" :preview-src-list="[row.photoUrl]"
                    preview-teleported fit="cover" style="width: 50px; height: 50px" />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.tag" size="small">{{ statusMap[row.status]?.text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="170" />
      <el-table-column label="操作" min-width="180">
        <template #default="{ row }">
          <template v-if="row.status === 1">
            <el-button size="small" type="success" @click="audit(row, true)">通过</el-button>
            <el-button size="small" type="danger" plain @click="audit(row, false)">驳回</el-button>
          </template>
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

const statusMap = {
  1: { text: '待审核', tag: 'warning' },
  2: { text: '已通过', tag: 'success' },
  3: { text: '已驳回', tag: 'danger' }
}

async function load(p = 1) {
  page.value = p
  const data = await request.get('/admin/realnames', { params: { page, size } })
  records.value = data.records
  total.value = Number(data.total)
}

async function audit(row, pass) {
  let reason = ''
  if (!pass) {
    const r = await ElMessageBox.prompt('请填写驳回原因', `驳回 ${row.realName} 的申请`, {
      inputPlaceholder: '如：证件照模糊，无法辨认',
      inputValidator: v => !!v || '驳回原因不能为空'
    })
    reason = r.value
  } else {
    await ElMessageBox.confirm(`确认通过 ${row.realName}（${row.idCardMasked}）的实名申请？`)
  }
  await request.post(`/admin/realnames/${row.id}/audit`, { pass, reason })
  ElMessage.success(pass ? '已通过' : '已驳回')
  load(page.value)
}

onMounted(load)
</script>

<style scoped>
.pager { display: flex; justify-content: center; margin-top: 12px; }
</style>
