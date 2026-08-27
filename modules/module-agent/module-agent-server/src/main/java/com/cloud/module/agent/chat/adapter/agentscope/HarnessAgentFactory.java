package com.cloud.module.agent.chat.adapter.agentscope;

import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinition;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinitionException;
import com.cloud.module.agent.chat.runtime.definition.AgentModelDefinition;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.state.JsonFileAgentStateStore;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/** 根据一次运行的数据库定义创建独立 HarnessAgent。 */
public final class HarnessAgentFactory {

    private final AgentRuntimeProperties properties;

    public HarnessAgentFactory(AgentRuntimeProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties 不能为空");
    }

    public HarnessAgent create(AgentDefinition definition, ChatActor actor) {
        Objects.requireNonNull(definition, "definition must not be null");
        Objects.requireNonNull(actor, "actor must not be null");

        Model model = createModel(definition);
        Path stateDirectory = properties.getStateRoot()
                .resolve(String.valueOf(actor.getTenantId()))
                .resolve(String.valueOf(definition.getAgentId()));

        return HarnessAgent.builder()
                .agentId(String.valueOf(definition.getAgentId()))
                .name(definition.getAgentName())
                .sysPrompt(definition.getSystemPrompt())
                .model(model)
                // 传输重试由 GenerateOptions 控制，避免 Harness 再嵌套重试。
                .modelExecutionConfig(ExecutionConfig.builder().maxAttempts(1).build())
                .maxIters(definition.getMaxIters())
                .stateStore(new JsonFileAgentStateStore(stateDirectory))
                .enableAgentTracingLog(false)
                .disableFilesystemTools()
                .disableShellTool()
                .disableToolsConfig()
                .disableDynamicSkills()
                .disableDefaultWorkspaceSkills()
                .disableSubagents()
                .disableDynamicSubagents()
                .disableMemoryHooks()
                .disableAtPathExpansion()
                .disableCompaction()
                .disableToolResultEviction()
                .build();
    }

    private Model createModel(AgentDefinition definition) {
        AgentModelDefinition modelDefinition = definition.getModel();
        if (!isOpenAiCompatible(modelDefinition.getProtocol())) {
            throw AgentDefinitionException.unsupportedProtocol(definition.getAgentId());
        }

        ExecutionConfig executionConfig = ExecutionConfig.builder()
                .timeout(properties.getModelTimeout())
                .maxAttempts(properties.getModelMaxAttempts())
                .build();

        GenerateOptions.Builder optionsBuilder = GenerateOptions.builder()
                .stream(true)
                .maxTokens(modelDefinition.getMaxTokens())
                .executionConfig(executionConfig);

        if (modelDefinition.getTemperature() != null) {
            optionsBuilder.temperature(modelDefinition.getTemperature().doubleValue());
        }
        if (modelDefinition.getTopP() != null) {
            optionsBuilder.topP(modelDefinition.getTopP().doubleValue());
        }
        if (modelDefinition.getThinkingBudget() != null) {
            optionsBuilder.thinkingBudget(modelDefinition.getThinkingBudget());
        }

        OpenAIChatModel.Builder modelBuilder = OpenAIChatModel.builder()
                .modelName(modelDefinition.getModelName())
                .stream(true)
                .generateOptions(optionsBuilder.build());

        if (modelDefinition.getBaseUrl() != null) {
            modelBuilder.baseUrl(modelDefinition.getBaseUrl());
        }
        if (modelDefinition.getApiKey() != null) {
            modelBuilder.apiKey(modelDefinition.getApiKey());
        }
        return modelBuilder.build();
    }

    private boolean isOpenAiCompatible(String protocol) {
        String normalized = protocol.replace("-", "")
                .replace("_", "")
                .toLowerCase(Locale.ROOT);
        return "openaicompatible".equals(normalized);
    }
}
