package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.module.agent.entity.AiSkillInfoEntity;
import com.cloud.module.agent.entity.DTO.SkillFileDTO;
import com.cloud.module.agent.mapper.AiSkillInfoMapper;
import com.cloud.module.agent.service.AiSkillInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AiSkillInfoServiceImpl
        extends ServiceImpl<AiSkillInfoMapper, AiSkillInfoEntity>
        implements AiSkillInfoService {

    private final AiSkillInfoMapper skillInfoMapper;

    @Override
    public SkillFileDTO getAgentSkill(
            String skillCode,
            Long agentId,
            Long agentConfigId,
            Long tenantId,
            Set<Long> roleIds
    ) {
        return skillInfoMapper.getAgentSkill(
                skillCode, agentId, agentConfigId, tenantId, roleIds
        );
    }

    @Override
    public List<SkillFileDTO> getAgentSkillNames(
            Long agentId,
            Long agentConfigId,
            Long tenantId,
            Set<Long> roleIds
    ) {
        return skillInfoMapper.getAgentSkillName(
                agentId, agentConfigId, tenantId, roleIds
        );
    }
}
