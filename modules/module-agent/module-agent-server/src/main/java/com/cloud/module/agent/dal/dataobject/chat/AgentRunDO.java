package com.cloud.module.agent.dal.dataobject.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.framework.tenant.core.db.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("agent_run")
public class AgentRunDO extends TenantBaseDO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String requestId;

    private Long sessionId;

    private Long userId;

    private String status;

    private String errorCode;

    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Long durationMs;

    private Long inputTokens;

    private Long outputTokens;

    private Long reasoningTokens;

    private Long totalTokens;
}