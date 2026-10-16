const { get, post } = require('../../utils/request')

Page({
  data: { product: null, favorited: false, reviews: [], canBuy: false },

  onLoad(options) { this.id = options.id },

  onShow() { this.load() },

  async load() {
    const product = await get('/api/products/' + this.id)
    const user = wx.getStorageSync('user')
    this.setData({
      product,
      canBuy: !!user && user.id !== product.userId
    })
    try {
      const fav = await get(`/api/favorites/PRODUCT/${this.id}/status`)
      this.setData({ favorited: fav.favorited })
    } catch (e) { /* 未登录 */ }
    const reviews = await get('/api/reviews', { targetType: 'PRODUCT', targetId: this.id })
    this.setData({ reviews })
  },

  async toggleFav() {
    const data = await post(`/api/favorites/PRODUCT/${this.id}/toggle`)
    this.setData({ favorited: data.favorited })
    wx.showToast({ title: data.favorited ? '已收藏' : '已取消', icon: 'none' })
  },

  buy() {
    wx.showModal({
      title: '确认下单',
      content: `下单将预冻结 ¥${this.data.product.price}，超时未完成扣除 20% 违约金`,
      success: async (res) => {
        if (!res.confirm) return
        try {
          await post('/api/orders', { productId: Number(this.id) })
          wx.showToast({ title: '下单成功', icon: 'success' })
          setTimeout(() => wx.switchTab({ url: '/pages/orders/orders' }), 600)
        } catch (e) { /* toast 已提示 */ }
      }
    })
  }
})
