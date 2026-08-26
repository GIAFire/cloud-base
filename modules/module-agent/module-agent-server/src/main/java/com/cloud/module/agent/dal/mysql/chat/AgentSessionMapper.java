package com.cloud.module.agent.dal.mysql.chat;

import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agent.dal.dataobject.chat.AgentSessionDO;
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