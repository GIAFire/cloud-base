package com.cloud.module.agent.controller.admin.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SSE data 字段结构。
 *
 * <p>该 DTO 是 ChatEvent 的传输投影，不包含 AgentScope 或数据库对象。字段
 * 是否为空由事件类型决定：消息事件携带 delta，失败事件携带 error 字段。</p>
 */
@Getter
@AllArgsConstructor
public final class ChatStreamEventResponse {

    /** 响应结构版本，便于前端兼容演进。 */
    private final int schemaVersion;

    /**
     * HTTP 响应中使用 String，避免 JavaScript Long 精度丢失。
     */
    private final String sessionId;

    /** String 形式的 Run ID，避免 JavaScript Long 精度丢失。 */
    private final String runId;

    /** Run 内严格递增的事件序号。 */
    private final long sequence;

    /** 稳定协议值，例如 run.started、message.delta。 */
    private final String type;

    /** Thinking 或最终正文增量，生命周期事件为空。 */
    private final String delta;

    /** 失败事件的安全错误码。 */
    private final String errorCode;

    /** 失败事件的已脱敏错误文案。 */
    private final String errorMessage;
}
