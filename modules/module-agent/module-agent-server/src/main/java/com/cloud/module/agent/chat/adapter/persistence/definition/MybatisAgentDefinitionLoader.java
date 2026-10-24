package com.cloud.module.agent.chat.adapter.persistence.definition;

import com.cloud.framework.tenant.core.util.TenantUtils;
import com.cloud.module.agent.chat.domain.AgentKind;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinition;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinitionException;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinitionLoader;
import com.cloud.module.agent.chat.runtime.definition.AgentModelDefinition;
import com.cloud.module.agent.dal.dataobject.agentInfo.AgentInfoDO;
import com.cloud.module.agent.dal.dataobject.config.ModelDO;
import com.cloud.module.agent.dal.dataobject.config.SysPromptDO;
import com.cloud.module.agent.dal.mysql.agentInfo.AgentInfoMapper;
import com.cloud.module.agent.dal.mysql.config.ModelMapper;
import com.cloud.module.agent.dal.mysql.config.SysPromptMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/** 使用 MyBatis 从当前租户加载 Agent、模型和系统提示词。 */
@Component
public final class MybatisAgentDefinitionLoader implements AgentDefinitionLoader {

    private static final int DEFAULT_MAX_TOKENS = 4096;

    private final AgentInfoMapper agentInfoMapper;
    private final ModelMapper modelMapper;
    private final SysPromptMapper sysPromptMapper;

    public MybatisAgentDefinitionLoader(
            AgentInfoMapper agentInfoMapper,
            ModelMapper modelMapper,
            SysPromptMapper sysPromptMapper
    ) {
        this.agentInfoMapper = Objects.requireNonNull(agentInfoMapper, "agentInfoMapper 不能为空");
        this.modelMapper = Objects.requireNonNull(modelMapper, "modelMapper 不能为空");
        this.sysPromptMapper = Objects.requireNonNull(sysPromptMapper, "sysPromptMapper 不能为空");
    }

    @Override
    public Mono<AgentDefinition> load(Long agentId, ChatActor actor) {
        Objects.requireNonNull(agentId, "agentId must not be null");
        Objects.requireNonNull(actor, "actor must not be null");

        return Mono.fromCallable(() -> executeInTenant(
                        actor.getTenantId(),
                        () -> loadSynchronously(agentId)
                ))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private AgentDefinition loadSynchronously(Long agentId) {
        AgentInfoDO agent = agentInfoMapper.selectById(agentId);
        if (agent == null) {
            throw AgentDefinitionException.agentNotFound(agentId);
        }
        if (agent.getModelId() == null) {
            throw AgentDefinitionException.modelReferenceMissing(agentId);
        }
        if (agent.getSysPromptId() == null) {
            throw AgentDefinitionException.promptReferenceMissing(agentId);
        }

        ModelDO model = modelMapper.selectById(agent.getModelId());
        if (model == null) {
            throw AgentDefinitionException.modelNotFound(agentId);
        }
        SysPromptDO prompt = sysPromptMapper.selectById(agent.getSysPromptId());
        if (prompt == null) {
            throw AgentDefinitionException.promptNotFound(agentId);
        }

        if (!isOpenAiCompatible(model.getProtocol())) {
            throw AgentDefinitionException.unsupportedProtocol(agentId);
        }

        try {
            AgentModelDefinition modelDefinition = new AgentModelDefinition(
                    model.getProtocol(),
                    model.getBaseUrl(),
                    model.getApiKey(),
                    model.getModelName(),
                    Boolean.TRUE.equals(model.getStreaming()),
                    model.getTemperature(),
                    model.getTopP(),
                    model.getMaxTokens() == null ? DEFAULT_MAX_TOKENS : model.getMaxTokens(),
                    model.getThinkingBudget()
            );
            return new AgentDefinition(
                    agent.getId(),
                    agent.getAgentName(),
                    AgentKind.valueOf(agent.getAgentKind() == null ? "CHAT" : agent.getAgentKind()),
                    prompt.getSysPrompt(),
                    agent.getMaxIters(),
                    modelDefinition
            );
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw AgentDefinitionException.invalidConfiguration(agentId);
        }
    }

    private boolean isOpenAiCompatible(String protocol) {
        if (protocol == null) {
            return false;
        }
        return "openAI".equals(protocol);
    }

    private <T> T executeInTenant(Long tenantId, Supplier<T> operation) {
        AtomicReference<T> result = new AtomicReference<>();
        TenantUtils.execute(tenantId, () -> result.set(operation.get()));
        return Objects.requireNonNull(result.get(), "Tenant operation returned null");
    }
}
