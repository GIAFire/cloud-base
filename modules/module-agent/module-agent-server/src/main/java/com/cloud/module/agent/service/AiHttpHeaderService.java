package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.module.agent.constant.enumeration.HeaderSourceType;
import com.cloud.module.agent.entity.AiHttpHeaderEntity;


import java.util.List;

/**
 * <p>
 * HTTP请求头配置表 服务类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-26
 */
public interface AiHttpHeaderService extends IService<AiHttpHeaderEntity> {

    List<AiHttpHeaderEntity> getHeaderList(
            Long sourceId,
            Long tenantId,
            HeaderSourceType source
    );
}
