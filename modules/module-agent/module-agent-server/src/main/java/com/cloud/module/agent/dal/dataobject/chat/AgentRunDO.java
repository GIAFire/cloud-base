package com.cloud.module.agent.dal.dataobject.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.framework.tenant.core.db.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * agent_run 表映射。
 *
 * <p>每次用户请求对应一个 Run。状态只允许从 RUNNING 进入一个终态；
 * requestId 在租户和用户范围内唯一，用于阻止客户端重试造成重复生成。</p>
 */
@Getter
@Setter
@TableName("agent_run")
public class AgentRunDO extends TenantBaseDO {

    /** 雪花 Run 主键。 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 客户端生成的幂等请求标识。 */
    private String requestId;

    /** 所属业务 Session。 */
    private Long sessionId;

    /** 发起运行的用户。 */
    private Long userId;

    /** AgentRunStatus 的枚举名。 */
    private String status;

    /** FAILED 终态的安全错误码。 */
    private String errorCode;

    /** FAILED 终态的已脱敏错误文案。 */
    private String errorMessage;

    /** begin 事务创建 Run 的时间。 */
    private LocalDateTime startedAt;

    /** finish 事务提交终态的时间。 */
    private LocalDateTime endedAt;

    /** startedAt 到 endedAt 的非负毫秒数。 */
    private Long durationMs;

    /** 模型输入 Token；当前事件协议尚未采集时允许为空。 */
    private Long inputTokens;

    /** 模型输出 Token；当前事件协议尚未采集时允许为空。 */
    private Long outputTokens;

    /** 模型推理 Token；供应商不支持时允许为空。 */
    private Long reasoningTokens;

    /** 模型总 Token；当前事件协议尚未采集时允许为空。 */
    private Long totalTokens;
}
