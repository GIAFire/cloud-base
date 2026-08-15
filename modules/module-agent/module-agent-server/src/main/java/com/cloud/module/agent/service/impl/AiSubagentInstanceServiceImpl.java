package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.module.agent.entity.AiSubagentInstanceEntity;
import com.cloud.module.agent.mapper.AiSubagentInstanceMapper;
import com.cloud.module.agent.service.AiSubagentInstanceService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 子Agent运行实例表：记录父Agent创建的子Agent实例、会话、工作区和暴露状态 服务实现类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-11
 */
@Service
public class AiSubagentInstanceServiceImpl extends ServiceImpl<AiSubagentInstanceMapper, AiSubagentInstanceEntity> implements AiSubagentInstanceService {

}
