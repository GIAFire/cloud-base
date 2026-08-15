package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.cloud.module.agent.constant.enumeration.HeaderSourceType;
import com.cloud.module.agent.entity.AiHttpHeaderEntity;
import com.cloud.module.agent.mapper.AiHttpHeaderMapper;
import com.cloud.module.agent.service.AiHttpHeaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * HTTP请求头配置表 服务实现类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-26
 */
@RequiredArgsConstructor
@Service
public class AiHttpHeaderServiceImpl extends ServiceImpl<AiHttpHeaderMapper, AiHttpHeaderEntity> implements AiHttpHeaderService {

    private final AiHttpHeaderMapper aiHttpHeaderMapper;

    @Override
    public List<AiHttpHeaderEntity> getHeaderList(
            Long sourceId,
            Long tenantId,
            HeaderSourceType source
    ) {
        return aiHttpHeaderMapper.getHeaderList(sourceId, tenantId, source);
    }
}
