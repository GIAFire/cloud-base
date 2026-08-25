package com.cloud.module.agentruntime.chat.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public final class ChatMessage {

    private final ChatRole role;
    private final String content;

    public ChatMessage(ChatRole role, String content) {
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
    }
}