package com.cloud.module.agentruntime.dal.dataobject.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.framework.tenant.core.db.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("agent_session")
public class AgentSessionDO extends TenantBaseDO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long agentId;

    private Long userId;

    private String title;

    private Long lastMessageSeq;

    private LocalDateTime lastMessageAt;
}