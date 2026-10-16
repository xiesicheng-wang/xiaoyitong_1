<template>
  <div class="page">
    <SiteHeader />

    <el-main class="main">
      <div class="chat-head">
        <span class="chat-title">社区群聊</span>
        <el-tag size="small" :type="connected ? 'success' : 'danger'">
          {{ connected ? '已连接' : '未连接' }}
        </el-tag>
      </div>
      <el-card class="chat-card">
        <div class="messages" ref="box">
          <div v-for="(m, i) in messages" :key="i" class="line" :class="{ mine: m.userId === store.user?.id, sys: m.type === 'system' }">
            <template v-if="m.type === 'system'">
              <span class="sys-text">— {{ m.content }} —</span>
            </template>
            <template v-else>
              <span class="meta">{{ m.nickname || m.username }} · {{ m.ts }}</span>
              <span class="bubble">{{ m.content }}</span>
            </template>
          </div>
        </div>
        <div class="input-row">
          <el-input v-model="draft" maxlength="500" placeholder="说点什么…（回车发送）"
                    :disabled="!connected" @keyup.enter="send" />
          <el-button type="primary" :disabled="!connected" @click="send">发送</el-button>
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useUserStore } from '../stores/user'
import SiteHeader from '../components/SiteHeader.vue'
import request from '../api/request'

const router = useRouter()
const store = useUserStore()

const messages = ref([])
const draft = ref('')
const connected = ref(false)
const box = ref(null)
let ws = null
let reconnectTimer = null

function scrollBottom() {
  nextTick(() => { if (box.value) box.value.scrollTop = box.value.scrollHeight })
}

function connect() {
  // 浏览器 WS 无法带 Authorization 头 → token 走查询参数，握手时后端校验
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  ws = new WebSocket(`${proto}://${location.host}/ws/chat?token=${store.token}`)
  ws.onopen = async () => {
    connected.value = true
    // 拉历史消息（倒序存储，反转展示）
    const data = await request.get('/chat/messages', { params: { page: 1, size: 30 } })
    messages.value = data.records.reverse().map(m => ({
      type: 'chat', userId: m.userId, username: m.username,
      nickname: m.nickname || m.username, content: m.content,
      ts: (m.createTime || '').replace('T', ' ').slice(11, 19)
    }))
    scrollBottom()
  }
  ws.onmessage = (e) => {
    try {
      const m = JSON.parse(e.data)
      messages.value.push(m)
      if (messages.value.length > 200) messages.value.splice(0, messages.value.length - 200)
      scrollBottom()
    } catch { /* 忽略非法帧 */ }
  }
  ws.onclose = () => {
    connected.value = false
    reconnectTimer = setTimeout(connect, 3000) // 断线 3 秒重连
  }
  ws.onerror = () => ws.close()
}

function send() {
  const content = draft.value.trim()
  if (!content || !connected.value) return
  ws.send(JSON.stringify({ content }))
  draft.value = ''
}

onMounted(connect)
onUnmounted(() => { clearTimeout(reconnectTimer); ws?.close() })
</script>

<style scoped>
.page { min-height: 100vh; }
.main { max-width: 800px; margin: 12px auto; padding: 0 16px; }
.chat-head { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.chat-title { font-weight: 600; }
.chat-card :deep(.el-card__body) { padding: 16px; }
.messages {
  height: 55vh; overflow-y: auto; background: #fdf3f7;
  border-radius: 8px; padding: 12px; display: flex; flex-direction: column; gap: 6px;
}
.line { display: flex; flex-direction: column; align-items: flex-start; }
.line.mine { align-items: flex-end; }
.sys { align-items: center; }
.sys-text { color: #b899a5; font-size: 12px; }
.meta { color: #909399; font-size: 12px; margin: 0 6px 2px; }
.bubble {
  background: #fff; border: 1px solid #f6e2ea; border-radius: 10px;
  padding: 6px 12px; max-width: 70%; word-break: break-all; font-size: 14px;
}
.mine .bubble { background: #ee7bab; color: #fff; border-color: #ee7bab; }
.input-row { display: flex; gap: 10px; margin-top: 12px; }
</style>
