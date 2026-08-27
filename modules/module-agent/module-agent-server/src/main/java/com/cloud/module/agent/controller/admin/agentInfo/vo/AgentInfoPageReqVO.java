package com.cloud.module.agent.controller.admin.agentInfo.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.cloud.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.cloud.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - Agent 定义表分页 Request VO")
@Data
public class AgentInfoPageReqVO extends PageParam {

    @Schema(description = "关联 ai_agent_model.id", example = "20375")
    private Long modelId;

    @Schema(description = "关联 ai_agent_sys_prompt.id", example = "14732")
    private Long sysPromptId;

    @Schema(description = "Agent唯一编码")
    private String agentCode;

    @Schema(description = "Agent 显示名称", example = "王五")
    private String agentName;

    @Schema(description = "Agent 描述", example = "你说的对")
    private String description;

    @Schema(description = "最大循环次数")
    private Integer maxIters;

    @Schema(description = "默认权限模式")
    private String permissionMode;

    @Schema(description = "是否启用上下文压缩：1启用，0关闭")
    private Boolean compactionEnabled;

    @Schema(description = "触发压缩消息数")
    private Integer triggerMessages;

    @Schema(description = "压缩后保留消息数")
    private Integer keepMessages;

    @Schema(description = "触发压缩token数")
    private Integer triggerTokens;

    @Schema(description = "压缩后保留token数")
    private Integer keepTokens;

    @Schema(description = "是否启用大工具结果卸载：1启用，0关闭")
    private Boolean toolResultEvictionEnabled;

    @Schema(description = "是否启用长期记忆:1启用,0禁用")
    private Boolean memoryEnable;

    @Schema(description = "是否启用Plan Mode：1启用，0关闭")
    private Boolean planModeEnabled;

    @Schema(description = "Plan阶段是否允许shell工具：1允许，0禁止；生产环境建议关闭")
    private Boolean allowShellInPlanMode;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}