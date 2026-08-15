package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentRunLogEntity;
import com.cloud.module.agent.mapper.AiAgentRunLogMapper;
import com.cloud.module.agent.service.AiAgentRunLogService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * Agent 运行表：一次用户请求对应一次 AgentScope call 或 streamEvents 执行 服务实现类
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
@Service
public class AiAgentRunLogServiceImpl extends ServiceImpl<AiAgentRunLogMapper, AiAgentRunLogEntity> implements AiAgentRunLogService {
    @Autowired
    private AiAgentRunLogMapper agentRunMapper;

    @Override
    public AiAgentRunLogEntity createRunningRun(LoginUser loginUser, Long agentId, Long agentConfigId, Long sessionId, Long messageId) {
        AiAgentRunLogEntity agentRunEntity = new AiAgentRunLogEntity();
        agentRunEntity.setAgentId(agentId);
        agentRunEntity.setAgentConfigId(agentConfigId);
        agentRunEntity.setSessionId(sessionId);
        agentRunEntity.setInputMessageId(messageId);
        agentRunEntity.setStatus("RUNNING");
        agentRunEntity.setTenantId(loginUser.getTenantId());
        agentRunEntity.setStartedAt(LocalDateTime.now());
        agentRunMapper.insert(agentRunEntity);
        return agentRunEntity;
    }

    @Override
    public void markSuccess(Long runId, Long messageId) {
        AiAgentRunLogEntity aiAgentRunLogEntity = agentRunMapper.selectById(runId);
        if (aiAgentRunLogEntity == null) {
            return;
        }
        aiAgentRunLogEntity.setOutputMessageId(messageId);
        aiAgentRunLogEntity.setStatus("SUCCESS");
        aiAgentRunLogEntity.setEndedAt(LocalDateTime.now());
        agentRunMapper.updateById(aiAgentRunLogEntity);
    }

    @Override
    public void markWaiting(Long runId, String status) {
        AiAgentRunLogEntity aiAgentRunLogEntity = agentRunMapper.selectById(runId);
        if (aiAgentRunLogEntity == null) {
            return;
        }
        aiAgentRunLogEntity.setStatus(status);
        agentRunMapper.updateById(aiAgentRunLogEntity);
    }

    @Override
    public void markFailed(Long runId, String agentRunFailed, String message) {
        AiAgentRunLogEntity aiAgentRunLogEntity = agentRunMapper.selectById(runId);
        if (aiAgentRunLogEntity == null) {
            return;
        }
        aiAgentRunLogEntity.setStatus("FAILED");
        aiAgentRunLogEntity.setErrorCode(agentRunFailed);
        aiAgentRunLogEntity.setErrorMessage(message);
        aiAgentRunLogEntity.setEndedAt(LocalDateTime.now());
        agentRunMapper.updateById(aiAgentRunLogEntity);
    }

    @Override
    public void markCancelled(Long runId) {
        AiAgentRunLogEntity aiAgentRunLogEntity = agentRunMapper.selectById(runId);
        if (aiAgentRunLogEntity == null) {
            return;
        }
        aiAgentRunLogEntity.setStatus("CANCELLED");
        aiAgentRunLogEntity.setEndedAt(LocalDateTime.now());
        agentRunMapper.updateById(aiAgentRunLogEntity);
    }
}
