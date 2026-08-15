package com.cloud.module.agent.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.module.agent.entity.AiAgentToolEntity;
import com.cloud.module.agent.entity.DTO.AgentBindToolDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * Agent与Tool绑定表：定义某个Agent启用了哪些工具 Mapper 接口
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-12
 */
@Mapper
public interface AiAgentToolMapper extends BaseMapper<AiAgentToolEntity> {

    @InterceptorIgnore(tenantLine = "true")
    List<AgentBindToolDTO> agentBindTools(
            Long agentId,
            Long agentConfigId,
            Long tenantId
    );
}
