package com.cloud.module.agentruntime.chat.application.command;

import com.cloud.module.agentruntime.chat.domain.ChatActor;
import lombok.Getter;

import java.util.Objects;

@Getter
public class StartChatCommand {

    private final Long sessionId;
    private final String requestId;
    private final String content;
    private final ChatActor actor;

    public StartChatCommand(Long sessionId, String requestId, String content, ChatActor actor) {
        this.sessionId = sessionId;
        this.requestId = Objects.requireNonNull(requestId, "requestId must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.actor = Objects.requireNonNull(actor, "actor must not be null");
    }
}
