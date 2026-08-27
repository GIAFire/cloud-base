package com.cloud.module.agent.chat.adapter.persistence;

import com.cloud.module.agent.chat.application.command.StartChatCommand;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.store.ChatStoreException;
import com.cloud.module.agent.dal.dataobject.chat.AgentSessionDO;
import com.cloud.module.agent.dal.mysql.chat.AgentMessageMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentRunMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentSessionMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatStoreTransactionsTest {

    @Test
    void rejects_switching_agent_in_an_existing_session() {
        AgentSessionMapper sessionMapper = mock(AgentSessionMapper.class);
        AgentRunMapper runMapper = mock(AgentRunMapper.class);
        AgentMessageMapper messageMapper = mock(AgentMessageMapper.class);
        ChatStoreTransactions transactions = new ChatStoreTransactions(
                sessionMapper,
                runMapper,
                messageMapper
        );

        ChatActor actor = new ChatActor(10L, 20L);
        StartChatCommand command = new StartChatCommand(
                200L,
                300L,
                "request-001",
                "你好",
                actor
        );
        AgentSessionDO session = new AgentSessionDO();
        session.setId(300L);
        session.setAgentId(100L);
        session.setTenantId(10L);
        session.setUserId(20L);

        when(runMapper.selectByUserAndRequestId(20L, "request-001", 300L))
                .thenReturn(null);
        when(sessionMapper.selectByIdForUpdate(300L)).thenReturn(session);

        assertThatThrownBy(() -> transactions.begin(command))
                .isInstanceOf(ChatStoreException.class)
                .satisfies(error -> assertThat(((ChatStoreException) error).getReason())
                        .isEqualTo(ChatStoreException.Reason.SESSION_AGENT_MISMATCH));
    }
}
