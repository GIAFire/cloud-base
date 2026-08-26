package com.cloud.module.agent.chat.adapter.persistence;

import com.cloud.module.agent.chat.adapter.agentscope.AgentScopeRuntimeProperties;
import com.cloud.module.agent.chat.store.ChatStore;
import com.cloud.module.agent.dal.mysql.chat.AgentMessageMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentRunMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentSessionMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MySQL ChatStore Adapter 的条件装配。
 *
 * <p>仅当 {@code agent.runtime.store.type=mysql} 时启用。配置层把当前固定
 * Agent ID 作为普通构造参数传入事务实现，避免持久化类直接依赖完整的
 * AgentScope 配置对象。</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "agent.runtime.store",
        name = "type",
        havingValue = "mysql"
)
public class ChatStorePersistenceConfiguration {

    /** 创建包含全部同步 MyBatis 事务规则的内部实现。 */
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
                    "agent.runtime.agentscope.agent-id " + "must be greater than 0"
            );
        }

        return new ChatStoreTransactions(
                sessionMapper,
                runMapper,
                messageMapper,
                agentId
        );
    }

    /** 创建负责 Reactor、租户上下文和阻塞 JDBC 调度的 ChatStore Adapter。 */
    @Bean
    public ChatStore mybatisChatStore(ChatStoreTransactions transactions) {
        return new MybatisChatStore(transactions);
    }
}
