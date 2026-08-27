package com.cloud.module.agent.chat.adapter.persistence;

import com.cloud.module.agent.chat.store.ChatStore;
import com.cloud.module.agent.dal.mysql.chat.AgentMessageMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentRunMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentSessionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MySQL ChatStore Adapter 的装配入口。
 */
@Configuration(proxyBeanMethods = false)
public class ChatStorePersistenceConfiguration {

    /** 创建包含全部同步 MyBatis 事务规则的内部实现。 */
    @Bean
    public ChatStoreTransactions chatStoreTransactions(
            AgentSessionMapper sessionMapper,
            AgentRunMapper runMapper,
            AgentMessageMapper messageMapper
    ) {
        return new ChatStoreTransactions(sessionMapper, runMapper, messageMapper);
    }

    /** 创建负责 Reactor、租户上下文和阻塞 JDBC 调度的 ChatStore Adapter。 */
    @Bean
    public ChatStore mybatisChatStore(ChatStoreTransactions transactions) {
        return new MybatisChatStore(transactions);
    }
}
