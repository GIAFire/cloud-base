package com.cloud.module.agent.dal.mysql.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agent.chat.domain.AgentMessageType;
import com.cloud.module.agent.chat.domain.ChatRole;
import com.cloud.module.agent.dal.dataobject.chat.AgentMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** 最终业务消息 Mapper。 */
@Mapper
public interface AgentMessageMapper extends BaseMapperX<AgentMessageDO> {

    /**
     * 查询给定 seq 之前最新的消息。limit 只接受应用层校验后的正整数。
     */
    default List<AgentMessageDO> selectLatestSlice(Long sessionId, Long beforeSeq, int limit) {
        if (limit < 1 || limit > 101) {
            throw new IllegalArgumentException("limit must be between 1 and 101");
        }
        LambdaQueryWrapper<AgentMessageDO> query = new LambdaQueryWrapper<AgentMessageDO>()
                .eq(AgentMessageDO::getSessionId, sessionId)
                .orderByDesc(AgentMessageDO::getSeq)
                .last("LIMIT " + limit);
        if (beforeSeq != null) {
            query.lt(AgentMessageDO::getSeq, beforeSeq);
        }
        return selectList(query);
    }

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

    /** 按 Session 软删全部消息。 */
    default int deleteBySessionId(Long sessionId) {
        return delete(AgentMessageDO::getSessionId, sessionId);
    }
}
