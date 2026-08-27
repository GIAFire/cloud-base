package com.cloud.module.agent.chat.adapter.agentscope;

import com.cloud.module.agent.chat.runtime.AgentRuntime;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinitionLoader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class AgentScopeRuntimeConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AgentScopeRuntimeConfiguration.class)
            .withBean(AgentDefinitionLoader.class, () -> (agentId, actor) -> Mono.empty())
            .withPropertyValues(
                    "agent.runtime.model-timeout=45s",
                    "agent.runtime.model-max-attempts=3"
            );

    @Test
    void always_registers_dynamic_runtime_and_binds_runtime_policy() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AgentRuntime.class);
            assertThat(context).hasSingleBean(HarnessAgentFactory.class);
            assertThat(context).hasSingleBean(AgentRuntimeProperties.class);

            AgentRuntimeProperties properties = context.getBean(AgentRuntimeProperties.class);
            assertThat(properties.getModelTimeout()).isEqualTo(Duration.ofSeconds(45));
            assertThat(properties.getModelMaxAttempts()).isEqualTo(3);
        });
    }

    @Test
    void rejects_non_positive_model_timeout() {
        contextRunner
                .withPropertyValues("agent.runtime.model-timeout=0s")
                .run(context -> assertThat(context).hasFailed());
    }
}
