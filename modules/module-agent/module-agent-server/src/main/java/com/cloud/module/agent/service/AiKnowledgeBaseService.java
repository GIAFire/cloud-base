package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.module.agent.entity.AiKnowledgeBaseEntity;

import java.util.List;

/**
 * <p>
 * 知识库表：平台知识库抽象层，兼容RAGFlow及不同向量库 服务类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-06
 */
public interface AiKnowledgeBaseService extends IService<AiKnowledgeBaseEntity> {

    List<AiKnowledgeBaseEntity> getAgentBindKnowledge(Long agentId, Long agentConfigId, Long tenantId);
}
