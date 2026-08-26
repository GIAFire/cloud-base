/**
 * Agent 运行时 seam。
 *
 * <p>应用层只认识 AgentInvocation、RuntimeEvent 和 AgentRuntime；具体框架
 * 产生的事件必须由 Adapter 转换后才能穿过该 seam。</p>
 */
package com.cloud.module.agent.chat.runtime;
