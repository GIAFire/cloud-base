package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentStateLogEntity;
import com.cloud.module.agent.entity.AiAgentStateOpLogEntity;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;
import com.cloud.module.agent.mapper.AiAgentStateOpLogMapper;
import com.cloud.module.agent.runtime.AgentRuntimeKeys;
import com.cloud.module.agent.service.AiAgentStateOpLogService;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.state.AgentState;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * <p>
 * AgentState操作日志表：记录状态加载、保存、压缩、清理等操作 服务实现类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-04
 */
@RequiredArgsConstructor
@Service
public class AiAgentStateOpLogServiceImpl extends ServiceImpl<AiAgentStateOpLogMapper, AiAgentStateOpLogEntity> implements AiAgentStateOpLogService {
    private final AiAgentStateOpLogMapper agentStateOpLogMapper;

    @Override
    public int recordLoad(
            HarnessAgent agent,
            LoginUser loginUser,
            AgentConfigDTO config,
            RuntimeContext runtimeContext,
            Long sessionId,
            Long runId
    ) {
        AgentState agentState = agent.getDelegate().getAgentState(runtimeContext);

        AiAgentStateOpLogEntity op = new AiAgentStateOpLogEntity();
        op.setTenantId(loginUser.getTenantId());
        op.setSessionId(sessionId);
        op.setRunId(runId);
        op.setUserId(loginUser.getId());
        op.setOpType("LOAD");
        op.setStateBackend("REDIS");
        op.setStateKey(AgentRuntimeKeys.redisStateKey(
                loginUser.getId(),
                sessionId
        ));
        op.setSuccess(1);

        if (agentState != null) {
            String json = agentState.toJson();
            op.setAfterSizeBytes((long) json.getBytes(StandardCharsets.UTF_8).length);
            op.setAfterMessageCount(agentState.getContext().isEmpty() ? 0 : agentState.getContext().size());
        }

        op.setCreateTime(LocalDateTime.now());
        return agentStateOpLogMapper.insert(op);
    }

    @Override
    public void recordSave(
            LoginUser loginUser,
            Long sessionId,
            Long runId,
            AiAgentStateLogEntity before,
            AiAgentStateLogEntity after,
            boolean success,
            String errorMessage
    ) {
        AiAgentStateOpLogEntity op = new AiAgentStateOpLogEntity();
        op.setTenantId(loginUser.getTenantId());
        op.setSessionId(sessionId);
        op.setRunId(runId);
        op.setUserId(loginUser.getId());
        op.setOpType("SAVE");
        op.setStateBackend("REDIS");
        op.setStateKey(AgentRuntimeKeys.redisStateKey(
                loginUser.getId(),
                sessionId));

        if (before != null) {
            op.setBeforeSizeBytes(before.getStateSizeBytes());
            op.setBeforeMessageCount(before.getContextMessageCount());
        }

        if (after != null) {
            op.setAfterSizeBytes(after.getStateSizeBytes());
            op.setAfterMessageCount(after.getContextMessageCount());
        }

        op.setSuccess(success ? 1 : 0);
        op.setErrorMessage(errorMessage);
        op.setCreateTime(LocalDateTime.now());

        agentStateOpLogMapper.insert(op);
    }

    @Override
    public void recordCompact(
            LoginUser loginUser,
            Long sessionId,
            Long runId,
            AiAgentStateLogEntity before,
            AiAgentStateLogEntity after
    ) {
        AiAgentStateOpLogEntity op = new AiAgentStateOpLogEntity();
        op.setTenantId(loginUser.getTenantId());
        op.setSessionId(sessionId);
        op.setRunId(runId);
        op.setUserId(loginUser.getId());
        op.setOpType("COMPACT");
        op.setStateBackend("REDIS");
        op.setStateKey(AgentRuntimeKeys.redisStateKey(loginUser.getId(), sessionId));
        op.setBeforeSizeBytes(before == null ? null : before.getStateSizeBytes());
        op.setAfterSizeBytes(after == null ? null : after.getStateSizeBytes());
        op.setBeforeMessageCount(before == null ? null : before.getContextMessageCount());
        op.setAfterMessageCount(after == null ? null : after.getContextMessageCount());
        op.setSuccess(1);
        op.setCreateTime(LocalDateTime.now());

        agentStateOpLogMapper.insert(op);
    }

    @Override
    public void recordSaveFailed(
            LoginUser loginUser,
            Long sessionId,
            Long runId,
            String errorMessage
    ) {
        AiAgentStateOpLogEntity op = new AiAgentStateOpLogEntity();
        op.setTenantId(loginUser.getTenantId());
        op.setSessionId(sessionId);
        op.setRunId(runId);
        op.setUserId(loginUser.getId());
        op.setOpType("SAVE");
        op.setStateBackend("REDIS");
        op.setStateKey(AgentRuntimeKeys.redisStateKey(loginUser.getId(), sessionId));
        op.setSuccess(0);
        op.setErrorMessage(errorMessage);
        op.setCreateTime(LocalDateTime.now());

        agentStateOpLogMapper.insert(op);
    }
}
