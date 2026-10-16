import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

/**
 * 统一请求封装：
 * - 自动附带 Authorization: Bearer <token>
 * - 前端会话 24 小时有效（与后端 JWT 有效期一致），过期自动清登录态
 * - 统一处理后端 { code, message, data } 结构
 * - 401 统一踢回登录页
 */
const SESSION_MS = 24 * 60 * 60 * 1000

export function sessionExpired() {
  const token = localStorage.getItem('token')
  if (!token) return false
  const loginAt = Number(localStorage.getItem('loginAt') || 0)
  return loginAt > 0 && Date.now() - loginAt > SESSION_MS
}

export function clearSession() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('loginAt')
}

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  // 会话超时：主动清掉并跳登录，不再发请求
  if (sessionExpired()) {
    clearSession()
    ElMessage.warning('登录已超过 24 小时，请重新登录')
    router.push('/login')
    return Promise.reject(new Error('会话过期'))
  }
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body.code !== 200) {
      if (body.code === 401 || body.code === 403 && String(body.message || '').includes('登录已过期')) {
        clearSession()
        ElMessage.warning('登录已过期，请重新登录')
        router.push('/login')
      } else {
        ElMessage.error(body.message || '请求失败')
      }
      return Promise.reject(new Error(body.message))
    }
    return body.data
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      clearSession()
      ElMessage.warning('登录已过期，请重新登录')
      router.push('/login')
    } else if (status === 503) {
      ElMessage.error(error.response?.data?.message || '服务暂不可用，请检查后端服务')
    } else {
      ElMessage.error(error.response?.data?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
