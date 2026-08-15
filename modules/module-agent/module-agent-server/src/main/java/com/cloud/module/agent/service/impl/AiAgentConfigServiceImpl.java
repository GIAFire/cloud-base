package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.module.agent.entity.AiAgentConfigEntity;
import com.cloud.module.agent.mapper.AiAgentConfigMapper;
import com.cloud.module.agent.service.AiAgentConfigService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Agent 版本表：保存每次可视化配置发布后的不可变快照 服务实现类
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
@Service
public class AiAgentConfigServiceImpl extends ServiceImpl<AiAgentConfigMapper, AiAgentConfigEntity> implements AiAgentConfigService {

}
