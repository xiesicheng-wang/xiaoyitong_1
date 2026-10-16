const { post } = require('../../utils/request')

Page({
  data: { mode: 'login', username: '', password: '', phone: '', loading: false },

  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value })
  },

  switchMode() {
    this.setData({ mode: this.data.mode === 'login' ? 'register' : 'login' })
  },

  async submit() {
    const { mode, username, password, phone } = this.data
    if (!username || !password) {
      wx.showToast({ title: '请填写用户名和密码', icon: 'none' })
      return
    }
    if (mode === 'register' && !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      let data
      if (mode === 'login') {
        data = await post('/api/auth/login', { username, password })
      } else {
        await post('/api/auth/register', { username, password, phone })
        data = await post('/api/auth/login', { username, password })
      }
      wx.setStorageSync('token', data.token)
      wx.setStorageSync('user', data.user)
      wx.showToast({ title: '欢迎回来', icon: 'success' })
      setTimeout(() => wx.switchTab({ url: '/pages/index/index' }), 600)
    } catch (e) {
      // 错误提示已在 request 封装中 toast
    } finally {
      this.setData({ loading: false })
    }
  }
})
