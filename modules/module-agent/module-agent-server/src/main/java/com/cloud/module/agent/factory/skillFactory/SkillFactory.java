package com.cloud.module.agent.factory.skillFactory;

import com.cloud.framework.common.enums.UserTypeEnum;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;
import com.cloud.module.agent.service.AiSkillInfoService;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SkillFactory {
    private final AiSkillInfoService skillInfoService;

    public AgentSkillRepository mysqlSkillFactory(
            AgentConfigDTO config,
            LoginUser loginUser) {
            return new SkillRepository(
                    config.getAgentId(),
                    config.getAgentConfigId(),
                    loginUser.getTenantId(),
                    loginUser.getRoleIds(),
                    skillInfoService);
    }
}
