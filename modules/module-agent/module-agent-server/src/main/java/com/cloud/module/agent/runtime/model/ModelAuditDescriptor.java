package com.cloud.module.agent.runtime.model;


import com.cloud.framework.security.core.LoginUser;

import java.util.List;

public record ModelAuditDescriptor(
        LoginUser loginUser,
        Long modelConfigId,
        Long agentId,
        Long agentConfigId,
        String configName,
        String protocol,
        String modelName,
        String sourcePath,
        List<String> secretValues
) {
}
