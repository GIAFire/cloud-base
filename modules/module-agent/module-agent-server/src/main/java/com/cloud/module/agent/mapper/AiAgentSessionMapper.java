package com.cloud.module.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.module.agent.entity.AiAgentSessionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * Agent 会话表：保存用户与 Agent 的一次连续对话 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
@Mapper
public interface AiAgentSessionMapper extends BaseMapper<AiAgentSessionEntity> {

    @Select("""
            SELECT id
            FROM ai_agent_session
            WHERE tenant_id = #{tenantId}
              AND id = #{sessionId}
            FOR UPDATE
            """)
    Long lockSessionForUpdate(@Param("tenantId") Long tenantId, @Param("sessionId") Long sessionId);
}
