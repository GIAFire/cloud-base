package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentEntity;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;


import java.util.List;

/**
 * <p>
 * Agent 定义表：保存一个可视化 Agent 的基础身份信息 服务类
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
public interface AiAgentService extends IService<AiAgentEntity> {

    AgentConfigDTO getAgentConfigById(Long agentId, LoginUser loginUser);

    AgentConfigDTO getAgentConfigById(Long agentId, Long agentConfigId, LoginUser loginUser);

    List<AiAgentEntity> subAgentList(Long agentId, Long tenantId);
}
