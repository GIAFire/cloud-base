package com.cloud.module.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.module.agent.entity.AiKnowledgeRetrievalHitEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 知识库检索命中明细表：记录每次检索命中的文档切片、分数和引用信息 Mapper 接口
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-06
 */
@Mapper
public interface AiKnowledgeRetrievalHitMapper extends BaseMapper<AiKnowledgeRetrievalHitEntity> {

}
