package com.cloud.module.agent.chat.runtime.definition;

import com.cloud.module.agent.chat.domain.AgentKind;
import lombok.Getter;

import java.util.Objects;

/** 单次运行所需的完整 Agent 定义。 */
@Getter
public final class AgentDefinition {

    private final Long agentId;
    private final String agentName;
    private final AgentKind kind;
    private final String systemPrompt;
    private final int maxIters;
    private final AgentModelDefinition model;

    public AgentDefinition(
            Long agentId,
            String agentName,
            String systemPrompt,
            int maxIters,
            AgentModelDefinition model
    ) {
        this(agentId, agentName, AgentKind.CHAT, systemPrompt, maxIters, model);
    }

    public AgentDefinition(
            Long agentId,
            String agentName,
            AgentKind kind,
            String systemPrompt,
            int maxIters,
            AgentModelDefinition model
    ) {
        this.agentId = requirePositive(agentId, "agentId");
        this.agentName = requireText(agentName, "agentName");
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
        this.systemPrompt = requireText(systemPrompt, "systemPrompt");
        if (maxIters <= 0) {
            throw new IllegalArgumentException("maxIters must be greater than 0");
        }
        this.maxIters = maxIters;
        this.model = Objects.requireNonNull(model, "model must not be null");
    }

    private static Long requirePositive(Long value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.strip();
    }
}
