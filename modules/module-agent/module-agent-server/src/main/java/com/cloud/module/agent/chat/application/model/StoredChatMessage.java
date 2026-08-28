package com.cloud.module.agent.chat.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/** 已持久化业务消息的读模型。 */
@Getter
@AllArgsConstructor
public final class StoredChatMessage {

    private final Long id;
    private final Long sessionId;
    private final Long runId;
    private final Long seq;
    private final String role;
    private final String messageType;
    private final String content;
    private final LocalDateTime createTime;
}
