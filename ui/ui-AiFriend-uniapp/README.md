# ui-AiFriend-uniapp

AI Friend 的 uni-app + Vue 3 移动端基础项目。目录分层参考 `ui-mall-uniapp`，保留页面、组件、API、请求、路由、状态和配置等扩展入口。

## 运行

```bash
pnpm install
pnpm dev:h5
```

也可以使用 HBuilderX 打开本目录运行到 App 或小程序。

## 后端配置

默认通过统一网关 `http://localhost:8081` 请求，App 接口前缀为 `/app-api`。首页登录后会自动请求：

```text
GET http://localhost:8081/app-api/AiFriend/test/get
```

接口地址在 `.env` 中配置。H5 开发环境会将浏览器发出的 `/app-api/**` 原路径代理到网关，不会添加额外前缀；真机运行时请将 `AIFRIEND_BASE_URL` 改为网关所在电脑的局域网地址。

认证逻辑与 `ui-mall-uniapp` 一致：

- 手机号 + 密码：`POST /app-api/member/auth/login`
- 获取验证码：`POST /app-api/member/auth/send-sms-code`
- 短信登录/自动注册：`POST /app-api/member/auth/sms-login`
- 刷新令牌：`POST /app-api/member/auth/refresh-token`
- 登出：`POST /app-api/member/auth/logout`

登录成功后保存 `accessToken` 和 `refreshToken`；访问令牌失效时请求层会自动刷新并重放原请求。
