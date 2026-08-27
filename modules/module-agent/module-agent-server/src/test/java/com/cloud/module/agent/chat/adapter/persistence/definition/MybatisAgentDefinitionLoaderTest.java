package com.cloud.module.agent.chat.adapter.persistence.definition;

import com.cloud.framework.tenant.core.context.TenantContextHolder;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinition;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinitionException;
import com.cloud.module.agent.dal.dataobject.agentInfo.AgentInfoDO;
import com.cloud.module.agent.dal.dataobject.config.ModelDO;
import com.cloud.module.agent.dal.dataobject.config.SysPromptDO;
import com.cloud.module.agent.dal.mysql.agentInfo.AgentInfoMapper;
import com.cloud.module.agent.dal.mysql.config.ModelMapper;
import com.cloud.module.agent.dal.mysql.config.SysPromptMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MybatisAgentDefinitionLoaderTest {

    private static final Long AGENT_ID = 100L;
    private static final Long TENANT_ID = 10L;

    private final AgentInfoMapper agentInfoMapper = mock(AgentInfoMapper.class);
    private final ModelMapper modelMapper = mock(ModelMapper.class);
    private final SysPromptMapper sysPromptMapper = mock(SysPromptMapper.class);
    private final MybatisAgentDefinitionLoader loader = new MybatisAgentDefinitionLoader(
            agentInfoMapper,
            modelMapper,
            sysPromptMapper
    );

    @Test
    void loads_complete_definition_inside_actor_tenant_context() {
        AgentInfoDO agent = new AgentInfoDO();
        agent.setId(AGENT_ID);
        agent.setModelId(200L);
        agent.setSysPromptId(300L);
        agent.setAgentName("客服助手");
        agent.setMaxIters(12);

        ModelDO model = new ModelDO();
        model.setId(200L);
        model.setProtocol("openaiCompatible");
        model.setBaseUrl("https://model.example/v1");
        model.setApiKey("secret-key");
        model.setModelName("test-model");
        model.setStreaming(true);
        model.setTemperature(new BigDecimal("0.3"));
        model.setTopP(new BigDecimal("0.8"));
        model.setMaxTokens(2048);

        SysPromptDO prompt = new SysPromptDO();
        prompt.setId(300L);
        prompt.setSysPrompt("你是客服助手");

        when(agentInfoMapper.selectById(AGENT_ID)).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(TENANT_ID);
            return agent;
        });
        when(modelMapper.selectById(200L)).thenReturn(model);
        when(sysPromptMapper.selectById(300L)).thenReturn(prompt);

        AgentDefinition definition = loader.load(
                AGENT_ID,
                new ChatActor(TENANT_ID, 20L)
        ).block();

        assertThat(definition).isNotNull();
        assertThat(definition.getAgentId()).isEqualTo(AGENT_ID);
        assertThat(definition.getAgentName()).isEqualTo("客服助手");
        assertThat(definition.getSystemPrompt()).isEqualTo("你是客服助手");
        assertThat(definition.getMaxIters()).isEqualTo(12);
        assertThat(definition.getModel().getModelName()).isEqualTo("test-model");
        assertThat(definition.getModel().getApiKey()).isEqualTo("secret-key");
    }

    @Test
    void reports_missing_agent_with_stable_reason() {
        when(agentInfoMapper.selectById(AGENT_ID)).thenReturn(null);

        assertThatThrownBy(() -> loader.load(
                AGENT_ID,
                new ChatActor(TENANT_ID, 20L)
        ).block())
                .isInstanceOf(AgentDefinitionException.class)
                .satisfies(error -> assertThat(
                        ((AgentDefinitionException) error).getReason()
                ).isEqualTo(AgentDefinitionException.Reason.AGENT_NOT_FOUND));
    }
}
