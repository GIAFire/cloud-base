package com.cloud.module.agent.dal.mysql.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agent.chat.domain.AgentRunStatus;
import com.cloud.module.agent.dal.dataobject.chat.AgentRunDO;
import org.apache.ibatis.annotations.Mapper;

/** Run 表 Mapper，提供幂等检查和固定锁顺序需要的查询。 */
@Mapper
public interface AgentRunMapper extends BaseMapperX<AgentRunDO> {

    /**
     * 查询用户范围内已使用的 requestId；tenantId 由多租户拦截器追加。
     */
    default AgentRunDO selectByUserAndRequestId(Long userId, String requestId,Long sessionId) {
        return selectOne(new LambdaQueryWrapper<AgentRunDO>()
                        .eq(AgentRunDO::getUserId, userId)
                        .eq(AgentRunDO::getRequestId, requestId)
                        .eq(AgentRunDO::getSessionId, sessionId));
    }

    /** 按主键锁定 Run，必须在 Spring 事务内调用。 */
    default AgentRunDO selectByIdForUpdate(Long runId) {
        return selectOneForUpdate(AgentRunDO::getId, runId);
    }

    /**
     * 锁定 Session 当前 RUNNING Run；返回非空时 begin 必须拒绝并发调用。
     */
    default AgentRunDO selectRunningBySessionIdForUpdate(Long sessionId) {
        return selectOneForUpdate(
                new LambdaQueryWrapper<AgentRunDO>()
                        .eq(AgentRunDO::getSessionId, sessionId)
                        .eq(AgentRunDO::getStatus, AgentRunStatus.RUNNING.name())
        );
    }
}
