# ui-AiFriend-uniapp

AI Friend 的 uni-app + Vue 3 移动端基础项目。目录分层参考 `ui-mall-uniapp`，保留页面、组件、API、请求、路由、状态和配置等扩展入口。

## 运行

```bash
pnpm install
pnpm dev:h5
```

也可以使用 HBuilderX 打开本目录运行到 App 或小程序。

## 后端配置

默认连接 `http://127.0.0.1:8088`，首页会自动请求：

```text
GET /AiFriend/test/get
```

接口地址在 `.env` 中配置。H5 开发环境使用 Vite 代理；真机运行时请将 `AIFRIEND_BASE_URL` 改为后端所在电脑的局域网地址。

当前注册、登录是前端占位流程：填写非空账号和密码即可进入主页，方便先搭建后续需求。正式认证接口确定后，可在 `aifriend/api` 和 `aifriend/store/user.js` 中替换。
