package com.cloud.module.agent.chat.runtime;

import lombok.Getter;

import java.util.Objects;

/**
 * AgentRuntime 发布的不可变框架中立事件。
 *
 * <p>STARTED/COMPLETED 不携带 delta；THINKING_DELTA/TEXT_DELTA 必须携带
 * 非空增量。事件顺序由 AgentRuntime 接口和应用层双重校验。</p>
 */
@Getter
public final class RuntimeEvent {

    /** 最小运行时事件类型。 */
    private final RuntimeEventType type;

    /** Thinking 或最终正文增量；生命周期事件为空。 */
    private final String delta;

    private RuntimeEvent(RuntimeEventType type, String delta) {
        this.type = Objects.requireNonNull(type, "type 不能为空");
        this.delta = delta;
    }

    public static RuntimeEvent started() {
        return new RuntimeEvent(RuntimeEventType.STARTED, null);
    }

    public static RuntimeEvent thinkingDelta(String delta) {
        requireDelta(delta);

        return new RuntimeEvent(RuntimeEventType.THINKING_DELTA, delta);
    }

    public static RuntimeEvent textDelta(String delta) {
        requireDelta(delta);

        return new RuntimeEvent(RuntimeEventType.TEXT_DELTA, delta);
    }

    public static RuntimeEvent completed() {
        return new RuntimeEvent(RuntimeEventType.COMPLETED,null);
    }

    private static void requireDelta(String delta) {
        if (delta == null || delta.isEmpty()) {
            throw new IllegalArgumentException("delta 不能为空");
        }
    }
}
