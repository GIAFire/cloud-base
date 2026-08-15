package com.cloud.module.agent.runtime;

import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;
import lombok.Data;

@Data
public class AgentCallContext {

    LoginUser loginUser;
    AgentConfigDTO agentConfig;
    Long sessionId;
    Long runId;
    String workspacePath;
}
