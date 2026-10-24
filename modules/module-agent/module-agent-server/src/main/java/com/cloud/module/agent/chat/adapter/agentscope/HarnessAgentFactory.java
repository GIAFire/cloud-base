package com.cloud.module.agent.chat.adapter.agentscope;

import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinition;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinitionException;
import com.cloud.module.agent.chat.runtime.definition.AgentModelDefinition;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;

import java.nio.file.Path;
import java.util.Objects;

/** 根据一次运行的数据库定义创建独立 HarnessAgent。 */
public final class HarnessAgentFactory {

    private final AgentRuntimeProperties properties;
    private final Path workspaceRoot;

    public HarnessAgentFactory(AgentRuntimeProperties properties) {
        this(properties, Path.of(".agentOS", "workspace"));
    }

    HarnessAgentFactory(AgentRuntimeProperties properties, Path workspaceRoot) {
        this.properties = Objects.requireNonNull(properties, "properties 不能为空");
        this.workspaceRoot = Objects.requireNonNull(workspaceRoot, "workspaceRoot 不能为空")
                .toAbsolutePath().normalize();
    }

    public HarnessAgent create(AgentDefinition definition, ChatActor actor) {
        Objects.requireNonNull(definition, "definition must not be null");
        Objects.requireNonNull(actor, "actor must not be null");

        return switch (definition.getKind()) {
            case CHAT -> createChatAgent(definition);
            case CREATE_COSPLAYER -> createCosplayerAgent(definition, actor);
        };
    }

    private HarnessAgent createChatAgent(AgentDefinition definition) {
        // 保留原聊天目录，兼容已存在的人格与会话文件。
        Path workspacePath = workspaceRoot
                .resolve("agents").resolve(String.valueOf(definition.getAgentId())).resolve("users");
        return commonBuilder(definition, workspacePath)
                .disableDefaultWorkspaceSkills()
                .build();
    }

    private HarnessAgent createCosplayerAgent(AgentDefinition definition, ChatActor actor) {
        Path workspacePath = workspaceRoot;
        // 客服保留默认 Workspace Skill Repository，构造差异仅在工厂内维护。
        return commonBuilder(definition, workspacePath).build();
    }

    private HarnessAgent.Builder commonBuilder(AgentDefinition definition, Path workspacePath) {
        return HarnessAgent.builder()
                .agentId(String.valueOf(definition.getAgentId()))
                .name(definition.getAgentName())
                .sysPrompt(definition.getSystemPrompt())
                .model(createModel(definition))
                // 传输重试由 GenerateOptions 控制，避免 Harness 再嵌套重试。
                .modelExecutionConfig(ExecutionConfig.builder().maxAttempts(1).build())
                .maxIters(definition.getMaxIters())     // 最大循环次数
                .workspace(workspacePath)
                // .stateStore(new JsonFileAgentStateStore(stateDirectory))     // 会话存储配置
                .enableAgentTracingLog(false)        // AgentScope自带 trace 日志
                .disableFilesystemTools()        // 内置文件系统工具
                .disableShellTool()      // Shell/命令执行工具
                .disableToolsConfig()    // workspace/tools.json 自动工具配置
                .disableDynamicSkills()     //运行过程中动态加载 Skill
                .disableDefaultWorkspaceSkills()    // Harness 默认 Workspace Skill Repository
                .disableSubagents()     // 子 Agent 系统
                .disableDynamicSubagents()      // 动态发现/生成子 Agent
                .disableAtPathExpansion()    // @路径 一类文件引用自动展开
                .disableMemoryHooks()       // 自动长期记忆写入、整理、维护
                .disableCompaction()        // 上下文自动总结/压缩
                .disableToolResultEviction();        // 大工具结果卸载
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
        return "openAI".equals(protocol);
    }
}
