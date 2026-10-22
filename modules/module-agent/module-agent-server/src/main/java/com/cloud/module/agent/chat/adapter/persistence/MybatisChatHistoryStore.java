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
import com.cloud.module.agent.dal.dataobject.chat.AgentRunDO;
import com.cloud.module.agent.dal.dataobject.chat.AgentSessionDO;
import com.cloud.module.agent.dal.mysql.chat.AgentMessageMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentRunMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 基于 MyBatis 的聊天历史读仓储。 */
@Component
@RequiredArgsConstructor
public class MybatisChatHistoryStore implements ChatHistoryStore {

    private final AgentSessionMapper sessionMapper;
    private final AgentRunMapper runMapper;
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
        requireSessionOwner(session, actor);

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

    /**
     * 先锁 Session，再检查并锁定活动 Run，锁顺序与 begin/finish 保持一致。
     * 所有删除均由 MyBatis-Plus 逻辑删除转换，并在同一事务中提交。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId, ChatActor actor) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(actor, "actor must not be null");

        AgentSessionDO session = sessionMapper.selectByIdForUpdate(sessionId);
        if (session == null) {
            throw ChatStoreException.sessionNotFound(sessionId);
        }
        requireSessionOwner(session, actor);

        AgentRunDO runningRun = runMapper.selectRunningBySessionIdForUpdate(sessionId);
        if (runningRun != null) {
            throw ChatStoreException.sessionBusy(sessionId, runningRun.getId());
        }

        messageMapper.deleteBySessionId(sessionId);
        runMapper.deleteBySessionId(sessionId);
        int deleted = sessionMapper.deleteById(sessionId);
        if (deleted != 1) {
            throw ChatStoreException.persistenceIntegrity(
                    "Failed to delete chat session",
                    sessionId,
                    null
            );
        }
    }

    private void requireSessionOwner(AgentSessionDO session, ChatActor actor) {
        if (!Objects.equals(session.getUserId(), actor.getUserId())
                || !Objects.equals(session.getTenantId(), actor.getTenantId())) {
            throw ChatStoreException.sessionAccessDenied(session.getId());
        }
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
