/**
 * Agent 聊天基座的核心模块。
 *
 * <p>一次聊天请求按照以下顺序运行：</p>
 *
 * <ol>
 *     <li>Web Adapter 将 HTTP 请求转换为应用命令；</li>
 *     <li>应用层通过 ChatStore 原子创建 Session/Run 并保存用户消息；</li>
 *     <li>应用层通过 AgentRuntime 启动 HarnessAgent 事件流；</li>
 *     <li>运行时事件被转换成稳定的 ChatEvent，并通过 SSE 发送给客户端；</li>
 *     <li>成功、失败或取消都会通过 ChatStore 完成最终收尾。</li>
 * </ol>
 *
 * <p>该模块包含两个刻意保留的 seam：</p>
 *
 * <ul>
 *     <li>AgentRuntime：隔离 AgentScope 等具体 Agent 框架；</li>
 *     <li>ChatStore：隔离内存、MySQL 等业务状态存储实现。</li>
 * </ul>
 *
 * <p>AgentScope StateStore 保存模型对话上下文；agent_session、agent_run、
 * agent_message 保存业务会话、运行状态和最终消息。两者职责不同，业务消息
 * 不会在本链路中重新拼装后传给 HarnessAgent。</p>
 */
package com.cloud.module.agent.chat;
