package com.cloud.module.agent.dal.dataobject.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.framework.tenant.core.db.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * agent_session 表映射。
 *
 * <p>Session 表示某个租户用户与固定 Agent 的连续对话，也是消息全局序号和
 * 同会话并发互斥的锁定根。业务层不得直接暴露该数据对象。</p>
 */
@Getter
@Setter
@TableName("agent_session")
public class AgentSessionDO extends TenantBaseDO {

    /** 雪花主键，同时用作 AgentScope RuntimeContext sessionId。 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 创建会话时绑定的业务 Agent。 */
    private Long agentId;

    /** 会话所属登录用户；tenantId 继承自 TenantBaseDO。 */
    private Long userId;

    /** 默认由第一条用户消息截断生成。 */
    private String title;

    /** 已分配的最后一条业务消息序号，初始为 0。 */
    private Long lastMessageSeq;

    /** 最后一条持久化业务消息时间。 */
    private LocalDateTime lastMessageAt;
}
