package com.cloud.module.agent.chat.adapter.persistence;

import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.module.agent.chat.application.model.ChatMessageSlice;
import com.cloud.module.agent.chat.application.model.ChatSessionSummary;
import com.cloud.module.agent.chat.application.model.StoredChatMessage;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.store.ChatHistoryStore;
import com.cloud.module.agent.chat.store.ChatStoreException;
import com.cloud.module.agent.dal.dataobject.chat.AgentMessageDO;
import com.cloud.module.agent.dal.dataobject.chat.AgentSessionDO;
import com.cloud.module.agent.dal.mysql.chat.AgentMessageMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 基于 MyBatis 的聊天历史读仓储。 */
@Component
@RequiredArgsConstructor
public class MybatisChatHistoryStore implements ChatHistoryStore {

    private final AgentSessionMapper sessionMapper;
    private final AgentMessageMapper messageMapper;

    @Override
    public PageResult<ChatSessionSummary> findSessions(
            Long agentId,
            PageParam pageParam,
            ChatActor actor
    ) {
        PageResult<AgentSessionDO> page = sessionMapper.selectPageByOwnerAndAgent(
                pageParam,
                actor.getUserId(),
                agentId
        );
        List<ChatSessionSummary> summaries = page.getList().stream()
                .map(this::toSessionSummary)
                .toList();
        return new PageResult<>(summaries, page.getTotal());
    }

    @Override
    public ChatMessageSlice findMessages(
            Long sessionId,
            Long beforeSeq,
            int pageSize,
            ChatActor actor
    ) {
        AgentSessionDO session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw ChatStoreException.sessionNotFound(sessionId);
        }
        if (!Objects.equals(session.getUserId(), actor.getUserId())
                || !Objects.equals(session.getTenantId(), actor.getTenantId())) {
            throw ChatStoreException.sessionAccessDenied(sessionId);
        }

        List<AgentMessageDO> rows = messageMapper.selectLatestSlice(
                sessionId,
                beforeSeq,
                pageSize + 1
        );
        boolean hasMore = rows.size() > pageSize;
        List<AgentMessageDO> visibleRows = new ArrayList<>(
                rows.subList(0, Math.min(rows.size(), pageSize))
        );
        Collections.reverse(visibleRows);

        List<StoredChatMessage> messages = visibleRows.stream()
                .map(this::toStoredMessage)
                .toList();
        Long nextBeforeSeq = hasMore && !messages.isEmpty()
                ? messages.get(0).getSeq()
                : null;
        return new ChatMessageSlice(messages, nextBeforeSeq, hasMore);
    }

    private ChatSessionSummary toSessionSummary(AgentSessionDO session) {
        return new ChatSessionSummary(
                session.getId(),
                session.getAgentId(),
                session.getTitle(),
                session.getLastMessageSeq(),
                session.getLastMessageAt()
        );
    }

    private StoredChatMessage toStoredMessage(AgentMessageDO message) {
        return new StoredChatMessage(
                message.getId(),
                message.getSessionId(),
                message.getRunId(),
                message.getSeq(),
                message.getRole(),
                message.getMessageType(),
                message.getTextContent(),
                message.getCreateTime()
        );
    }
}
