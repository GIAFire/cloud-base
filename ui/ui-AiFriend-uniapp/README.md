# 心屿 · AI Friend

按本目录的 `demo.html` 实现的 uni-app + Vue 3 + Pinia 前端，支持桌面网页和手机 H5。默认使用本地模拟数据，无需启动后端。

## 运行与验证

```bash
pnpm install
pnpm dev:h5
pnpm test
pnpm build:h5
```

开发端口默认是 3001；被占用时 Vite 会选择下一可用端口，以终端输出为准。生产产物位于 `dist/build/h5`，使用 history 路由，部署时将未匹配的页面请求回退到 `index.html`。

## 页面和演示

- 首页：角色发现、搜索、分类/标签筛选、收藏和角色详情。
- 对话：对话列表、消息草稿、预设回复、陪伴风格和静态视频邀请。
- 视频：竖向动态封面流；手机上下滑动，桌面滚轮/方向键切换；暂停、关注、点赞、收藏和留言。
- 我的：收藏入口、对话统计、记忆说明、陪伴偏好、隐私与数据清除、退出登录。
- 认证：邮箱登录、注册、找回密码；验证码为 `123456`，表单保留校验、重发倒计时、密码显示和协议说明。

访客可以浏览、搜索和筛选；聊天、收藏、视频互动和保存偏好需要模拟登录。登录页可点击“填入演示身份”：`traveler@example.com / Xinyu1234`。所有账号流程都是演示，不验证真实账号、不发送邮件、不保存密码。

登录后回到原页面，收藏等操作恢复执行；消息与留言草稿保留，由用户再次点击发送。单一演示数据集由所有演示邮箱共用，不提供多账号隔离。

演示身份、聊天、草稿、收藏、视频互动、偏好与预约状态保存在当前浏览器。退出登录保留数据；清除体验数据需要二次确认，只删除本应用演示存储，并退出演示身份。损坏的存储会恢复默认数据；无法保存时有提示。

真实大模型、长期记忆、语音、视频素材播放、实时通话、付费订阅及真实认证留待后续接入。原型中的 Unsplash 图片地址继续使用，图片显示需要网络；应用本身不请求后端业务接口。

## 分层与接口接入位置

```text
pages/                        页面展示、页面生命周期与导航
aifriend/components/          公共布局、按钮、弹窗、角色、认证及通话界面
aifriend/domain/catalog.js    角色、动态及陪伴偏好的领域数据
aifriend/services/experience.js 数据访问边界、搜索、模拟回复、认证校验与返回地址规则
aifriend/store/experience.js  Pinia 体验状态、登录意图恢复及交互流程
aifriend/repositories/experience.js 本地数据契约、恢复、保存与清除
aifriend/api/                 已有真实接口，留待后续接入
aifriend/request/             已有请求、令牌刷新及网关能力
aifriend/router/              uni-app 导航封装
aifriend/styles/              原型视觉规则与 uni-app 样式适配
tests/experience.test.mjs     数据边界、认证、交互与持久化测试
```

页面不直接请求真实 API 或读写浏览器存储。后续在服务/数据仓库边界接入异步 API，并逐步替换模拟认证与预设回复。现有 `api/auth.js`、`api/user.js`、`request/index.js` 和真实用户 Store 均保留，但本阶段界面不启用真实 Token 登录逻辑。

真实接口原有配置仍在 `.env.*` 中（网关 `AIFRIEND_BASE_URL`、接口前缀 `AIFRIEND_API_PATH`、H5 代理 `AIFRIEND_H5_PROXY_ENABLED`），修改这些配置不会自动启用真实接口。

保留 App/微信小程序构建脚本及配置；本次交付与验收范围仅为桌面浏览器和手机 H5，不承诺其他平台可用。
