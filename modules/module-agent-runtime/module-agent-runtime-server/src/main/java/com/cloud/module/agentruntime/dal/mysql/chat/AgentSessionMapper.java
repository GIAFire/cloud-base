package com.cloud.module.agentruntime.dal.mysql.chat;

import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agentruntime.dal.dataobject.chat.AgentSessionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentSessionMapper extends BaseMapperX<AgentSessionDO> {

    default AgentSessionDO selectByIdForUpdate(
            Long sessionId
    ) {
        return selectOneForUpdate(
                AgentSessionDO::getId,
                sessionId
        );
    }
}