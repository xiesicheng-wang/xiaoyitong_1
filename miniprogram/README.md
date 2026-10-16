# 友邻帮 微信小程序

原生小程序工程，与网页版共用同一套后端接口（http://localhost:8080）。

## 使用步骤

1. 下载安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 导入项目：选择本目录 `E:\bishe\youlinbang\miniprogram`
3. AppID：先点「测试号」或填入你自己的小程序 AppID（`project.config.json` 中 appid 留白）
4. 右上角「详情」→「本地设置」→ 勾选 **「不校验合法域名」**（开发期必须，否则请求 localhost 被拦）
5. 确保后端已启动（`http://localhost:8080`），编译即可体验

## 已覆盖页面

- 登录 / 注册（用户名+手机号）
- 二手市场：列表/搜索/分类/详情/收藏/下单
- 技能服务：列表/详情/收藏/预约
- 发布闲置（图片上传到 MinIO）
- 订单：买卖双视角 / 确认完成 / 取消退款 / 交易互评
- 钱包：余额 / 冻结 / 充值（模拟）/ 流水
- 我的：实名状态 / 信誉分 / 收到的评价 / 收藏

## 真机调试

- `utils/request.js` 中 `BASE_URL` 改为电脑局域网 IP（如 `http://192.168.1.100:8080`），手机与电脑连同一 WiFi
- 正式上线需 HTTPS 域名并在小程序后台配置 request 合法域名

## 设计说明

- 登录态与网页版一致（JWT + Redis 会话），小程序端用 wx.setStorageSync 持久化
- 401 统一拦截跳登录页
- 实名认证的证件照上传建议在网页端完成（拍照体验更完整），小程序内引导跳转
