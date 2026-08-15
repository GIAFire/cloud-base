package com.cloud.module.agent.factory.runtimeContextFactory;

import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;
import com.cloud.module.agent.runtime.AgentCallContext;
import io.agentscope.core.agent.RuntimeContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@RequiredArgsConstructor
@Component
public class RuntimeContextFactory {


    public RuntimeContext buildRuntimeContext(AgentConfigDTO config,
                                               LoginUser loginUser,
                                               Long sessionId,
                                               Long runId) {

        RuntimeContext.Builder builder = RuntimeContext.builder()
                .userId(String.valueOf(loginUser.getId()))
                .sessionId(String.valueOf(sessionId));
        AgentCallContext agentCallContext = new AgentCallContext();
        agentCallContext.setAgentConfig(config);
        agentCallContext.setLoginUser(loginUser);
        agentCallContext.setSessionId(sessionId);
        agentCallContext.setRunId(runId);
        builder.put(AgentCallContext.class, agentCallContext);

        return builder.build();
    }
}
