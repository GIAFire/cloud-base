package com.cloud.module.agent.controller.admin.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/** 管理后台 - 已持久化聊天消息。 */
@Schema(description = "管理后台 - 已持久化聊天消息")
@Getter
@AllArgsConstructor
public final class ChatMessageResponse {

    private final String id;
    private final String sessionId;
    private final String runId;
    private final String seq;
    private final String role;
    private final String messageType;
    private final String content;
    private final LocalDateTime createTime;
}
