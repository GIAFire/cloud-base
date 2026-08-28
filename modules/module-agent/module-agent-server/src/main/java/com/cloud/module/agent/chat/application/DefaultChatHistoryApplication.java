package com.cloud.module.agent.chat.application;

import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.module.agent.chat.application.model.ChatMessageSlice;
import com.cloud.module.agent.chat.application.model.ChatSessionSummary;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.store.ChatHistoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/** 默认聊天历史查询用例实现。 */
@Component
@RequiredArgsConstructor
public class DefaultChatHistoryApplication implements ChatHistoryApplication {

    private final ChatHistoryStore chatHistoryStore;

    @Override
    public PageResult<ChatSessionSummary> findSessions(
            Long agentId,
            PageParam pageParam,
            ChatActor actor
    ) {
        Objects.requireNonNull(agentId, "agentId must not be null");
        Objects.requireNonNull(pageParam, "pageParam must not be null");
        Objects.requireNonNull(actor, "actor must not be null");
        return chatHistoryStore.findSessions(agentId, pageParam, actor);
    }

    @Override
    public ChatMessageSlice findMessages(
            Long sessionId,
            Long beforeSeq,
            int pageSize,
            ChatActor actor
    ) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(actor, "actor must not be null");
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize must be between 1 and 100");
        }
        return chatHistoryStore.findMessages(sessionId, beforeSeq, pageSize, actor);
    }
}
