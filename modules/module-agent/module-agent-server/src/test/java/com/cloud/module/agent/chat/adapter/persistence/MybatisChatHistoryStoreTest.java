package com.cloud.module.agent.chat.adapter.persistence;

import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.module.agent.chat.application.model.ChatMessageSlice;
import com.cloud.module.agent.chat.application.model.ChatSessionSummary;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.store.ChatStoreException;
import com.cloud.module.agent.dal.dataobject.chat.AgentMessageDO;
import com.cloud.module.agent.dal.dataobject.chat.AgentSessionDO;
import com.cloud.module.agent.dal.mysql.chat.AgentMessageMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentSessionMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MybatisChatHistoryStoreTest {

    @Test
    void maps_sessions_for_current_owner_and_agent() {
        AgentSessionMapper sessionMapper = mock(AgentSessionMapper.class);
        AgentMessageMapper messageMapper = mock(AgentMessageMapper.class);
        MybatisChatHistoryStore store = new MybatisChatHistoryStore(sessionMapper, messageMapper);
        PageParam pageParam = new PageParam();
        pageParam.setPageSize(20);
        AgentSessionDO session = session(100L, 200L, 10L, 20L);
        session.setTitle("第一段会话");
        session.setLastMessageSeq(8L);

        when(sessionMapper.selectPageByOwnerAndAgent(pageParam, 20L, 200L))
                .thenReturn(new PageResult<>(List.of(session), 1L));

        PageResult<ChatSessionSummary> result = store.findSessions(
                200L,
                pageParam,
                new ChatActor(10L, 20L)
        );

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).singleElement().satisfies(summary -> {
            assertThat(summary.getId()).isEqualTo(100L);
            assertThat(summary.getAgentId()).isEqualTo(200L);
            assertThat(summary.getLastMessageSeq()).isEqualTo(8L);
        });
    }

    @Test
    void returns_latest_slice_in_ascending_order_with_cursor() {
        AgentSessionMapper sessionMapper = mock(AgentSessionMapper.class);
        AgentMessageMapper messageMapper = mock(AgentMessageMapper.class);
        MybatisChatHistoryStore store = new MybatisChatHistoryStore(sessionMapper, messageMapper);
        AgentSessionDO session = session(100L, 200L, 10L, 20L);
        when(sessionMapper.selectById(100L)).thenReturn(session);
        when(messageMapper.selectLatestSlice(100L, null, 3))
                .thenReturn(List.of(message(4L), message(3L), message(2L)));

        ChatMessageSlice result = store.findMessages(
                100L,
                null,
                2,
                new ChatActor(10L, 20L)
        );

        assertThat(result.isHasMore()).isTrue();
        assertThat(result.getNextBeforeSeq()).isEqualTo(3L);
        assertThat(result.getList()).extracting("seq").containsExactly(3L, 4L);
    }

    @Test
    void rejects_session_owned_by_another_user() {
        AgentSessionMapper sessionMapper = mock(AgentSessionMapper.class);
        AgentMessageMapper messageMapper = mock(AgentMessageMapper.class);
        MybatisChatHistoryStore store = new MybatisChatHistoryStore(sessionMapper, messageMapper);
        when(sessionMapper.selectById(100L)).thenReturn(session(100L, 200L, 10L, 99L));

        assertThatThrownBy(() -> store.findMessages(
                100L,
                null,
                50,
                new ChatActor(10L, 20L)
        )).isInstanceOf(ChatStoreException.class)
                .satisfies(error -> assertThat(((ChatStoreException) error).getReason())
                        .isEqualTo(ChatStoreException.Reason.SESSION_ACCESS_DENIED));
    }

    private AgentSessionDO session(
            Long id,
            Long agentId,
            Long tenantId,
            Long userId
    ) {
        AgentSessionDO session = new AgentSessionDO();
        session.setId(id);
        session.setAgentId(agentId);
        session.setTenantId(tenantId);
        session.setUserId(userId);
        return session;
    }

    private AgentMessageDO message(Long seq) {
        AgentMessageDO message = new AgentMessageDO();
        message.setId(seq * 10);
        message.setSessionId(100L);
        message.setRunId(500L);
        message.setSeq(seq);
        message.setRole("ASSISTANT");
        message.setMessageType("ASSISTANT_TEXT");
        message.setTextContent("message-" + seq);
        return message;
    }
}
