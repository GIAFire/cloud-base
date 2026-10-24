package com.cloud.module.agent.chat.adapter.agentscope;

import com.cloud.module.agent.chat.domain.AgentKind;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinition;
import com.cloud.module.agent.chat.runtime.definition.AgentModelDefinition;
import io.agentscope.harness.agent.HarnessAgent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class HarnessAgentFactoryTest {

    @TempDir
    Path workspaceRoot;

    @Test
    void keeps_existing_chat_workspace_and_disables_default_skills() {
        try (HarnessAgent agent = factory().create(definition(AgentKind.CHAT), new ChatActor(10L, 20L))) {
            assertThat(agent.getWorkspaceManager().getWorkspace())
                    .isEqualTo(workspaceRoot.resolve("agents/100/users"));
            assertThat(agent.getSkillRepositories()).isEmpty();
            assertThat(agent.getName()).isEqualTo("test-agent");
            assertThat(agent.getMaxIters()).isEqualTo(12);
        }
    }

    @Test
    void enables_customer_service_workspace_skills_and_separates_tenants() {
        HarnessAgentFactory factory = factory();
        AgentDefinition definition = definition(AgentKind.CUSTOMER_SERVICE);
        try (HarnessAgent first = factory.create(definition, new ChatActor(10L, 20L));
             HarnessAgent second = factory.create(definition, new ChatActor(11L, 20L))) {
            assertThat(first.getWorkspaceManager().getWorkspace())
                    .isEqualTo(workspaceRoot.resolve("customer-service/tenants/10/agents/100"));
            assertThat(second.getWorkspaceManager().getWorkspace())
                    .isEqualTo(workspaceRoot.resolve("customer-service/tenants/11/agents/100"));
            assertThat(first.getSkillRepositories()).isNotEmpty();
            assertThat(second.getSkillRepositories()).isNotEmpty();
        }
    }

    private HarnessAgentFactory factory() {
        AgentRuntimeProperties properties = new AgentRuntimeProperties();
        return new HarnessAgentFactory(properties, workspaceRoot);
    }

    private AgentDefinition definition(AgentKind kind) {
        AgentModelDefinition model = new AgentModelDefinition(
                "openAI", "https://model.example/v1", "test-key", "test-model",
                true, null, null, 2048, null
        );
        return new AgentDefinition(100L, "test-agent", kind, "测试提示词", 12, model);
    }
}
