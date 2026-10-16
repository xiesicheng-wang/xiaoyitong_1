const { get, post } = require('../../utils/request')

Page({
  data: { wallet: {}, logs: [], amount: '', loading: false },

  onShow() { this.load() },
  onInput(e) { this.setData({ amount: e.detail.value }) },

  typeName(t) {
    return { RECHARGE: '充值', FREEZE: '冻结', SETTLE: '结算', REFUND: '退款', PENALTY: '违约金' }[t] || t
  },
  isIncome(t) { return ['RECHARGE', 'REFUND', 'PENALTY'].includes(t) },

  async load() {
    this.setData({
      wallet: await get('/api/wallet'),
      logs: (await get('/api/wallet/logs', { page: 1, size: 20 })).records
    })
  },

  async recharge() {
    const amount = Number(this.data.amount)
    if (!amount || amount <= 0) {
      wx.showToast({ title: '请输入金额', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      await post('/api/wallet/recharge', { amount })
      wx.showToast({ title: '充值成功', icon: 'success' })
      this.setData({ amount: '' })
      this.load()
    } catch (e) { /* toast 已提示 */ } finally {
      this.setData({ loading: false })
    }
  }
})
