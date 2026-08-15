package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.module.agent.entity.AiKnowledgeRetrievalHitEntity;
import com.cloud.module.agent.mapper.AiKnowledgeRetrievalHitMapper;
import com.cloud.module.agent.service.AiKnowledgeRetrievalHitService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 知识库检索命中明细表：记录每次检索命中的文档切片、分数和引用信息 服务实现类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-06
 */
@Service
public class AiKnowledgeRetrievalHitServiceImpl extends ServiceImpl<AiKnowledgeRetrievalHitMapper, AiKnowledgeRetrievalHitEntity> implements AiKnowledgeRetrievalHitService {

}
