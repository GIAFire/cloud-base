package com.cloud.module.agentruntime.chat.adapter.agentscope;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "agent.runtime.agentscope")
public class AgentScopeRuntimeProperties {

    private Long agentId = 111222333L;

    private String agentName = "BaseChatAgent";

    private String systemPrompt = "你是一个专业、准确、简洁的企业级智能助手。";

    /**
     * 当前不启用工具，一次模型调用即可完成回答。
     */
    private int maxIters = 30;

    private ModelProperties model = new ModelProperties();

    public void validateForActivation() {
        if (agentId == null || agentId <= 0) {
            throw new IllegalArgumentException(
                    "agent.runtime.agentscope.agent-id must be greater than 0"
            );
        }
        requireText(agentName, "agentName");
        requireText(systemPrompt, "systemPrompt");

        if (maxIters < 1) {
            throw new IllegalArgumentException(
                    "agent.runtime.agentscope.max-iters must be greater than 0"
            );
        }

        if (model == null) {
            throw new IllegalArgumentException(
                    "agent.runtime.agentscope.model must not be null"
            );
        }

        requireText(model.getBaseUrl(), "model.baseUrl");
        requireText(model.getModelName(), "model.modelName");

        if (model.getMaxTokens() == null
                || model.getMaxTokens() < 1) {
            throw new IllegalArgumentException(
                    "agent.runtime.agentscope.model.max-tokens must be greater than 0"
            );
        }

        if (model.getTimeout() == null
                || model.getTimeout().isZero()
                || model.getTimeout().isNegative()) {
            throw new IllegalArgumentException(
                    "agent.runtime.agentscope.model.timeout must be greater than 0"
            );
        }

        if (model.getMaxAttempts() == null
                || model.getMaxAttempts() < 1) {
            throw new IllegalArgumentException(
                    "agent.runtime.agentscope.model.max-attempts must be greater than 0"
            );
        }
    }

    private static void requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "agent.runtime.agentscope."
                            + fieldName
                            + " must not be blank"
            );
        }
    }

    @Getter
    @Setter
    public static class ModelProperties {

        private String baseUrl =
                "https://api.openai.com/v1";

        /**
         * 某些本地 OpenAI-compatible 服务可以不需要 API Key，
         * 因此这里不强制非空。
         */
        private String apiKey;

        private String modelName;

        private Integer maxTokens = 4096;

        private Duration timeout = Duration.ofMinutes(2);

        private Integer maxAttempts = 10;
    }
}