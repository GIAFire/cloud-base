package com.cloud.module.agent.dal.dataobject.tag;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Agent 标签 DO。
 *
 * @author Cloud
 */
@TableName("agent_tag")
@KeySequence("agent_tag_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AgentTagDO extends TenantBaseDO {

    /** 标签编号。 */
    @TableId
    private Long id;

    /** 标签名称，同一租户内唯一。 */
    private String name;

    /** 显示顺序。 */
    private Integer sort;

    /** 状态，参见 {@code CommonStatusEnum}。 */
    private Integer status;

}
