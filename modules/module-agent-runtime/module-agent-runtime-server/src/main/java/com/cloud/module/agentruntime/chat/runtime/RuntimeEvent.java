package com.cloud.module.agentruntime.chat.runtime;

import lombok.Getter;

import java.util.Objects;

@Getter
public final class RuntimeEvent {

    private final RuntimeEventType type;
    private final String delta;

    private RuntimeEvent(
            RuntimeEventType type,
            String delta
    ) {
        this.type = Objects.requireNonNull(
                type,
                "type must not be null"
        );
        this.delta = delta;
    }

    public static RuntimeEvent started() {
        return new RuntimeEvent(
                RuntimeEventType.STARTED,
                null
        );
    }

    public static RuntimeEvent textDelta(String delta) {
        if (delta == null || delta.isEmpty()) {
            throw new IllegalArgumentException(
                    "delta must not be null or empty"
            );
        }

        return new RuntimeEvent(
                RuntimeEventType.TEXT_DELTA,
                delta
        );
    }

    public static RuntimeEvent completed() {
        return new RuntimeEvent(
                RuntimeEventType.COMPLETED,
                null
        );
    }
}