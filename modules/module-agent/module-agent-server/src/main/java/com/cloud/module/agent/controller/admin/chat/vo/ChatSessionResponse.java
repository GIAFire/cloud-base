package com.cloud.module.agent.controller.admin.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/** 管理后台 - 会话列表项。 */
@Schema(description = "管理后台 - 会话列表项")
@Getter
@AllArgsConstructor
public final class ChatSessionResponse {

    private final String id;
    private final String agentId;
    private final String title;
    private final String lastMessageSeq;
    private final LocalDateTime lastMessageAt;
}
