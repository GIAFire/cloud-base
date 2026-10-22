package com.cloud.module.agent.dal.dataobject.agentInfo;

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
 * Agent 定义表 DO
 *
 * @author ZR
 */
@TableName("agent_info")
@KeySequence("agent_info_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentInfoDO extends TenantBaseDO {

    /**
     * Agent 主键ID
     */
    @TableId
    private Long id;
    /**
     * 关联 agent_model.id
     */
    private Long modelId;
    /**
     * 关联 agent_sys_prompt.id
     */
    private Long sysPromptId;
    /**
     * Agent唯一编码
     */
    private String agentCode;
    /**
     * Agent 显示名称
     */
    private String agentName;
    /**
     * Agent 描述
     */
    private String description;
    /**
     * 最大循环次数
     */
    private Integer maxIters;
    /**
     * 默认权限模式
     */
    private String permissionMode;
    /**
     * 是否启用上下文压缩：1启用，0关闭
     */
    private Boolean compactionEnabled;
    /**
     * 触发压缩消息数
     */
    private Integer triggerMessages;
    /**
     * 压缩后保留消息数
     */
    private Integer keepMessages;
    /**
     * 触发压缩token数
     */
    private Integer triggerTokens;
    /**
     * 压缩后保留token数
     */
    private Integer keepTokens;
    /**
     * 是否启用大工具结果卸载：1启用，0关闭
     */
    private Boolean toolResultEvictionEnabled;
    /**
     * 是否启用长期记忆:1启用,0禁用
     */
    private Boolean memoryEnable;
    /**
     * 是否启用Plan Mode：1启用，0关闭
     */
    private Boolean planModeEnabled;
    /**
     * Plan阶段是否允许shell工具：1允许，0禁止；生产环境建议关闭
     */
    private Boolean allowShellInPlanMode;
    /**
     * 人格设定
     */
    private String personality;


}
