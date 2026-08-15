package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.module.agent.entity.AiModelCallLogEntity;
import com.cloud.module.agent.mapper.AiModelCallLogMapper;
import com.cloud.module.agent.service.AiModelCallLogService;
import org.springframework.stereotype.Service;

@Service
public class AiModelCallLogServiceImpl
        extends ServiceImpl<AiModelCallLogMapper, AiModelCallLogEntity>
        implements AiModelCallLogService {
}
