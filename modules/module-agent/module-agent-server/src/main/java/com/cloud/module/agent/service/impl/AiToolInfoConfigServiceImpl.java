package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.module.agent.entity.AiToolInfoConfigEntity;
import com.cloud.module.agent.mapper.AiToolInfoConfigMapper;
import com.cloud.module.agent.service.AiToolInfoConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 全局工具配置表 服务实现类
 * </p>
 *
 * @author zhiRan
 * @since 2026-06-27
 */
@Service
@RequiredArgsConstructor
public class AiToolInfoConfigServiceImpl extends ServiceImpl<AiToolInfoConfigMapper, AiToolInfoConfigEntity> implements AiToolInfoConfigService {

    private final AiToolInfoConfigMapper aiToolInfoConfigMapper;


    @Override
    public int upsertBatch(List<AiToolInfoConfigEntity> list) {
        return aiToolInfoConfigMapper.upsertBatch(list);
    }
}
