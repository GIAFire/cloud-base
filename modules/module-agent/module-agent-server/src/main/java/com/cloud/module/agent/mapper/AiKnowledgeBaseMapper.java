package com.cloud.module.agent.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.module.agent.entity.AiKnowledgeBaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 知识库表：平台知识库抽象层，兼容RAGFlow及不同向量库 Mapper 接口
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-06
 */
@Mapper
public interface AiKnowledgeBaseMapper extends BaseMapper<AiKnowledgeBaseEntity> {

    @Select("""
            SELECT *
            FROM ai_knowledge_base
            WHERE id = #{knowledgeBaseId}
              AND deleted = 0
            FOR UPDATE
            """)
    AiKnowledgeBaseEntity selectByIdForUpdate(
            @Param("knowledgeBaseId") Long knowledgeBaseId
    );

    @InterceptorIgnore(tenantLine = "true")
    List<AiKnowledgeBaseEntity> getAgentBindKnowledge(
            @Param("agentId") Long agentId,
            @Param("agentConfigId") Long agentConfigId,
            @Param("tenantId") Long tenantId
    );
}
