package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.module.agent.entity.AiAgentPlanEntity;
import com.cloud.module.agent.mapper.AiAgentPlanMapper;
import com.cloud.module.agent.service.AiAgentPlanService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Agent计划表：保存Plan Mode生成的计划元数据、内容快照、审批和执行状态 服务实现类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-08
 */
@Service
public class AiAgentPlanServiceImpl extends ServiceImpl<AiAgentPlanMapper, AiAgentPlanEntity> implements AiAgentPlanService {

}
