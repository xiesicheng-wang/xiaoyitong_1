const { post, upload } = require('../../utils/request')

Page({
  data: {
    categories: ['数码', '图书', '家居', '服饰', '运动', '其他'],
    title: '', category: '数码', price: '', description: '',
    images: [], imageUrls: [], loading: false
  },

  onInput(e) { this.setData({ [e.currentTarget.dataset.field]: e.detail.value }) },
  onCategory(e) { this.setData({ category: this.data.categories[Number(e.detail.value)] }) },

  chooseImage() {
    wx.chooseMedia({
      count: 9 - this.data.images.length,
      mediaType: ['image'],
      success: async (res) => {
        try {
          wx.showLoading({ title: '上传中' })
          const urls = []
          for (const f of res.tempFiles) {
            const d = await upload('/api/files/upload', f.tempFilePath)
            urls.push(d.url)
          }
          this.setData({
            images: this.data.images.concat(res.tempFiles.map(f => f.tempFilePath)),
            imageUrls: this.data.imageUrls.concat(urls)
          })
        } catch (e) {
          wx.showToast({ title: '图片上传失败', icon: 'none' })
        } finally {
          wx.hideLoading()
        }
      }
    })
  },

  async submit() {
    const { title, price, description, category, imageUrls } = this.data
    if (!title || !price) {
      wx.showToast({ title: '请填写标题和价格', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      await post('/api/products', {
        title, price: Number(price), description,
        category, images: imageUrls
      })
      wx.showToast({ title: '发布成功', icon: 'success' })
      setTimeout(() => wx.switchTab({ url: '/pages/index/index' }), 600)
    } catch (e) { /* toast 已提示 */ } finally {
      this.setData({ loading: false })
    }
  }
})
