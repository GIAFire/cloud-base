package com.cloud.module.agent.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.module.agent.entity.AiSubagentEntity;
import com.cloud.module.agent.entity.DTO.SubagentHeaderDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 子Agent定义表：保存可复用专家Agent的能力描述、模型、工具、知识库和安全配置 Mapper 接口
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-11
 */
@Mapper
public interface AiSubagentMapper extends BaseMapper<AiSubagentEntity> {

    List<SubagentHeaderDTO> subAgentList(@Param("agentId") Long agentId, @Param("tenantId") Long tenantId);

    @Delete("DELETE FROM ai_subagent WHERE id = #{id} AND tenant_id = #{tenantId}")
    int hardDeleteById(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @InterceptorIgnore(tenantLine = "true")
    @Select({
            "<script>",
            "SELECT id, subagent_name FROM ai_subagent",
            "WHERE tenant_id = #{tenantId}",
            "AND id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    List<AiSubagentEntity> selectNamesIncludingDeleted(
            @Param("tenantId") Long tenantId,
            @Param("ids") Collection<Long> ids
    );
}
