package com.cloud.module.agent.chat.store;

import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.module.agent.chat.application.model.ChatMessageSlice;
import com.cloud.module.agent.chat.application.model.ChatSessionSummary;
import com.cloud.module.agent.chat.domain.ChatActor;

/** 聊天历史读仓储端口。 */
public interface ChatHistoryStore {

    PageResult<ChatSessionSummary> findSessions(Long agentId, PageParam pageParam, ChatActor actor);

    ChatMessageSlice findMessages(Long sessionId, Long beforeSeq, int pageSize, ChatActor actor);
}
