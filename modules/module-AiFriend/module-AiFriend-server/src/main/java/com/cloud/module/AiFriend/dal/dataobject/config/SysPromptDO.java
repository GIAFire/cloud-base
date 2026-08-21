package com.cloud.module.AiFriend.dal.dataobject.config;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.cloud.framework.mybatis.core.dataobject.BaseDO;

/**
 * 系统提示词表 DO
 *
 * @author ZR
 */
@TableName("agent_sys_prompt")
@KeySequence("agent_sys_prompt_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysPromptDO extends BaseDO {

    /**
     * 提示词模板主键ID
     */
    @TableId
    private Long id;
    /**
     * 提示词模板名称
     */
    private String promptName;
    /**
     * 描述
     */
    private String description;
    /**
     * 提示词内容
     */
    private String sysPrompt;

}