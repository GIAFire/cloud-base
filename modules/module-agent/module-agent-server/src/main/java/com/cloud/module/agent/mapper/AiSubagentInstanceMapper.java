package com.cloud.module.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.module.agent.entity.AiSubagentInstanceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 子Agent运行实例表：记录父Agent创建的子Agent实例、会话、工作区和暴露状态 Mapper 接口
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-11
 */
@Mapper
public interface AiSubagentInstanceMapper extends BaseMapper<AiSubagentInstanceEntity> {

}
