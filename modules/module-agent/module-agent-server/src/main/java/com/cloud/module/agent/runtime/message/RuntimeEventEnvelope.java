package com.cloud.module.agent.runtime.message;

import com.cloud.module.agent.event.AgentRuntimeEvent;

import java.util.Map;

public record RuntimeEventEnvelope(
        AgentRuntimeEvent runtimeEvent,
        Map<String, Object> planPayload
) {
}
