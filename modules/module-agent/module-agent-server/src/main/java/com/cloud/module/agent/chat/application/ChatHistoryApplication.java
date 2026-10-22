package com.cloud.module.agent.chat.application;

import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.module.agent.chat.application.model.ChatMessageSlice;
import com.cloud.module.agent.chat.application.model.ChatSessionSummary;
import com.cloud.module.agent.chat.domain.ChatActor;

/**
 * 聊天历史管理用例。
 *
 * <p>历史查询和会话删除与流式运行分开建模，避免普通历史管理请求侵入 ChatApplication 的
 * begin - invoke - finalize 生命周期。</p>
 */
public interface ChatHistoryApplication {

    PageResult<ChatSessionSummary> findSessions(Long agentId, PageParam pageParam, ChatActor actor);

    ChatMessageSlice findMessages(Long sessionId, Long beforeSeq, int pageSize, ChatActor actor);

    /** 删除当前参与者拥有的、没有活动 Run 的聊天会话。 */
    void deleteSession(Long sessionId, ChatActor actor);
}
