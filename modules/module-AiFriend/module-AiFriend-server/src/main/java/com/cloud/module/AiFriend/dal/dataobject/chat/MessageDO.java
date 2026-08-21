package com.cloud.module.AiFriend.dal.dataobject.chat;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.cloud.framework.mybatis.core.dataobject.BaseDO;

/**
 * Agent消息表：保存输入、思考、正文等消息 DO
 *
 * @author ZR
 */
@TableName("agent_message")
@KeySequence("agent_message_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDO extends BaseDO {

    /**
     * 消息主键ID
     */
    @TableId
    private Long id;
    /**
     * 会话ID，关联ai_agent_session.id
     */
    private Long sessionId;
    /**
     * 会话内全局消息序号，用于历史恢复和稳定排序
     */
    private Long seq;
    /**
     * 协议角色：USER、ASSISTANT、SYSTEM、TOOL
     */
    private String role;
    /**
     * 消息类型：USER_TEXT、ASSISTANT_THINKING、ASSISTANT_TEXT、TOOL_CALL、TOOL_RESULT、SKILL_CALL、SKILL_RESULT、SUBAGENT_CALL、SUBAGENT_RESULT、PLAN_SNAPSHOT、PLAN_OPERATION、SYSTEM_NOTICE、ERROR
     */
    private String messageType;
    /**
     * 可搜索和直接展示的文本内容
     */
    private String textContent;
    /**
     * Token用量，例如inputTokens、outputTokens、reasoningTokens、totalTokens
     */
    private String usageJson;
    /**
     * 耗时，单位毫秒
     */
    private Long durationMs;

}