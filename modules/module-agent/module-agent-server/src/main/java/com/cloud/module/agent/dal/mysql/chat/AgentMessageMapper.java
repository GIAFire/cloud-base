package com.cloud.module.agent.dal.mysql.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agent.chat.domain.AgentMessageType;
import com.cloud.module.agent.chat.domain.ChatRole;
import com.cloud.module.agent.dal.dataobject.chat.AgentMessageDO;
import org.apache.ibatis.annotations.Mapper;

/** 最终业务消息 Mapper。 */
@Mapper
public interface AgentMessageMapper extends BaseMapperX<AgentMessageDO> {

    /**
     * 查询 Run 的完整助手正文，用于验证重复 SUCCEEDED finalization 是否幂等。
     */
    default AgentMessageDO selectAssistantTextByRunId(Long runId) {
        return selectOne(
                new LambdaQueryWrapper<AgentMessageDO>()
                        .eq(AgentMessageDO::getRunId, runId)
                        .eq(AgentMessageDO::getRole, ChatRole.ASSISTANT.name())
                        .eq(AgentMessageDO::getMessageType, AgentMessageType.ASSISTANT_TEXT.name())
        );
    }
}
