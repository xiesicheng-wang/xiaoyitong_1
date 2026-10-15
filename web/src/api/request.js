import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

/**
 * 统一请求封装：
 * - 自动附带 Authorization: Bearer <token>
 * - 统一处理后端 { code, message, data } 结构
 * - 401 统一踢回登录页
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use((config) => {
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
      if (body.code === 401) {
        localStorage.removeItem('token')
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
    ElMessage.error('网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
