const { get, post } = require('../../utils/request')

Page({
  data: {
    user: null, realnameText: '未实名', reviewStat: {}, reviews: [],
    favCount: 0, favList: []
  },

  onShow() {
    const user = wx.getStorageSync('user')
    this.setData({ user, realnameText: { 0: '未实名', 1: '待审核', 2: '已实名' }[user?.realnameStatus] || '未实名' })
    if (!wx.getStorageSync('token')) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    this.load()
  },

  async load() {
    try {
      const user = wx.getStorageSync('user')
      const stat = await get('/api/reviews/stat', { userId: user.id })
      this.setData({ reviewStat: stat })
      // 我收到的评价：从互评表按人查——简化用收藏/统计展示，评价明细走订单
      const favs = await get('/api/favorites/my')
      this.setData({ favCount: favs.length, favList: favs.slice(0, 5) })
    } catch (e) { /* toast 已提示 */ }
  },

  go(e) { wx.navigateTo({ url: e.currentTarget.dataset.url }) },
  goFavorites() { /* 收藏列表已在下方展示 */ },

  toggleRealname() {
    if (this.data.user.realnameStatus === 2) {
      wx.showToast({ title: '已通过实名认证', icon: 'none' })
      return
    }
    // 小程序内提交实名需上传证件照，引导到网页端完成（H5 体验更完整）
    wx.showModal({
      title: '实名认证',
      content: '请登录网页版友邻帮完成实名提交（上传证件照需网页端操作）',
      showCancel: false
    })
  },

  logout() {
    wx.removeStorageSync('token')
    wx.removeStorageSync('user')
    wx.navigateTo({ url: '/pages/login/login' })
  }
})
