const { get, post } = require('../../utils/request')

Page({
  data: {
    tab: 'bought', orders: [], reviewedIds: [],
    reviewVisible: false, reviewLoading: false,
    reviewForm: { orderId: null, score: 5, content: '' }
  },

  onShow() { this.load() },

  switchTab(e) {
    this.setData({ tab: e.currentTarget.dataset.tab })
    this.load()
  },

  statusText(s) {
    return { 0: '进行中(冻结)', 1: '已完成', 2: '已取消', 3: '违约关闭' }[s] || ''
  },

  async load() {
    try {
      const data = await get(`/api/orders/${this.data.tab}`, { page: 1, size: 20 })
      this.setData({ orders: data.records })
      const ids = await get('/api/reviews/my-reviewed-ids')
      this.setData({ reviewedIds: ids })
    } catch (e) { /* 未登录时 request 已跳登录 */ }
  },

  complete(e) {
    wx.showModal({
      title: '确认完成',
      content: '确认已线下完成交易？货款将结算给你',
      success: async (r) => {
        if (!r.confirm) return
        await post(`/api/orders/${e.currentTarget.dataset.id}/complete`)
        wx.showToast({ title: '已结算', icon: 'success' })
        this.load()
      }
    })
  },

  cancel(e) {
    wx.showModal({
      title: '取消订单',
      content: '取消将全额退款，确认？',
      success: async (r) => {
        if (!r.confirm) return
        await post(`/api/orders/${e.currentTarget.dataset.id}/cancel`)
        wx.showToast({ title: '已取消退款', icon: 'success' })
        this.load()
      }
    })
  },

  openReview(e) {
    this.setData({
      reviewVisible: true,
      reviewForm: { orderId: Number(e.currentTarget.dataset.id), score: 5, content: '' }
    })
  },
  closeReview() { this.setData({ reviewVisible: false }) },
  setScore(e) { this.setData({ 'reviewForm.score': Number(e.currentTarget.dataset.score) }) },
  onReviewInput(e) { this.setData({ 'reviewForm.content': e.detail.value }) },

  async submitReview() {
    this.setData({ reviewLoading: true })
    try {
      await post('/api/reviews', {
        orderId: this.data.reviewForm.orderId,
        score: this.data.reviewForm.score,
        content: this.data.reviewForm.content
      })
      wx.showToast({ title: '评价成功', icon: 'success' })
      this.setData({ reviewVisible: false })
      this.load()
    } catch (e) { /* toast 已提示 */ } finally {
      this.setData({ reviewLoading: false })
    }
  }
})
