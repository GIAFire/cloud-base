package com.cloud.module.agent.dal.dataobject.relation;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent 与标签的多对多关联。
 *
 * <p>关联记录按业务语义硬删除，因此不继承带有 {@code @TableLogic}
 * 的 BaseDO。tenantId 显式存储，以便租户拦截器为所有读写自动补充租户边界。</p>
 */
@TableName("agent_tag_agent")
@KeySequence("agent_tag_agent_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTagAgentRelationDO implements Serializable {

    /** 代理主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Agent ID。 */
    private Long agentId;

    /** 标签 ID。 */
    private Long tagId;

    /** 租户编号。 */
    private Long tenantId;

    /** 关联创建时间。 */
    private LocalDateTime createTime;

}
