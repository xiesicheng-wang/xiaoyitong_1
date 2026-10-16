const { get } = require('../../utils/request')

Page({
  data: {
    categories: ['全部', '维修', '家教', '代取', '跑腿', '摄影', '其他'],
    keyword: '', category: '', skills: []
  },

  onShow() { this.load() },
  onSearchInput(e) { this.setData({ keyword: e.detail.value }) },
  search() { this.load() },
  pickCategory(e) {
    this.setData({ category: e.currentTarget.dataset.cat })
    this.load()
  },

  async load() {
    const { keyword, category } = this.data
    const data = await get('/api/skills', {
      page: 1, size: 20,
      keyword: keyword || undefined,
      category: category && category !== '全部' ? category : undefined
    })
    this.setData({ skills: data.records })
  },

  goDetail(e) { wx.navigateTo({ url: '/pages/skill-detail/skill-detail?id=' + e.currentTarget.dataset.id }) }
})
