package com.cloud.module.agent.chat.domain;

import lombok.Getter;

import java.util.Objects;

/**
 * 与具体存储无关的完整聊天消息值对象。
 *
 * <p>当前主要供内存 ChatStore 保存已完成消息；它表示完整消息而不是流式
 * delta，也不承担 AgentScope 上下文序列化。</p>
 */
@Getter
public final class ChatMessage {

    /** 消息发送方。 */
    private final ChatRole role;

    /** 完整文本内容。 */
    private final String content;

    public ChatMessage(ChatRole role, String content) {
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
    }
}
