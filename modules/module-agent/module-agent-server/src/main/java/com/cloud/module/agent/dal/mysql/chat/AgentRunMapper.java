package com.cloud.module.agent.dal.mysql.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agent.chat.domain.AgentRunStatus;
import com.cloud.module.agent.dal.dataobject.chat.AgentRunDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentRunMapper extends BaseMapperX<AgentRunDO> {

    default AgentRunDO selectByUserAndRequestId(
            Long userId,
            String requestId
    ) {
        return selectOne(
                new LambdaQueryWrapper<AgentRunDO>()
                        .eq(AgentRunDO::getUserId, userId)
                        .eq(AgentRunDO::getRequestId, requestId)
        );
    }

    default AgentRunDO selectByIdForUpdate(
            Long runId
    ) {
        return selectOneForUpdate(
                AgentRunDO::getId,
                runId
        );
    }

    default AgentRunDO selectRunningBySessionIdForUpdate(
            Long sessionId
    ) {
        return selectOneForUpdate(
                new LambdaQueryWrapper<AgentRunDO>()
                        .eq(AgentRunDO::getSessionId, sessionId)
                        .eq(
                                AgentRunDO::getStatus,
                                AgentRunStatus.RUNNING.name()
                        )
        );
    }
}