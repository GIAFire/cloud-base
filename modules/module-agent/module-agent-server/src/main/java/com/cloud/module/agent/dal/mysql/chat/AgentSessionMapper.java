package com.cloud.module.agent.dal.mysql.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agent.dal.dataobject.chat.AgentSessionDO;
import org.apache.ibatis.annotations.Mapper;

/** Session 表 Mapper，集中提供 Session 行锁查询。 */
@Mapper
public interface AgentSessionMapper extends BaseMapperX<AgentSessionDO> {

    /** 查询当前用户在指定 Agent 下的会话，最近消息优先。 */
    default PageResult<AgentSessionDO> selectPageByOwnerAndAgent(
            PageParam pageParam,
            Long userId,
            Long agentId
    ) {
        return selectPage(
                pageParam,
                new LambdaQueryWrapper<AgentSessionDO>()
                        .eq(AgentSessionDO::getUserId, userId)
                        .eq(AgentSessionDO::getAgentId, agentId)
                        .orderByDesc(AgentSessionDO::getLastMessageAt)
                        .orderByDesc(AgentSessionDO::getId)
        );
    }

    /**
     * 按主键读取并锁定 Session，用于消息序号分配和同会话运行互斥。
     * 必须在 Spring 事务内调用。
     */
    default AgentSessionDO selectByIdForUpdate(Long sessionId) {
        return selectOneForUpdate(AgentSessionDO::getId, sessionId);
    }
}
