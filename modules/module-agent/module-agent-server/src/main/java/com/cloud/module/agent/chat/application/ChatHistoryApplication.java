package com.cloud.module.agent.chat.application;

import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.module.agent.chat.application.model.ChatMessageSlice;
import com.cloud.module.agent.chat.application.model.ChatSessionSummary;
import com.cloud.module.agent.chat.domain.ChatActor;

/**
 * 聊天历史查询用例。
 *
 * <p>历史查询与流式运行分开建模，避免普通分页请求侵入 ChatApplication 的
 * begin - invoke - finalize 生命周期。</p>
 */
public interface ChatHistoryApplication {

    PageResult<ChatSessionSummary> findSessions(Long agentId, PageParam pageParam, ChatActor actor);

    ChatMessageSlice findMessages(Long sessionId, Long beforeSeq, int pageSize, ChatActor actor);
}
