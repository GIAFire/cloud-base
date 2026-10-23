# 织梦 · AI Friend

uni-app + Vue 3 + Pinia 前端，支持桌面网页和手机 H5。邮箱登录、注册和密码修改已接入会员后端，其他业务数据仍待接入。

## 运行与验证

```bash
pnpm install
pnpm dev:h5
pnpm test
pnpm build:h5
```

开发端口默认 3001，被占用时使用下一可用端口，以终端输出为准。生产产物位于 `dist/build/h5`；history 路由部署时，需要将未匹配的页面请求回退至 `index.html`。

## 当前页面与行为

- 首页保留发现、搜索、筛选和角色详情结构，未加载角色时显示空状态。
- 对话页只展示已有对话，不再预置角色、欢迎消息或回复。
- 视频页保留推荐/关注及列表结构，没有数据时显示空状态；不再用封面动画和虚假进度条代替视频播放。
- 个人中心保留收藏、记忆、偏好及隐私入口。
- 登录和注册均由后端验证；成功后保存 accessToken、refreshToken 和会员基础信息，返回原页面。注册接口返回登录令牌，因此注册成功后直接登录。
- 注册和密码修改使用真实邮箱验证码，场景分别为 `1`、`2`；只有接口确认成功后才开始 60 秒重发倒计时，失败时显示后端原因。
- 登录页提供找回密码，已登录的个人中心提供“修改密码”，两者均使用邮箱验证码重置密码接口。成功后清除本地令牌并返回登录页，后端同步撤销该会员的旧令牌。
- 应用启动时恢复已有登录，并获取会员资料；访问令牌过期继续使用已有请求层的刷新与重放能力。退出登录清除本地认证数据。
- 消息、留言、预约等未接入的业务操作仍提示暂不可用，不生成模拟业务记录。
- 本地保存用户草稿、偏好以及真实认证令牌和公开会员资料；不保存密码或验证码。旧演示存储仍会在启动时清除。

`demo.html` 保留为原始设计参考，不参与应用构建或运行。

## 分层与接口接入

```text
pages/                          页面与生命周期
aifriend/components/            公共布局、表单、角色等界面组件
aifriend/domain/catalog.js      通用领域选项与显示格式
aifriend/services/experience.js  业务边界、搜索、表单校验及返回地址规则
aifriend/services/member-auth.js 会员认证契约与业务流程
aifriend/services/member-auth-runtime.js 真实 API 与认证服务装配
aifriend/store/experience.js     Pinia 页面状态
aifriend/repositories/experience.js 本地草稿与偏好存储
aifriend/api/                   已有真实 API，留待接入
aifriend/request/               请求、网关与令牌刷新能力
aifriend/router/                导航
aifriend/styles/                视觉与响应式适配
tests/experience.test.mjs        空状态、服务边界与存储回归测试
```

会员认证已启用真实 API；角色、视频和对话后续由服务层接入。网关配置位于 `.env.*`（`AIFRIEND_BASE_URL`、`AIFRIEND_API_PATH`、`AIFRIEND_H5_PROXY_ENABLED`）。

## 后端准备

1. 若尚未安装邮箱认证表，使用 `modules/module-member/docs/sql/member-email-auth.sql`。已安装的环境执行增量脚本 `modules/module-member/docs/sql/member-email-auth-integration.sql`，将登录日志标识和验证码创建者字段扩至 100 字符。
2. 重启加载最新会员和系统模块代码的后端服务；本次修复了被注释的邮件发送调用，以及登录日志对长邮箱的 30 字符限制。
3. 配置可用的系统邮件账户，启用 `member-email-register-code` 和 `member-email-reset-password-code` 两个邮件模板，均声明 `code` 参数。服务端不返回或打印真实验证码。

当前接口（统一前缀 `/app-api`）：

- `POST /member/auth/email-login`：`email, password`
- `POST /member/auth/email-code/send`：`email, scene`（注册 1，密码重置 2）
- `POST /member/auth/email-register`：`email, password, code`，返回令牌
- `POST /member/auth/email-reset-password`：`email, password, code`
- `GET /member/user/get`：获取当前会员
- `POST /member/auth/logout`：退出登录

认证错误统一显示接口返回原因；注册、密码重置的密码长度遵循后端 8–32 位约束，不额外要求字母与数字组合。登录兼容已有账号的较短密码。

App/微信小程序的配置与脚本保留，本阶段只验收桌面浏览器与手机 H5。
