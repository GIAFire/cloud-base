package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.module.agent.entity.AiAgentModelEntity;
import com.cloud.module.agent.mapper.AiAgentModelMapper;
import com.cloud.module.agent.service.AiAgentModelService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 模型配置表：把凭证、模型名、生成参数组合成可被 Agent 选择的模型 服务实现类
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
@Service
public class AiAgentModelServiceImpl extends ServiceImpl<AiAgentModelMapper, AiAgentModelEntity> implements AiAgentModelService {

}
