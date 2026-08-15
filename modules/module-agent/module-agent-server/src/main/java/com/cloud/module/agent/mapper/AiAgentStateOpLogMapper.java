package com.cloud.module.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.module.agent.entity.AiAgentStateOpLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * AgentState操作日志表：记录状态加载、保存、压缩、清理等操作 Mapper 接口
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-04
 */
@Mapper
public interface AiAgentStateOpLogMapper extends BaseMapper<AiAgentStateOpLogEntity> {

}
