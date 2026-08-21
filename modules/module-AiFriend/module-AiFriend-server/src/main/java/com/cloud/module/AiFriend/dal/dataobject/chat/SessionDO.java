package com.cloud.module.AiFriend.dal.dataobject.chat;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.cloud.framework.mybatis.core.dataobject.BaseDO;

/**
 * Agent会话表：保存用户和Agent之间的一段连续对话 DO
 *
 * @author ZR
 */
@TableName("agent_session")
@KeySequence("agent_session_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDO extends BaseDO {

    /**
     * 会话主键ID
     */
    @TableId
    private Long id;
    /**
     * Agent ID，关联智能体定义表
     */
    private Long agentId;
    /**
     * 用户ID，表示该会话归属哪个登录用户
     */
    private Long userId;
    /**
     * 会话标题，可由第一条用户消息或大模型生成
     */
    private String title;


}