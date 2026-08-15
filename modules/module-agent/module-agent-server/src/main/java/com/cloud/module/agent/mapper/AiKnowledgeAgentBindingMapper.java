package com.cloud.module.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.module.agent.entity.AiKnowledgeAgentBindingEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Agent知识库绑定表：控制Agent配置可访问的知识库及检索参数 Mapper 接口
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-11
 */
@Mapper
public interface AiKnowledgeAgentBindingMapper extends BaseMapper<AiKnowledgeAgentBindingEntity> {

}
