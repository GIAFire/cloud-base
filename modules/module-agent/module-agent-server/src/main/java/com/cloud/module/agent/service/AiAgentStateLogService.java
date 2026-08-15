package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentStateLogEntity;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.HarnessAgent;

import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * AgentState状态引用表：记录AgentScope运行时状态在外部存储中的位置和元数据 服务类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-04
 */
public interface AiAgentStateLogService extends IService<AiAgentStateLogEntity> {


    CompletableFuture<Boolean> saveStateLog(Long runId,LoginUser loginUser,
                                                 AgentConfigDTO config,
                                                 Long sessionId);

    Boolean updateState(
            HarnessAgent agent,
            RuntimeContext runtimeContext,
            AgentConfigDTO config,
            LoginUser loginUser,
            Long sessionId,
            Long runId
    );

}
