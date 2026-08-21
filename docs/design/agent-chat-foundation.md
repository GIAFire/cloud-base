# Agent 聊天基座重建方案与实施交接

> 状态：方案已确认，尚未开始编码  
> 形成日期：2026-08-21  
> 适用仓库：`cloud-base`  
> 目标读者：后续接手实现的开发者或 Codex Agent

## 1. 新会话如何使用本文档

拿到本文档后，先读取仓库根目录 `AGENTS.md` 及其指向的约束，再检查当前工作树和 `modules/module-agent` 的实际代码。以本文档的“首期范围”和“完成定义”为实施依据，保留旧 `module-agent` 作为行为参考，在并行的新模块中搭建最小聊天链路。

开始实施前只需核对本文档第 14 节列出的少量实施决策。整体架构、首期范围和验收标准已经确定，不需要重新从现有大模块中复制 Tool、Skill、审计、知识库等能力。

建议用户在新会话中直接发送：

```text
请阅读这份《Agent 聊天基座重建方案与实施交接》，检查当前仓库状态，
然后保留旧 module-agent，按文档开始实现新的最小 Agent 聊天基座。
先完成文档中的第一里程碑并运行相关测试。
```

## 2. 背景与问题

仓库已经在 `modules/module-agent` 中基于 AgentScope 2.0 实现了完整链路：

```text
前端聊天页面
  → 后端 HTTP/SSE 接口
  → 会话、消息和 Run 初始化
  → 构造 HarnessAgent
  → AgentScope streamEvents()
  → 事件转换、聚合和持久化
  → SSE 响应
  → 前端增量渲染
```

该实现能够工作，但聊天主链路同时承载了控制面和运行数据面的能力，导致模块过重、测试困难、变更风险集中。

截至方案形成时：

- `module-agent` 约有 346 个 Java 文件、2.6 万行 Java 代码。
- `AgentRuntimeFactory` 同时装配模型、Tool、权限、Skill、状态存储、压缩、工作区、Plan 和子 Agent。
- `AgentChatServiceImpl` 同时处理事件转换、Tool 审计、Plan 跟踪、消息聚合、状态日志和 Run 收尾。
- `AgentChatController` 同步完成会话校验、配置查询、用户消息保存和 Run 创建。
- 聊天接口直接暴露 `LoginUser`、`AgentConfigDTO`、`ServerSentEvent` 等不同层的类型。
- 当前 `module-agent` 没有发现 `src/test` 测试。

相关代码入口：

- `modules/module-agent/module-agent-server/src/main/java/com/cloud/module/agent/controller/admin/AgentChatController.java`
- `modules/module-agent/module-agent-server/src/main/java/com/cloud/module/agent/service/AgentChatService.java`
- `modules/module-agent/module-agent-server/src/main/java/com/cloud/module/agent/service/impl/AgentChatServiceImpl.java`
- `modules/module-agent/module-agent-server/src/main/java/com/cloud/module/agent/factory/agentFactory/AgentRuntimeFactory.java`
- `ui/ui-admin/src/api/agent/chat/index.ts`

## 3. 重建目标

新模块先成为稳定、可测试、可扩展的“Agent 聊天运行数据面”，只负责完成一次聊天从请求到流式响应的全过程。

首期目标：

1. UI 能发送文本消息并实时看到文本增量。
2. 后端能鉴权、校验、初始化会话和 Run。
3. 后端能构造最小 AgentScope `HarnessAgent` 并调用 `streamEvents()`。
4. AgentScope 原始事件能转换成平台自己的稳定事件协议。
5. 成功、异常和客户端断开都能可靠收尾。
6. 会话、消息和 Run 能持久化，多轮对话在进程重启后仍可恢复。
7. 核心链路能通过 Fake Runtime 和 HTTP/SSE 集成测试验证。

新模块不是当前 `module-agent` 的文件精简副本。它通过小接口隐藏实现复杂度，后续能力在正确位置逐项加入。

## 4. 首期范围

### 4.1 必须包含

- 单个固定 Agent。
- 单个 OpenAI-compatible 模型协议。
- 系统提示词和 `maxIters`。
- 文本输入和文本增量输出。
- 会话、消息、Run 三个最小持久化模型。
- 成功、失败、取消三种 Run 收尾。
- `requestId` 幂等保护。
- 同一会话同时只运行一个 Run。
- 客户端断开向上游传播取消信号。
- 多轮消息历史加载。
- SSE 网关透传要求和最小健康日志。

### 4.2 首期排除

- Tool 注册和执行。
- Skill 注册和执行。
- 权限确认和外部执行。
- 知识库和 RAG。
- 子 Agent。
- Plan、Task List 和 Workspace。
- Compaction 和 Tool Result Eviction。
- Redis、MySQL AgentState 扩展。
- Nacos Skill Repository。
- 模型调用审计、Tool 审计和逐事件审计。
- 多模型、多协议和模型管理后台。
- 可视化 Agent 配置后台。

基础日志、异常日志和指标预留不等于引入完整审计体系。

## 5. 领域术语

实现和协议统一使用以下术语：

| 术语 | 含义 |
| --- | --- |
| 会话 `Conversation` | 用户与一个 Agent 的连续多轮对话 |
| 消息 `ChatMessage` | 会话中的持久化用户文本或 Agent 文本 |
| 运行 `AgentRun` | 一条用户消息触发的一次 AgentScope 执行 |
| 运行时事件 `RuntimeEvent` | AgentScope Adapter 内部产生的框架无关事件 |
| 聊天事件 `ChatEvent` | Application 对外输出、可被 Web 层转换为 SSE 的平台事件 |

一个会话包含多条消息和多个运行；一条用户消息至多触发一个有效运行；一个运行对应零或一条最终 Agent 消息。

## 6. 控制面与运行数据面

新模块只建设运行数据面：

```text
运行数据面：聊天请求、运行编排、AgentScope 调用、流式事件、消息和 Run 状态
控制面：Agent 定义、模型管理、Tool、Skill、权限、知识库、审计和运营配置
```

控制面未来可以为运行数据面提供已发布配置，但不能重新进入聊天编排主类。首期直接从 Spring 配置读取单个 Agent 和模型配置。

## 7. 目标分层与依赖方向

```text
UI（fetch POST + ReadableStream + AbortController）
    │
    │ SSE
    ▼
Gateway（流式透传，不缓冲）
    │
    ▼
Web Adapter
    │ 鉴权、参数校验、HTTP/SSE 映射
    │ Flux<ChatEvent>
    ▼
Chat Application
    │ begin → invoke → finalize
    ├──────────────► ChatStore
    │                 会话、消息、Run 短事务
    │
    └──────────────► AgentRuntime
                       │
                       ▼
                 AgentScope Adapter
                 构造 Agent、streamEvents、事件转换
```

依赖规则：

- Web Adapter 可以依赖 Application。
- Application 只依赖自身领域类型以及 `AgentRuntime`、`ChatStore` 接口。
- AgentScope 类型只能存在于 AgentScope Adapter。
- MyBatis 类型只能存在于 Persistence Adapter。
- `ServerSentEvent`、HTTP Request/Response 只能存在于 Web Adapter。
- Domain 不依赖 Spring、AgentScope、MyBatis 或具体传输协议。
- 原始 AgentScope `AgentEvent` 不进入 Application、数据库实体或 HTTP DTO。

## 8. 建议目录结构

首期使用一个可运行的 Maven server 模块，通过 package 管理职责。避免在行为尚未增长前拆出大量浅层子模块。

```text
modules/module-agent-base/
├─ pom.xml
└─ module-agent-base-server/
   ├─ pom.xml
   └─ src/
      ├─ main/java/com/cloud/module/agentbase/
      │  ├─ AgentBaseApplication.java
      │  ├─ config/
      │  │  └─ AgentBaseProperties.java
      │  └─ chat/
      │     ├─ domain/
      │     │  ├─ Conversation.java
      │     │  ├─ AgentRun.java
      │     │  ├─ ChatMessage.java
      │     │  ├─ ChatEvent.java
      │     │  └─ RunStatus.java
      │     ├─ application/
      │     │  ├─ ChatApplication.java
      │     │  ├─ DefaultChatApplication.java
      │     │  ├─ StartChatCommand.java
      │     │  ├─ AgentRuntime.java
      │     │  └─ ChatStore.java
      │     ├─ web/
      │     │  ├─ ChatController.java
      │     │  ├─ ChatRequest.java
      │     │  ├─ ChatEventResponse.java
      │     │  └─ SseEventMapper.java
      │     ├─ agentscope/
      │     │  ├─ AgentScopeRuntime.java
      │     │  └─ AgentScopeEventMapper.java
      │     └─ persistence/
      │        ├─ MyBatisChatStore.java
      │        ├─ entity/
      │        └─ mapper/
      └─ test/java/com/cloud/module/agentbase/
         ├─ chat/application/
         ├─ chat/web/
         └─ support/FakeAgentRuntime.java
```

`module-agent-base` 是工作名称。若实施时确认最终直接替换旧模块，可以在切换阶段再统一命名，首期不要边重建边删除旧实现。

## 9. 核心接口

Application 对 Web 层提供一个深接口：

```java
public interface ChatApplication {
    Flux<ChatEvent> stream(StartChatCommand command);
}
```

Application 调用 Agent 运行时：

```java
public interface AgentRuntime {
    Flux<RuntimeEvent> stream(AgentInvocation invocation);
}
```

持久化模块隐藏会话、消息和 Run 的多表操作：

```java
public interface ChatStore {
    PreparedRun begin(StartChatCommand command);

    void complete(RunCompletion completion);

    void fail(RunFailure failure);

    void cancel(RunCancellation cancellation);
}
```

`AgentRuntime` 至少有 AgentScope 生产 Adapter 和测试 Fake；`ChatStore` 至少有 MyBatis 生产 Adapter 和测试内存 Adapter。这两个 seam 都服务于真实替换和测试需求。

不要在 `ChatApplication` 接口中出现 `LoginUser`、`AgentConfigDTO`、`HarnessAgent`、`AgentEvent` 或 `ServerSentEvent`。

## 10. HTTP 与 SSE 协议

### 10.1 请求

建议使用一个 POST 流式端点：

```http
POST /agent-base/chat/stream
Accept: text/event-stream
Content-Type: application/json
```

```json
{
  "sessionId": "可选，首次聊天不传",
  "requestId": "前端生成的 UUID",
  "content": "用户消息"
}
```

首次请求可以在 `ChatStore.begin()` 中自动创建会话。前端无需为了第一条消息预先调用“创建会话”。

### 10.2 首期事件

首期只公开四种业务事件：

```text
run.started
message.delta
run.completed
run.failed
```

可使用 SSE 注释作为心跳；心跳不进入业务事件协议。

事件数据示例：

```json
{
  "schemaVersion": 1,
  "sessionId": "193000000000000001",
  "runId": "194000000000000001",
  "sequence": 2,
  "type": "message.delta",
  "delta": "你好"
}
```

协议约束：

- HTTP DTO 中的数据库 ID 一律使用字符串，避免 JavaScript 大整数精度问题。
- `sequence` 在单个 Run 内从 1 开始严格递增。
- SSE `event` 字段与数据中的 `type` 必须一致。
- 每个 Run 只输出一个终止事件：`run.completed` 或 `run.failed`。
- 终止事件之后不再输出业务事件。
- HTTP 响应开始前的鉴权和参数错误使用正常 HTTP 状态码。
- HTTP 响应开始后的运行异常使用 `run.failed`，并返回经过脱敏的错误信息。
- 客户端主动取消通常不再强制发送终止事件，但必须将 Run 落为 `CANCELLED`。

## 11. 完整运行流程

1. Web Adapter 从安全上下文解析用户和租户，校验 `requestId`、`content`、`sessionId`。
2. Application 延迟执行 `ChatStore.begin()`。
3. `begin()` 在一个短事务中完成：
   - 根据用户和租户验证会话归属，或创建新会话。
   - 检查 `requestId` 幂等记录。
   - 检查同一会话是否已有活动 Run。
   - 保存用户消息。
   - 创建状态为 `RUNNING` 的 Run。
   - 返回 Run 信息和有序会话历史。
4. Application 根据历史消息构造框架无关的 `AgentInvocation`。
5. `AgentScopeRuntime` 构造最小 `HarnessAgent` 和 `RuntimeContext`。
6. `AgentScopeRuntime` 调用 `streamEvents()`。
7. `AgentScopeEventMapper` 只处理开始、文本增量、结束和异常相关事件。
8. Application 将 `RuntimeEvent` 转换为带单调序号的 `ChatEvent`，同时聚合最终 Agent 文本。
9. Web Adapter 将 `ChatEvent` 转为 SSE，UI 增量渲染 `message.delta`。
10. 正常完成时，在短事务中保存 Agent 消息并把 Run 更新为 `SUCCESS`。
11. AgentScope 异常时，把 Run 更新为 `FAILED`，然后输出 `run.failed`。
12. 浏览器关闭、Abort 或网络断开触发 Reactor cancel，上游 AgentScope Flux 被取消，Run 更新为 `CANCELLED`。

数据库事务不能贯穿 SSE 生命周期。开始和收尾分别使用独立短事务。

## 12. Reactor 生命周期要求

当前模块在 `doFinally` 中另行 `subscribe()` 执行异步收尾，新模块应让收尾成为主响应链的一部分。

推荐使用 `Flux.usingWhen` 或具有相同语义的组合方式，分别定义 complete、error 和 cancel 清理逻辑。需要满足：

- MyBatis 阻塞操作通过 `Mono.fromCallable` 或 `Mono.fromRunnable` 延迟执行。
- 阻塞操作调度到 `Schedulers.boundedElastic()`。
- 不在 Controller 返回 Flux 之前同步访问数据库。
- 不在业务代码中嵌套调用 `subscribe()`。
- cancel 信号能够到达 AgentScope 上游。
- 数据库收尾失败必须被记录，且不能泄漏密钥或完整模型响应。

## 13. AgentScope 首期装配

首期 `HarnessAgent` 只装配：

- Agent 名称。
- 系统提示词。
- OpenAI-compatible `Model`。
- `maxIters`。

不装配 Toolkit、SkillRepository、PermissionContext、StateStore、Compaction、Workspace、Filesystem、Plan 或 Subagent。

为了进程重启和水平扩展，首期优先采用“数据库消息历史 + 每次运行构造 Agent”的无状态方式，不依赖按会话缓存 `HarnessAgent` 保存记忆。构造开销需要在集成测试中测量；只有出现明确性能问题后才增加受控缓存。

建议配置：

```yaml
agent:
  base:
    name: assistant
    system-prompt: 你是一个企业智能助手
    max-iters: 8
    model:
      base-url: ${AGENT_MODEL_BASE_URL}
      api-key: ${AGENT_MODEL_API_KEY}
      model-name: ${AGENT_MODEL_NAME}
```

首期固定开启流式输出。超时和重试使用少量安全默认值；配置类启动时执行校验，缺少必要模型参数时直接启动失败。

Maven 依赖只保留当前工程运行必需项，重点包括：

- `agentscope-harness`
- `agentscope-extensions-model-openai`
- WebFlux/Reactor Netty
- Security
- Validation
- MyBatis 及数据库驱动

不从旧 `module-agent-server/pom.xml` 复制 Redis State、MySQL State、Nacos Skill、RAG、MQ、Excel、多模型 Provider 等依赖。

## 14. 实施开始时核对的决策

下列细节不改变整体方案，但在创建文件前应结合仓库现状核对一次：

1. 新 Maven 模块最终使用 `module-agent-base` 还是其他临时名称；默认使用 `module-agent-base`。
2. 是否复用现有 `ai_agent_session`、`ai_agent_message`、`ai_agent_run_log` 表；默认优先复用核心字段，若旧表约束阻碍独立运行，再新增最小表。
3. 第一阶段是复用现有 UI 解析器还是建立 `/agent-base` 对应的新前端调用文件；默认建立新调用文件，旧页面通过明确开关切换。
4. Gateway 当前是否会缓冲 SSE；必须通过真实请求验证，不能只依赖配置推断。

这些决策确认后记录在实施文档或必要的 ADR 中。本文档是实施方案，不代替领域词汇表或正式 ADR。

## 15. 测试与验收

### 15.1 Application 接口测试

使用 `FakeAgentRuntime` 和内存 `ChatStore` 验证：

- 三个文本增量按顺序转换为 `message.delta`。
- `sequence` 严格递增。
- 正常完成只产生一个 `run.completed`。
- AgentRuntime 异常只产生一个 `run.failed`，Run 状态为 `FAILED`。
- 订阅取消会调用取消收尾，Run 状态为 `CANCELLED`。
- 重复 `requestId` 不创建重复消息和 Run。
- 同一会话并发运行第二个请求时明确拒绝。

### 15.2 Web/SSE 集成测试

使用 `WebTestClient` 验证：

- 响应媒体类型为 `text/event-stream`。
- 未登录、非法参数和越权会话在流开始前返回正确 HTTP 错误。
- SSE `event` 和数据 `type` 一致。
- ID 被序列化为字符串。
- 文本增量无需等待整个模型响应即可到达客户端。

### 15.3 Persistence 集成测试

验证：

- `begin()` 的消息与 Run 创建具有原子性。
- `complete()` 同时保存 Agent 消息并更新 Run。
- `fail()` 和 `cancel()` 状态正确且可重复调用。
- 历史消息按会话内序号稳定加载。
- 用户和租户不能读取或运行其他人的会话。

### 15.4 真实链路验收

- UI 输入文本后能持续看到增量内容。
- AgentScope 异常时 UI 得到可理解的失败事件。
- UI 点击停止后模型请求被取消，Run 最终为 `CANCELLED`。
- 服务重启后可以继续已有会话。
- SSE 经过 Gateway 后没有被聚合成一次性响应。
- 日志中不出现 API Key、Authorization 或完整敏感请求头。

## 16. 实施里程碑

### 里程碑一：可测试的后端聊天骨架

实施内容：

1. 创建新 Maven 模块和最小依赖。
2. 创建配置类、领域类型和三个核心接口。
3. 实现 `DefaultChatApplication`。
4. 使用 `FakeAgentRuntime`、内存 `ChatStore` 写 Application 测试。

完成标准：成功、异常、取消、幂等和并发保护测试全部通过；此时可以尚未调用真实模型。

### 里程碑二：AgentScope 与持久化 Adapter

实施内容：

1. 实现最小 OpenAI-compatible Model 构造。
2. 实现 `AgentScopeRuntime` 和事件转换。
3. 实现 MyBatis `ChatStore` 和短事务。
4. 完成 AgentScope Adapter 和 Persistence 集成测试。

完成标准：真实模型可以通过 Application 接口流式返回文本，三类 Run 收尾均可在数据库验证。

### 里程碑三：HTTP/SSE 与 UI 贯通

实施内容：

1. 实现 Controller 和 SSE 映射。
2. 新增前端请求和事件解析类型。
3. 验证 AbortController、错误展示和首次会话创建。
4. 验证 Gateway 流式透传。

完成标准：UI 到模型再到 UI 的完整文本聊天链路稳定运行，首期验收项全部通过。

### 里程碑四：切换与清理

实施内容：

1. 使用明确开关把聊天页面切换到新端点。
2. 保留旧模块作为回退，观察新链路。
3. 稳定后再决定旧聊天代码的迁移或删除范围。

完成标准：新链路成为默认入口并有回退方案；旧模块的删除必须单独评估，不包含在首期基座实现中。

## 17. 首期完成定义

同时满足以下条件，才能认为“标准 Agent 聊天基座”完成：

- 新模块不依赖旧模块的 Tool、Skill、审计、知识库、Plan、子 Agent 实现。
- Web、Application、AgentScope、Persistence 之间没有框架类型泄漏。
- UI、Gateway、Controller、Application、AgentScope、数据库的完整链路可运行。
- 成功、失败、取消、幂等和同会话并发均有自动化测试。
- 多轮会话在进程重启后可恢复。
- SSE 事件协议稳定、ID 安全、终止语义唯一。
- 阻塞数据库操作不占用 Reactor 事件循环。
- 主响应链中没有嵌套 `subscribe()`。
- 必要配置缺失时启动失败，日志不泄漏敏感信息。

## 18. 后续扩展原则

首期稳定后按真实需求逐项扩展：

- Tool 和 Skill 注册属于 AgentScope 装配，不进入 Controller 或聊天协议编排。
- 审计属于运行事件的旁路消费和持久化，不阻塞文本增量输出。
- 多模型属于配置解析和 Model Adapter，不改变 `ChatApplication` 接口。
- 新事件先形成明确业务语义，再扩展 `ChatEvent`，不直接把 AgentScope 原始事件暴露给 UI。
- 只有出现第二个真实实现或测试替身时才增加新的接口 seam。
- 每次扩展都必须继续通过首期聊天基座的契约测试。

核心目标始终不变：调用者只需要提交一次聊天命令并消费统一事件流，AgentScope 和后续扩展的复杂度留在模块实现内部。
