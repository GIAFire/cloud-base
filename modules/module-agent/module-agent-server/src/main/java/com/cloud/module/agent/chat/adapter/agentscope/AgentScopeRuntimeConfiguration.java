package com.cloud.module.agent.chat.adapter.agentscope;

import com.cloud.module.agent.chat.runtime.AgentRuntime;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * AgentScope Runtime 的条件装配。
 *
 * <p>仅当 {@code agent.runtime.type=agentscope} 时创建模型、单例 HarnessAgent
 * 和 AgentRuntime Adapter，避免与开发用 EchoAgentRuntime 同时存在。</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "agent.runtime",
        name = "type",
        havingValue = "agentscope"
)
public class AgentScopeRuntimeConfiguration {

    /**
     * 创建 OpenAI-compatible 流式模型。
     *
     * <p>配置只在 Adapter 激活时校验；API Key 允许为空，以兼容不要求鉴权的
     * 本地兼容服务。超时和重试由 AgentScope ExecutionConfig 执行。</p>
     */
    @Bean
    public Model agentScopeChatModel(AgentScopeRuntimeProperties properties) {
        properties.validateForActivation();

        AgentScopeRuntimeProperties.ModelProperties modelProperties = properties.getModel();

        ExecutionConfig executionConfig = ExecutionConfig.builder()
                        .timeout(modelProperties.getTimeout())
                        .maxAttempts(modelProperties.getMaxAttempts())
                        .build();

        GenerateOptions generateOptions = GenerateOptions.builder()
                        .stream(true)
                        .maxTokens(modelProperties.getMaxTokens())
                        .executionConfig(executionConfig)
                        .build();

        OpenAIChatModel.Builder builder = OpenAIChatModel.builder()
                        .baseUrl(modelProperties.getBaseUrl())
                        .modelName(modelProperties.getModelName())
                        .stream(true)
                        .generateOptions(generateOptions);

        if (modelProperties.getApiKey() != null
                && !modelProperties.getApiKey().isBlank()) {
            builder.apiKey(modelProperties.getApiKey());
        }

        return builder.build();
    }

    /**
     * HarnessAgent 必须是单例。
     *
     * 同一个 HarnessAgent 根据 RuntimeContext 中的 userId、sessionId
     * 管理不同会话的 AgentState。
     *
     * destroyMethod 只会在 Spring 容器关闭时执行，
     * 不能在单次聊天结束时关闭。
     */
    @Bean(destroyMethod = "close")
    public HarnessAgent baseHarnessAgent(
            @Qualifier("agentScopeChatModel") Model model,
            AgentScopeRuntimeProperties properties
    ) {
        return HarnessAgent.builder()
                .agentId(String.valueOf(properties.getAgentId()))
                .name(properties.getAgentName())
                .sysPrompt(properties.getSystemPrompt())
                .model(model)
                .maxIters(properties.getMaxIters())
                .enableAgentTracingLog(false)
                .build();
    }

    /** 将容器管理的 HarnessAgent 包装为模块内部 AgentRuntime seam。 */
    @Bean
    public AgentRuntime agentScopeRuntime(HarnessAgent baseHarnessAgent) {
        return new AgentScopeRuntime(baseHarnessAgent);
    }
}
