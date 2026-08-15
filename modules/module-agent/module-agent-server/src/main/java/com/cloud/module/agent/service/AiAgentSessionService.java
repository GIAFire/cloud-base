package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentSessionEntity;


/**
 * <p>
 * Agent 会话表：保存用户与 Agent 的一次连续对话 服务类
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
public interface AiAgentSessionService extends IService<AiAgentSessionEntity> {

    AiAgentSessionEntity createSession(LoginUser loginUser, Long agentId, Long agentConfigId, String title);

    AiAgentSessionEntity getOwnedSession(LoginUser loginUser, Long agentId, Long sessionId);
}
