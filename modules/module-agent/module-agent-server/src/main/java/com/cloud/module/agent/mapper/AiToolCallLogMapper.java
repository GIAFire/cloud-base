package com.cloud.module.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.module.agent.entity.AiToolCallLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 工具调用审计表：记录Agent每一次工具调用的权限结果、参数、结果和耗时 Mapper 接口
 * </p>
 *
 * @author zhiRan
 * @since 2026-06-28
 */
@Mapper
public interface AiToolCallLogMapper extends BaseMapper<AiToolCallLogEntity> {

}
