# 织梦会员认证接口接入

Status: ready-for-agent
Implementation: completed

## Scope

- 邮箱登录、注册和修改密码接入真实会员后端，不恢复任何演示身份或固定验证码。
- 注册验证码场景 1，密码重置验证码场景 2；只有服务确认成功后启动重发倒计时。
- 登录与注册按后端 `userId/accessToken/refreshToken` 响应建立会话，注册后自动登录并返回原页面。
- 登录页找回密码与个人中心修改密码共用邮箱验证码重置流程；成功后清除本地认证，返回登录。
- 应用启动恢复会员资料；退出、失效会话和密码重置同步更新界面与令牌状态。密码、验证码不进入持久化存储。
- 其他业务数据保持空状态；不更改用户已有的品牌、布局和后端模块调整。

## Backend integration fixes

- 恢复 `MemberEmailCodeServiceImpl` 的会员邮件发送调用，失败结果不再被忽略。
- `LoginLogCreateReqDTO.username` 上限由 30 扩至 100，匹配会员邮箱上限。
- 增量 SQL 扩容 `system_login_log.username` 和 `member_email_code.creator`，避免审计字段阻断长邮箱。

## Validation

- 前端 `pnpm test`：20 项通过。
- 后端针对 `EmailAuthRequestValidationTest,MemberEmailCodeServiceTest`：7 项通过；修复前有 3 项失败，修复后全部通过。
- H5 生产构建通过。
- 已通过真实网关验证登录失败原因回显、密码重置验证码接口确认后的倒计时。
- 当前运行中的后端仍返回旧日志字段的 30 字符限制；源码修复须执行增量 SQL 并重启服务后验证。
- 未使用真实会员邮箱执行收信、注册成功及密码变更完整流程；不读取数据库验证码、不设置固定验证码。

## Runtime preparation

应用 `modules/module-member/docs/sql/member-email-auth-integration.sql`，重启后端，并确认可用邮件账户及注册/密码重置模板已启用。该迁移尚未对当前数据库执行。
