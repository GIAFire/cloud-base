package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentRunEventLogEntity;
import com.cloud.module.agent.mapper.AiAgentRunEventLogMapper;
import com.cloud.module.agent.service.AiAgentRunEventLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * Agent 运行事件表：保存流式 token、工具调用、权限请求等事件 服务实现类
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
@Service
public class AiAgentRunEventLogServiceImpl extends ServiceImpl<AiAgentRunEventLogMapper, AiAgentRunEventLogEntity> implements AiAgentRunEventLogService {

    @Autowired
    private AiAgentRunEventLogMapper agentRunEventMapper;
    @Override
    public int saveEvent(LoginUser loginUser, Long runId, Long sessionId, String runtimeEvent) {
        AiAgentRunEventLogEntity agentRunEventEntity = new AiAgentRunEventLogEntity();
        agentRunEventEntity.setRunId(runId);
        agentRunEventEntity.setEventType(runtimeEvent);
        agentRunEventEntity.setTenantId(loginUser.getTenantId());
        agentRunEventEntity.setSessionId(sessionId);
        agentRunEventEntity.setCreateTime(LocalDateTime.now());
        return agentRunEventMapper.insert(agentRunEventEntity);
    }
}
