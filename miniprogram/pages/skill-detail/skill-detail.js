const { get, post } = require('../../utils/request')

Page({
  data: { skill: null, favorited: false },

  onLoad(options) { this.id = options.id },
  onShow() { this.load() },

  async load() {
    const skill = await get('/api/skills/' + this.id)
    this.setData({ skill })
    try {
      const fav = await get(`/api/favorites/SKILL/${this.id}/status`)
      this.setData({ favorited: fav.favorited })
    } catch (e) { /* 未登录 */ }
  },

  async toggleFav() {
    const data = await post(`/api/favorites/SKILL/${this.id}/toggle`)
    this.setData({ favorited: data.favorited })
    wx.showToast({ title: data.favorited ? '已收藏' : '已取消', icon: 'none' })
  },

  buy() {
    wx.showModal({
      title: '确认预约',
      content: `预约将预冻结 ¥${this.data.skill.price}，超时未完成扣除 20% 违约金`,
      success: async (res) => {
        if (!res.confirm) return
        try {
          await post('/api/orders/skill', { skillId: Number(this.id) })
          wx.showToast({ title: '预约成功', icon: 'success' })
          setTimeout(() => wx.switchTab({ url: '/pages/orders/orders' }), 600)
        } catch (e) { /* toast 已提示 */ }
      }
    })
  }
})
