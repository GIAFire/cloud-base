package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentRunEventLogEntity;


/**
 * <p>
 * Agent 运行事件表：保存流式 token、工具调用、权限请求等事件 服务类
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
public interface AiAgentRunEventLogService extends IService<AiAgentRunEventLogEntity> {

    int saveEvent(LoginUser loginUser, Long runId, Long sessionId, String runtimeEvent);
}
