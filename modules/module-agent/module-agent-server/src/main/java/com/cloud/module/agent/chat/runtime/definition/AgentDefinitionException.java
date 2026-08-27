package com.cloud.module.agent.chat.runtime.definition;

import lombok.Getter;

/** Agent 配置缺失、不完整或当前运行时不支持时抛出的类型化异常。 */
@Getter
public final class AgentDefinitionException extends RuntimeException {

    private final Reason reason;
    private final Long agentId;

    private AgentDefinitionException(Reason reason, Long agentId, String message) {
        super(message, null, false, false);
        this.reason = reason;
        this.agentId = agentId;
    }

    public static AgentDefinitionException agentNotFound(Long agentId) {
        return new AgentDefinitionException(
                Reason.AGENT_NOT_FOUND,
                agentId,
                "Agent definition not found: " + agentId
        );
    }

    public static AgentDefinitionException modelReferenceMissing(Long agentId) {
        return new AgentDefinitionException(
                Reason.MODEL_REFERENCE_MISSING,
                agentId,
                "Agent model reference is missing: " + agentId
        );
    }

    public static AgentDefinitionException modelNotFound(Long agentId) {
        return new AgentDefinitionException(
                Reason.MODEL_NOT_FOUND,
                agentId,
                "Agent model definition not found: " + agentId
        );
    }

    public static AgentDefinitionException promptReferenceMissing(Long agentId) {
        return new AgentDefinitionException(
                Reason.PROMPT_REFERENCE_MISSING,
                agentId,
                "Agent system prompt reference is missing: " + agentId
        );
    }

    public static AgentDefinitionException promptNotFound(Long agentId) {
        return new AgentDefinitionException(
                Reason.PROMPT_NOT_FOUND,
                agentId,
                "Agent system prompt not found: " + agentId
        );
    }

    public static AgentDefinitionException invalidConfiguration(Long agentId) {
        return new AgentDefinitionException(
                Reason.INVALID_CONFIGURATION,
                agentId,
                "Agent definition is invalid: " + agentId
        );
    }

    public static AgentDefinitionException unsupportedProtocol(Long agentId) {
        return new AgentDefinitionException(
                Reason.UNSUPPORTED_PROTOCOL,
                agentId,
                "Agent model protocol is unsupported: " + agentId
        );
    }

    public enum Reason {
        AGENT_NOT_FOUND,
        MODEL_REFERENCE_MISSING,
        MODEL_NOT_FOUND,
        PROMPT_REFERENCE_MISSING,
        PROMPT_NOT_FOUND,
        INVALID_CONFIGURATION,
        UNSUPPORTED_PROTOCOL
    }
}
