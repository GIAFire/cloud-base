package com.cloud.module.agent.chat.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/** 会话列表中的稳定读模型。 */
@Getter
@AllArgsConstructor
public final class ChatSessionSummary {

    private final Long id;
    private final Long agentId;
    private final String title;
    private final Long lastMessageSeq;
    private final LocalDateTime lastMessageAt;
}
