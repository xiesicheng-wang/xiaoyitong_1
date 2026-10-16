import { defineStore } from 'pinia'
import request from '../api/request'

/**
 * 用户状态：token 持久化到 localStorage，刷新页面不掉登录
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  actions: {
    async login(payload) {
      const data = await request.post('/auth/login', payload)
      this.token = data.token
      this.user = data.user
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(data.user))
      // 登录时间戳：前端会话 24 小时有效，过期自动踢回登录页
      localStorage.setItem('loginAt', String(Date.now()))
    },
    async fetchMe() {
      this.user = await request.get('/user/me')
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('loginAt')
    }
  }
})
