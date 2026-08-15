package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentStateLogEntity;
import com.cloud.module.agent.entity.AiAgentStateOpLogEntity;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.HarnessAgent;

/**
 * <p>
 * AgentState操作日志表：记录状态加载、保存、压缩、清理等操作 服务类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-04
 */
public interface AiAgentStateOpLogService extends IService<AiAgentStateOpLogEntity> {

    int recordLoad(HarnessAgent agent, LoginUser loginUser, AgentConfigDTO config, RuntimeContext runtimeContext, Long sessionId, Long runId);

    void recordSave(
            LoginUser loginUser,
            Long sessionId,
            Long runId,
            AiAgentStateLogEntity before,
            AiAgentStateLogEntity after,
            boolean success,
            String errorMessage
    );

    void recordCompact(
            LoginUser loginUser,
            Long sessionId,
            Long runId,
            AiAgentStateLogEntity before,
            AiAgentStateLogEntity after
    );

    void recordSaveFailed(
            LoginUser loginUser,
            Long sessionId,
            Long runId,
            String errorMessage
    );
}
