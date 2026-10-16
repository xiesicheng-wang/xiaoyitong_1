/**
 * 请求封装：自动附带 token，401 跳登录页
 *
 * baseUrl 说明：
 * - 微信开发者工具里用 http://localhost:8080 即可（需勾选「不校验合法域名」）
 * - 真机调试改为电脑局域网 IP，如 http://192.168.1.100:8080（手机与电脑同一 WiFi）
 * - 上线前换成你的服务器域名（须 HTTPS 且在小程序后台配置 request 合法域名）
 */
const BASE_URL = 'http://localhost:8080';

function request(method, path, data, isUpload) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');
    const opts = {
      url: BASE_URL + path,
      method,
      header: token ? { Authorization: 'Bearer ' + token } : {},
      success(res) {
        if (res.statusCode === 401 || (res.data && res.data.code === 401)) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('user');
          wx.navigateTo({ url: '/pages/login/login' });
          reject(new Error('未登录'));
          return;
        }
        if (res.data && res.data.code === 200) {
          resolve(res.data.data);
        } else {
          const msg = (res.data && res.data.message) || '请求失败';
          wx.showToast({ title: msg, icon: 'none' });
          reject(new Error(msg));
        }
      },
      fail(err) {
        wx.showToast({ title: '网络异常', icon: 'none' });
        reject(err);
      }
    };
    if (isUpload) {
      // multipart 场景由调用方自己用 wx.uploadFile，不走这里
      reject(new Error('use uploadFile'));
      return;
    }
    if (data && (method === 'POST' || method === 'PUT')) {
      opts.data = data;
      opts.header['Content-Type'] = 'application/json';
    } else if (data) {
      opts.data = data;
    }
    wx.request(opts);
  });
}

function upload(path, filePath) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');
    wx.uploadFile({
      url: BASE_URL + path,
      filePath,
      name: 'file',
      header: token ? { Authorization: 'Bearer ' + token } : {},
      success(res) {
        const j = JSON.parse(res.data);
        if (j.code === 200) resolve(j.data);
        else reject(new Error(j.message || '上传失败'));
      },
      fail: reject
    });
  });
}

const get = (path, data) => request('GET', path, data);
const post = (path, data) => request('POST', path, data);

module.exports = { get, post, upload, BASE_URL };
