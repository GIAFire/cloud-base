package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentRunLogEntity;


/**
 * <p>
 * Agent 运行表：一次用户请求对应一次 AgentScope call 或 streamEvents 执行 服务类
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
public interface AiAgentRunLogService extends IService<AiAgentRunLogEntity> {

    AiAgentRunLogEntity createRunningRun(LoginUser loginUser, Long agentId, Long agentConfigId, Long sessionId, Long messageId);

    void markSuccess(Long runId, Long messageId);

    void markWaiting(Long runId, String status);

    void markFailed(Long runId, String agentRunFailed, String message);

    void markCancelled(Long runId);
}
