# Agent 用途与构造

`agent_info.agent_kind` 指定 Agent 用途：`CHAT`（用户聊天）或
`CREATE_COSPLAYER`（客服）。在管理后台的 Agent 表单中选择用途；旧 Agent
和未传用途的新增请求默认为 `CHAT`，修改请求省略用途时保留原值。

两个用途通过同一个聊天接口提交 `agentId`，共用 `ChatApplication.stream`、
`AgentRuntime.stream` 和 Session/Run/Message 的持久化流程。服务端加载
`AgentDefinition` 后，`HarnessAgentFactory` 根据 Kind 选择构造方法；新增
工具、Skill 或其他构造差异时，修改对应的私有构造方法。

聊天 Agent 保留已有构造行为，禁用默认 Workspace Skill Repository。
客服 Agent 保留默认 Workspace Skill Repository。两者均禁用 Shell、
文件系统工具、动态 Skill、子 Agent 和自动长期记忆维护。

工作目录根路径为 `.agentOS/workspace`：

- 聊天：`agents/{agentId}/users`，兼容已有聊天目录。
- 客服：`customer-service/tenants/{tenantId}/agents/{agentId}`，在客服用途内按租户与 Agent 分开。

客服工作目录独立于原先共享的根目录，已有客服 Skill 需要放入对应工作目录。

已有数据库部署前执行 `sql/agent_kind_migration.sql`；新数据库使用包含该字段的
`sql/agent_init.sql`。迁移仅增加字段并默认设为聊天用途，不自动修改任何 Agent 的用途。
