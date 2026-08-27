package com.cloud.module.agent.chat.adapter.agentscope;

import com.cloud.module.agent.chat.runtime.AgentRuntime;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinitionLoader;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** AgentScope Adapter 的生产装配入口。 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AgentRuntimeProperties.class)
public class AgentScopeRuntimeConfiguration {

    /** 创建根据数据库定义按 Run 实例化 HarnessAgent 的工厂。 */
    @Bean
    public HarnessAgentFactory harnessAgentFactory(AgentRuntimeProperties properties) {
        return new HarnessAgentFactory(properties);
    }

    /** 创建模块对应用层暴露的唯一 AgentRuntime 实现。 */
    @Bean
    public AgentRuntime agentScopeRuntime(
            AgentDefinitionLoader definitionLoader,
            HarnessAgentFactory harnessAgentFactory
    ) {
        return new AgentScopeRuntime(definitionLoader, harnessAgentFactory);
    }
}
