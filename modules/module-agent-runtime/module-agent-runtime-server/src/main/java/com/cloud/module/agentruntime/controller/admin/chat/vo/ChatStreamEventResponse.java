package com.cloud.module.agentruntime.controller.admin.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class ChatStreamEventResponse {

    private final int schemaVersion;

    /**
     * HTTP 响应中使用 String，避免 JavaScript Long 精度丢失。
     */
    private final String sessionId;

    private final String runId;

    private final long sequence;

    private final String type;

    private final String delta;

    private final String errorCode;

    private final String errorMessage;
}