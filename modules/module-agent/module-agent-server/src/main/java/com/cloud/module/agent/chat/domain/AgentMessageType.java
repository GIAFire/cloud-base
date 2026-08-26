package com.cloud.module.agent.chat.domain;

/**
 * 聊天基座持久化的业务消息类型。
 *
 * <p>流式 delta 不逐条写库；USER_TEXT 在 begin 事务保存，ASSISTANT_TEXT
 * 在 SUCCEEDED 的 finish 事务保存。Thinking 暂时只通过 SSE 展示。</p>
 */
public enum AgentMessageType {

    /** 用户提交的完整文本。 */
    USER_TEXT,

    /** Agent 成功完成后聚合得到的完整正文。 */
    ASSISTANT_TEXT
}
