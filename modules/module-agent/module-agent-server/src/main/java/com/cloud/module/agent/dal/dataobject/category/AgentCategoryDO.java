package com.cloud.module.agent.dal.dataobject.category;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Agent 分类 DO。
 *
 * @author Cloud
 */
@TableName("agent_category")
@KeySequence("agent_category_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AgentCategoryDO extends TenantBaseDO {

    /** 分类编号。 */
    @TableId
    private Long id;

    /** 分类名称，同一租户内唯一。 */
    private String name;

    /** 显示顺序。 */
    private Integer sort;

    /** 状态，参见 {@code CommonStatusEnum}。 */
    private Integer status;

}
