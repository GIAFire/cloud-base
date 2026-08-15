package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentMessageEntity;
import com.cloud.module.agent.runtime.message.RuntimeMessageDraft;


import java.util.List;

/**
 * <p>
 * Agent 消息表：保存用户输入、Agent 回复、工具消息等完整上下文 服务类
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
public interface AiAgentMessageService extends IService<AiAgentMessageEntity> {

    AiAgentMessageEntity saveUserMessage(LoginUser loginUser, Long sessionId, String content);

    void bindRunId(Long messageId, Long runId);

    AiAgentMessageEntity saveRunMessages(
            LoginUser loginUser,
            Long sessionId,
            Long runId,
            List<RuntimeMessageDraft> drafts,
            Integer usageToken,
            Double usageTime
    );
}
