package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.module.agent.entity.AiSkillInfoEntity;
import com.cloud.module.agent.entity.DTO.SkillFileDTO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface AiSkillInfoService extends IService<AiSkillInfoEntity> {

    SkillFileDTO getAgentSkill(
            String skillCode,
            Long agentId,
            Long agentConfigId,
            Long tenantId,
            Set<Long> roleIds
    );

    List<SkillFileDTO> getAgentSkillNames(
            Long agentId,
            Long agentConfigId,
            Long tenantId,
            Set<Long> roleIds
    );
}
