package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.module.agent.entity.AiKnowledgeDocumentEntity;
import com.cloud.module.agent.mapper.AiKnowledgeDocumentMapper;
import com.cloud.module.agent.service.AiKnowledgeDocumentService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 知识库文档表：记录平台文档与外部RAG/向量库文档的映射关系 服务实现类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-06
 */
@Service
public class AiKnowledgeDocumentServiceImpl extends ServiceImpl<AiKnowledgeDocumentMapper, AiKnowledgeDocumentEntity> implements AiKnowledgeDocumentService {

}
