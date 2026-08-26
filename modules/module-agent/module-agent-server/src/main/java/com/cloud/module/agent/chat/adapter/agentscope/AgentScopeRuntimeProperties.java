package com.cloud.module.agent.chat.adapter.agentscope;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * AgentScope 聊天运行时配置。
 *
 * <p>该配置类不使用 {@code @Data}，避免生成包含 API Key 的 toString。
 * {@link #validateForActivation()} 只在 AgentScope Adapter 被激活时调用，因而
 * Echo 模式不需要提供真实模型参数。</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "agent.runtime.agentscope")
public class AgentScopeRuntimeProperties {

    /** 业务 Agent 标识，同时写入新建 Session 并作为 AgentScope agentId。 */
    private Long agentId = 111222333L;

    /** 模型消息中的 Agent 显示名称。 */
    private String agentName = "BaseChatAgent";

    /** 每次调用都会注入的基础系统提示词。 */
    private String systemPrompt = "你是一个专业、准确、简洁的企业级智能助手。";

    /**
     * 当前不启用工具，一次模型调用即可完成回答。
     */
    private int maxIters = 30;

    /** OpenAI-compatible 模型连接和执行参数。 */
    private ModelProperties model = new ModelProperties();

    /**
     * 在创建外部模型之前集中校验配置，尽早暴露部署错误且不打印密钥。
     */
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

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "agent.runtime.agentscope." + fieldName + " 不能为空"
            );
        }
    }

    /** OpenAI-compatible 模型的最小配置集合。 */
    @Getter
    @Setter
    public static class ModelProperties {

        /** 兼容接口根地址，通常以 /v1 结尾。 */
        private String baseUrl = "https://api.openai.com/v1";

        /**
         * 某些本地 OpenAI-compatible 服务可以不需要 API Key，
         * 因此这里不强制非空。
         */
        private String apiKey;

        /** 供应商识别的模型名称。 */
        private String modelName;

        /** 单次模型回答允许生成的最大 Token 数。 */
        private Integer maxTokens = 4096;

        /** 单次模型请求超时。 */
        private Duration timeout = Duration.ofMinutes(2);

        /** 包含首次请求在内的最大尝试次数。 */
        private Integer maxAttempts = 10;
    }
}
