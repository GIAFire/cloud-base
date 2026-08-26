package com.cloud.module.agent.chat.adapter.persistence;

import com.cloud.module.agent.chat.adapter.agentscope.AgentScopeRuntimeProperties;
import com.cloud.module.agent.chat.store.ChatStore;
import com.cloud.module.agent.dal.mysql.chat.AgentMessageMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentRunMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentSessionMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "agent.runtime.store",
        name = "type",
        havingValue = "mysql"
)
public class ChatStorePersistenceConfiguration {

    @Bean
    public ChatStoreTransactions chatStoreTransactions(
            AgentSessionMapper sessionMapper,
            AgentRunMapper runMapper,
            AgentMessageMapper messageMapper,
            AgentScopeRuntimeProperties runtimeProperties
    ) {
        Long agentId = runtimeProperties.getAgentId();

        if (agentId == null || agentId <= 0) {
            throw new IllegalArgumentException(
                    "agent.runtime.agentscope.agent-id "
                            + "must be greater than 0"
            );
        }

        return new ChatStoreTransactions(
                sessionMapper,
                runMapper,
                messageMapper,
                agentId
        );
    }

    @Bean
    public ChatStore mybatisChatStore(
            ChatStoreTransactions transactions
    ) {
        return new MybatisChatStore(
                transactions
        );
    }
}