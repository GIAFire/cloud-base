package com.cloud.module.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.module.agent.entity.AiSubagentAgentBindingEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 主Agent与子Agent绑定表：定义某个主Agent版本可以委派哪些子Agent及调用策略 Mapper 接口
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-11
 */
@Mapper
public interface AiSubagentAgentBindingMapper extends BaseMapper<AiSubagentAgentBindingEntity> {

}
