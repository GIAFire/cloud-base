package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentStateLogEntity;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;
import com.cloud.module.agent.mapper.AiAgentStateLogMapper;
import com.cloud.module.agent.runtime.AgentRuntimeKeys;
import com.cloud.module.agent.service.AiAgentStateLogService;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.state.AgentState;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * AgentState状态引用表：记录AgentScope运行时状态在外部存储中的位置和元数据 服务实现类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-04
 */
@RequiredArgsConstructor
@Service
public class AiAgentStateLogServiceImpl extends ServiceImpl<AiAgentStateLogMapper, AiAgentStateLogEntity> implements AiAgentStateLogService {

    private final AiAgentStateLogMapper agentStateLogMapper;
    private final AiAgentStateOpLogServiceImpl agentStateOpLogService;

    @Async
    @Override
    public CompletableFuture<Boolean> saveStateLog(Long runId, LoginUser loginUser,
                                                   AgentConfigDTO config,
                                                   Long sessionId) {
        try {
            AiAgentStateLogEntity stateLogEntity = new AiAgentStateLogEntity();
            stateLogEntity.setTenantId(loginUser.getTenantId());
            stateLogEntity.setAgentId(config.getAgentId());
            stateLogEntity.setAgentConfigId(config.getAgentConfigId());
            stateLogEntity.setRunId(runId);
            stateLogEntity.setSessionId(sessionId);
            stateLogEntity.setUserId(loginUser.getId());
            stateLogEntity.setStateBackend("REDIS");
            stateLogEntity.setStateKey(AgentRuntimeKeys.redisStateKey(loginUser.getId(), sessionId));
            stateLogEntity.setLastLoadedAt(LocalDateTime.now());
            stateLogEntity.setExpireAt(LocalDateTime.now().plusDays(30));
            this.saveOrUpdate(stateLogEntity);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }


    public Boolean updateState(
            HarnessAgent agent,
            RuntimeContext runtimeContext,
            AgentConfigDTO config,
            LoginUser loginUser,
            Long sessionId,
            Long runId
    ) {
        AiAgentStateLogEntity updateBefore = agentStateLogMapper.selectByRuntimeKey(
                runId,
                loginUser.getId(),
                sessionId);

        AgentState agentState = agent.getDelegate().getAgentState(runtimeContext);

        if (agentState == null) {
            agentStateOpLogService.recordSaveFailed(
                    loginUser,
                    sessionId,
                    runId,
                    "RuntimeContext AgentState is null"
            );
            return false;
        }
        String json = agentState.toJson();
        AiAgentStateLogEntity updateAfter = new AiAgentStateLogEntity();
        updateAfter.setTenantId(loginUser.getTenantId());
        updateAfter.setUserId(loginUser.getId());
        updateAfter.setSessionId(sessionId);
        updateAfter.setStateSizeBytes((long) json.getBytes(StandardCharsets.UTF_8).length);
        updateAfter.setContextMessageCount(agentState.getContext().isEmpty() ? 0 : agentState.getContext().size());
        updateAfter.setSummaryExists(agentState.getSummary().isEmpty() ? 0 : 1);
        updateAfter.setSummaryPreview(agentState.getSummary());
        updateAfter.setLastSavedAt(LocalDateTime.now());

        if (!agentState.getSummary().isEmpty()) {
            updateAfter.setLastCompactedAt(LocalDateTime.now());
        }

        Boolean isUpdateByRuntime  = agentStateLogMapper.updateByRuntimeKey(updateAfter);

        agentStateOpLogService.recordSave(
                loginUser,
                sessionId,
                runId,
                updateBefore,
                updateAfter,
                isUpdateByRuntime,
                null
        );

        if (isUpdateByRuntime && isCompacted(updateBefore, updateAfter)) {
            agentStateOpLogService.recordCompact(
                    loginUser,
                    sessionId,
                    runId,
                    updateBefore,
                    updateAfter
            );
        }

        return isUpdateByRuntime;
    }

    private boolean isCompacted(AiAgentStateLogEntity before, AiAgentStateLogEntity after) {
        if (before == null || after == null) {
            return false;
        }

        boolean summaryCreated =
                (before.getSummaryExists() == null || before.getSummaryExists() == 0)
                        && after.getSummaryExists() != null
                        && after.getSummaryExists() == 1;

        boolean messageReduced =
                before.getContextMessageCount() != null
                        && after.getContextMessageCount() != null
                        && after.getContextMessageCount() < before.getContextMessageCount();

        return summaryCreated || messageReduced;
    }

}
