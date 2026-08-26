package com.cloud.module.agent.dal.dataobject.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.framework.tenant.core.db.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

/**
 * agent_message 表映射。
 *
 * <p>只保存完整业务消息，不保存逐 Token delta。Session 行锁保护 seq 在会话
 * 内严格递增，数据库唯一键负责最终兜底。</p>
 */
@Getter
@Setter
@TableName("agent_message")
public class AgentMessageDO extends TenantBaseDO {

    /** 雪花消息主键。 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属 Session。 */
    private Long sessionId;

    /** 产生该消息的 Run。 */
    private Long runId;

    /** Session 内全局、严格递增的消息位置。 */
    private Long seq;

    /** ChatRole 的枚举名。 */
    private String role;

    /** AgentMessageType 的枚举名。 */
    private String messageType;

    /** 用户输入或最终助手回答的完整文本。 */
    private String textContent;
}
