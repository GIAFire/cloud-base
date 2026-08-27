package com.cloud.module.agent.chat.runtime.definition;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Agent 使用的模型定义。
 *
 * <p>该对象属于模块内部运行协议，不暴露数据库 DO 或 AgentScope 类型。
 * API Key 不参与 {@code toString}，防止被日志意外输出。</p>
 */
@Getter
public final class AgentModelDefinition {

    private final String protocol;
    private final String baseUrl;
    private final String apiKey;
    private final String modelName;
    private final boolean streaming;
    private final BigDecimal temperature;
    private final BigDecimal topP;
    private final Integer maxTokens;
    private final Integer thinkingBudget;

    public AgentModelDefinition(
            String protocol,
            String baseUrl,
            String apiKey,
            String modelName,
            boolean streaming,
            BigDecimal temperature,
            BigDecimal topP,
            Integer maxTokens,
            Integer thinkingBudget
    ) {
        this.protocol = requireText(protocol, "protocol");
        this.baseUrl = normalizeOptionalText(baseUrl);
        this.apiKey = normalizeOptionalText(apiKey);
        this.modelName = requireText(modelName, "modelName");
        this.streaming = streaming;
        this.temperature = temperature;
        this.topP = topP;
        this.maxTokens = requirePositive(maxTokens, "maxTokens");
        this.thinkingBudget = requireOptionalPositive(thinkingBudget, "thinkingBudget");

        if (!streaming) {
            throw new IllegalArgumentException("streaming must be enabled");
        }
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.strip();
    }

    private static String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static Integer requirePositive(Integer value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private static Integer requireOptionalPositive(Integer value, String fieldName) {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
