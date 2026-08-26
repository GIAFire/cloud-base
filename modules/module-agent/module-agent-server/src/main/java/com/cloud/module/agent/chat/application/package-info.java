/**
 * 聊天应用编排层。
 *
 * <p>该包不感知 HTTP、AgentScope 或 MyBatis 类型，只通过 AgentRuntime 和
 * ChatStore 两个接口协调运行。这里同时维护运行时事件协议、SSE 事件顺序和
 * 成功/失败/取消的可靠收尾语义。</p>
 */
package com.cloud.module.agent.chat.application;
