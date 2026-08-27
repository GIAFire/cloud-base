package com.cloud.module.agent.chat.adapter.agentscope;

import com.cloud.module.agent.chat.runtime.AgentRuntime;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.state.JsonFileAgentStateStore;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.MemoryConfig;
import io.agentscope.harness.agent.memory.MemoryConsolidator;
import io.agentscope.harness.agent.memory.MemoryFlushManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * AgentScope Runtime 的条件装配。
 *
 * <p>仅当 {@code agent.runtime.type=agentscope} 时创建模型、单例 HarnessAgent
 * 和 AgentRuntime Adapter，避免与开发用 EchoAgentRuntime 同时存在。</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "agent",
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
        AgentStateStore stateStore = new TimingAgentStateStore(
                new JsonFileAgentStateStore(defaultStateDirectory(properties.getAgentId()))
        );

        /*
         * Harness 的长期记忆刷新与维护直接调用 Model，不发布主 Agent 的
         * MODEL_CALL_START/END 事件。给记忆管线单独安装模型装饰器，既保持
         * 默认记忆行为不变，也能把隐藏模型请求从总耗时中拆出来。
         */
//        MemoryConfig defaultMemoryConfig = MemoryConfig.defaults();
//        String flushPrompt = MemoryFlushManager.DEFAULT_FLUSH_PROMPT;
//        String maintenancePrompt = String.format(
//                MemoryConsolidator.DEFAULT_CONSOLIDATION_PROMPT,
//                defaultMemoryConfig.consolidationMaxTokens(),
//                defaultMemoryConfig.consolidationMaxTokens() * 4
//        );
//        Model timingMemoryModel = new TimingMemoryModel(
//                model,
//                flushPrompt,
//                maintenancePrompt
//        );
//        MemoryConfig timingMemoryConfig = MemoryConfig.builder()
//                .model(timingMemoryModel)
//                .build();

        return HarnessAgent.builder()
                .agentId(String.valueOf(properties.getAgentId()))
                .name(properties.getAgentName())
                .sysPrompt(properties.getSystemPrompt())
                .model(model)
                .modelExecutionConfig(ExecutionConfig.builder().maxAttempts(1).build())
                .maxIters(properties.getMaxIters())
                .enableAgentTracingLog(false)
//                .memory(timingMemoryConfig)
//                .stateStore(stateStore)

                .disableFilesystemTools()
                .disableShellTool()
                .disableToolsConfig()

                .disableDynamicSkills()
                .disableDefaultWorkspaceSkills()

                .disableSubagents()
                .disableDynamicSubagents()

//                .disableMemoryTools()
//                .disableMemoryHooks()

//                .disableWorkspaceContext()
                .disableAtPathExpansion()

                .disableCompaction()
                .disableToolResultEviction()
                .build();
    }

    /**
     * 复用 HarnessAgent 默认的 StateStore 目录规则。
     *
     * <p>显式创建存储只是为了安装耗时装饰器；未配置覆盖项时仍然写入
     * {@code ~/.agentscope/state/{agentId}}，不会迁移或丢失现有会话状态。</p>
     */
    private Path defaultStateDirectory(Long agentId) {
        String override = System.getProperty("agentscope.state.home");
        Path root = override != null && !override.isBlank()
                ? Paths.get(override)
                : Paths.get(System.getProperty("user.home"), ".agentscope", "state");
        return root.resolve(String.valueOf(agentId));
    }

    /** 将容器管理的 HarnessAgent 包装为模块内部 AgentRuntime seam。 */
    @Bean
    public AgentRuntime agentScopeRuntime(HarnessAgent baseHarnessAgent) {
        return new AgentScopeRuntime(baseHarnessAgent);
    }
}
