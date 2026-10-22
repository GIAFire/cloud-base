package com.cloud.module.agent.chat.application;

import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.module.agent.chat.application.model.ChatMessageSlice;
import com.cloud.module.agent.chat.application.model.ChatSessionSummary;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.store.ChatHistoryStore;
import com.cloud.module.agent.chat.store.ChatStoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.cloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloud.module.agent.enums.ErrorCodeConstants.CHAT_SESSION_BUSY;
import static com.cloud.module.agent.enums.ErrorCodeConstants.CHAT_SESSION_NOT_FOUND_OR_DENIED;

/** 默认聊天历史管理用例实现。 */
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

    @Override
    public void deleteSession(Long sessionId, ChatActor actor) {
        requirePositive(sessionId, "sessionId");
        Objects.requireNonNull(actor, "actor must not be null");
        try {
            chatHistoryStore.deleteSession(sessionId, actor);
        } catch (ChatStoreException storeException) {
            throw mapDeleteException(storeException);
        }
    }

    /**
     * 不区分“不存在”和“非所有者”，避免调用方枚举其他用户的 Session ID。
     */
    private RuntimeException mapDeleteException(ChatStoreException storeException) {
        return switch (storeException.getReason()) {
            case SESSION_NOT_FOUND, SESSION_ACCESS_DENIED ->
                    exception(CHAT_SESSION_NOT_FOUND_OR_DENIED);
            case SESSION_BUSY -> exception(CHAT_SESSION_BUSY);
            default -> storeException;
        };
    }

    private static Long requirePositive(Long value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
