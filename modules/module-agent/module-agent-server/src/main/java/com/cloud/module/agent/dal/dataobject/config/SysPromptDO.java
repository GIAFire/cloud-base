package com.cloud.module.agent.dal.dataobject.config;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
public class SysPromptDO extends TenantBaseDO {

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
