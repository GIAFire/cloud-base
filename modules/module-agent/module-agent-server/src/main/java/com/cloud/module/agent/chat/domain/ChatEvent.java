package com.cloud.module.agent.chat.domain;

import lombok.Getter;

import java.util.Objects;

/**
 * 应用层发布给 Web Adapter 的不可变业务事件。
 *
 * <p>同一个 Run 内 sequence 从 1 严格递增，前端可以用
 * {@code runId:sequence} 去重并恢复顺序。delta 只允许出现在消息增量事件，
 * errorCode/errorMessage 只允许出现在 run.failed。</p>
 */
@Getter
public final class ChatEvent {

    /** SSE data 结构版本，与事件类型版本分离。 */
    private static final int CURRENT_SCHEMA_VERSION = 1;

    /** 响应数据结构版本。 */
    private final int schemaVersion;
    /** 业务会话标识。 */
    private final Long sessionId;
    /** 单次运行标识。 */
    private final Long runId;
    /** Run 内单调递增的事件序号。 */
    private final long sequence;
    /** 业务事件类型。 */
    private final ChatEventType type;
    /** Thinking 或正文的单个文本增量。 */
    private final String delta;
    /** 面向客户端的安全错误码。 */
    private final String errorCode;
    /** 面向客户端的已脱敏错误信息。 */
    private final String errorMessage;

    private ChatEvent(
            Long sessionId,
            Long runId,
            long sequence,
            ChatEventType type,
            String delta,
            String errorCode,
            String errorMessage
    ) {
        this.schemaVersion = CURRENT_SCHEMA_VERSION;
        this.sessionId = requirePositive(sessionId, "sessionId");
        this.runId = requirePositive(runId, "runId");

        if (sequence <= 0) {
            throw new IllegalArgumentException("sequence must be greater than 0");
        }

        this.sequence = sequence;
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.delta = delta;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static ChatEvent runStarted(Long sessionId, Long runId, long sequence) {
        return new ChatEvent(
                sessionId,
                runId,
                sequence,
                ChatEventType.RUN_STARTED,
                null,
                null,
                null
        );
    }

    public static ChatEvent messageThinkingDelta(
            Long sessionId,
            Long runId,
            long sequence,
            String delta
    ) {
        if (delta == null || delta.isEmpty()) {
            throw new IllegalArgumentException(
                    "delta must not be null or empty"
            );
        }

        return new ChatEvent(
                sessionId,
                runId,
                sequence,
                ChatEventType.MESSAGE_THINKING_DELTA,
                delta,
                null,
                null
        );
    }

    public static ChatEvent messageDelta(
            Long sessionId,
            Long runId,
            long sequence,
            String delta
    ) {
        if (delta == null || delta.isEmpty()) {
            throw new IllegalArgumentException("delta must not be null or empty");
        }

        return new ChatEvent(
                sessionId,
                runId,
                sequence,
                ChatEventType.MESSAGE_DELTA,
                delta,
                null,
                null
        );
    }

    public static ChatEvent runCompleted(
            Long sessionId,
            Long runId,
            long sequence
    ) {
        return new ChatEvent(
                sessionId,
                runId,
                sequence,
                ChatEventType.RUN_COMPLETED,
                null,
                null,
                null
        );
    }

    public static ChatEvent runFailed(
            Long sessionId,
            Long runId,
            long sequence,
            String errorCode,
            String errorMessage
    ) {
        requireText(errorCode, "errorCode");
        requireText(errorMessage, "errorMessage");

        return new ChatEvent(
                sessionId,
                runId,
                sequence,
                ChatEventType.RUN_FAILED,
                null,
                errorCode,
                errorMessage
        );
    }

    private static Long requirePositive(Long value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }

        return value;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }
}
