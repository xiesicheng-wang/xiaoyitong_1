const { get } = require('../../utils/request')

Page({
  data: {
    categories: ['全部', '数码', '图书', '家居', '服饰', '运动', '其他'],
    keyword: '', category: '', products: []
  },

  onShow() { this.load() },

  onSearchInput(e) { this.setData({ keyword: e.detail.value }) },
  search() { this.load() },
  pickCategory(e) {
    this.setData({ category: e.currentTarget.dataset.cat })
    this.load()
  },

  async load() {
    try {
      const { keyword, category } = this.data
      const data = await get('/api/products', {
        page: 1, size: 20,
        keyword: keyword || undefined,
        category: category && category !== '全部' ? category : undefined
      })
      this.setData({ products: data.records })
    } catch (e) { /* toast 已提示 */ }
  },

  goDetail(e) { wx.navigateTo({ url: '/pages/detail/detail?id=' + e.currentTarget.dataset.id }) },
  goPublish() { wx.navigateTo({ url: '/pages/publish/publish' }) }
})
