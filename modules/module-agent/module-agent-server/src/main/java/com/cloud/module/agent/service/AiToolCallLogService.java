package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiToolCallLogEntity;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;
import com.cloud.module.agent.event.AgentRuntimeEvent;


import java.util.Map;

/**
 * <p>
 * 工具调用审计表：记录Agent每一次工具调用的权限结果、参数、结果和耗时 服务类
 * </p>
 *
 * @author zhiRan
 * @since 2026-06-28
 */
public interface AiToolCallLogService extends IService<AiToolCallLogEntity> {

    public void handleToolCallAuditEvent(
            String eventType,
            AgentRuntimeEvent runtimeEvent,
            AgentConfigDTO config,
            LoginUser loginUser,
            Long sessionId,
            Long runId,
            Map<String, AiToolCallLogEntity> toolAuditMap
    );

}
