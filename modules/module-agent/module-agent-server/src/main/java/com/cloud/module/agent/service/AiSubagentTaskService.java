package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiSubagentTaskEntity;
import com.cloud.module.agent.entity.AiToolCallLogEntity;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;


/**
 * <p>
 * 子Agent任务表：记录agent_spawn/agent_send产生的同步或后台委派任务 服务类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-11
 */
public interface AiSubagentTaskService extends IService<AiSubagentTaskEntity> {

    void recordDelegationTask(
            AiToolCallLogEntity toolCallLog,
            AgentConfigDTO config,
            LoginUser loginUser,
            Long sessionId,
            Long runId
    );
}
