package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentEntity;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;
import com.cloud.module.agent.exception.AgentConfigException;
import com.cloud.module.agent.mapper.AiAgentMapper;
import com.cloud.module.agent.service.AiAgentService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiAgentServiceImpl
        extends ServiceImpl<AiAgentMapper, AiAgentEntity>
        implements AiAgentService {

    private final AiAgentMapper agentMapper;

    @Override
    public AgentConfigDTO getAgentConfigById(Long agentId, LoginUser loginUser) {
        return getAgentConfigById(agentId, null, loginUser);
    }

    @Override
    public AgentConfigDTO getAgentConfigById(
            Long agentId,
            Long agentConfigId,
            LoginUser loginUser
    ) {
        if (agentId == null) {
            throw new AgentConfigException("agentId 不能为空");
        }
        if (loginUser == null || loginUser.getTenantId() == null) {
            throw new AgentConfigException("租户信息不能为空");
        }
        AgentConfigDTO config = agentMapper.getAgentConfigById(
                agentId, loginUser.getTenantId(), agentConfigId);
        if (config == null) {
            throw new AgentConfigException("智能体不存在、已删除或配置不可用");
        }
        if (config.getModelId() == null
                || config.getProtocol() == null
                || !Integer.valueOf(1).equals(config.getModelStatus())
                || !Integer.valueOf(0).equals(config.getModelDeleted())) {
            throw new AgentConfigException("智能体未绑定可用模型，请先完成模型配置");
        }
        return config;
    }

    @Override
    public List<AiAgentEntity> subAgentList(Long agentId, Long tenantId) {
        return agentMapper.subAgentList(agentId, tenantId);
    }
}
