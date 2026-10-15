<template>
  <div class="page">
    <el-card>
      <template #header>挂出我的技能</template>
      <el-form :model="form" label-width="90px">
        <el-form-item label="服务标题" required>
          <el-input v-model="form.title" maxlength="64" show-word-limit placeholder="如：修电脑装系统 / 高数家教 / 校园代取快递" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="10" />
          <el-radio-group v-model="form.priceUnit" style="margin-left: 12px">
            <el-radio-button value="次">元 / 次</el-radio-button>
            <el-radio-button value="小时">元 / 小时</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="服务描述">
          <el-input v-model="form.description" type="textarea" :rows="5" maxlength="2000" show-word-limit
                    placeholder="技能背景、服务内容、可约时间、地点范围等" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">挂单</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const categories = ['维修', '家教', '代取', '跑腿', '摄影', '其他']
const loading = ref(false)
const form = reactive({ title: '', category: '其他', price: null, priceUnit: '次', description: '' })

async function onSubmit() {
  if (!form.title) { ElMessage.warning('请填写服务标题'); return }
  if (!form.price) { ElMessage.warning('请填写价格'); return }
  loading.value = true
  try {
    await request.post('/skills', form)
    ElMessage.success('挂单成功')
    router.push('/skills')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { max-width: 720px; margin: 24px auto; }
</style>
