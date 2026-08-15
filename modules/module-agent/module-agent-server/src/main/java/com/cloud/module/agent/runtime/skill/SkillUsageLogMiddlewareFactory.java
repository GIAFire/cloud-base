package com.cloud.module.agent.runtime.skill;

import com.cloud.module.agent.entity.DTO.AgentConfigDTO;
import com.cloud.module.agent.service.AiSkillInfoService;
import com.cloud.module.agent.service.AiSkillLogService;
import io.agentscope.core.middleware.MiddlewareBase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkillUsageLogMiddlewareFactory {

    private final AiSkillInfoService skillService;
    private final AiSkillLogService logService;

    public MiddlewareBase create(AgentConfigDTO config) {
        return new SkillUsageLogMiddleware(skillService, logService, config);
    }
}
