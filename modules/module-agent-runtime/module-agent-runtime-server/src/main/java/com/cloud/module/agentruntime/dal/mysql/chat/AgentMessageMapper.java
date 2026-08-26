package com.cloud.module.agentruntime.dal.mysql.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agentruntime.chat.domain.ChatRole;
import com.cloud.module.agentruntime.dal.dataobject.chat.AgentMessageDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentMessageMapper extends BaseMapperX<AgentMessageDO> {

    default AgentMessageDO selectAssistantTextByRunId(
            Long runId
    ) {
        return selectOne(
                new LambdaQueryWrapper<AgentMessageDO>()
                        .eq(AgentMessageDO::getRunId, runId)
                        .eq(
                                AgentMessageDO::getRole,
                                ChatRole.ASSISTANT.name()
                        )
                        .eq(
                                AgentMessageDO::getMessageType,
                                "ASSISTANT_TEXT"
                        )
        );
    }
}