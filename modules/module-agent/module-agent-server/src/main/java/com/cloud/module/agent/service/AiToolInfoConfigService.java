package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.module.agent.entity.AiToolInfoConfigEntity;

import java.util.List;

/**
 * <p>
 * 全局工具配置表 服务类
 * </p>
 *
 * @author zhiRan
 * @since 2026-06-27
 */
public interface AiToolInfoConfigService extends IService<AiToolInfoConfigEntity> {

    int upsertBatch(List<AiToolInfoConfigEntity> list);

}
