package com.cloud.module.agent.controller.admin.agentInfo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - Agent 定义表新增/修改 Request VO")
@Data
public class AgentInfoSaveReqVO {

    @Schema(description = "Agent 主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "9929")
    private Long id;

    @Schema(description = "关联 ai_agent_model.id", example = "20375")
    private Long modelId;

    @Schema(description = "关联 ai_agent_sys_prompt.id", example = "14732")
    private Long sysPromptId;

    @Schema(description = "Agent唯一编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Agent唯一编码不能为空")
    private String agentCode;

    @Schema(description = "Agent 显示名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "Agent 显示名称不能为空")
    private String agentName;

    @Schema(description = "Agent 描述", example = "你说的对")
    private String description;

    @Schema(description = "最大循环次数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "最大循环次数不能为空")
    private Integer maxIters;

    @Schema(description = "默认权限模式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "默认权限模式不能为空")
    private String permissionMode;

    @Schema(description = "是否启用上下文压缩：1启用，0关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否启用上下文压缩：1启用，0关闭不能为空")
    private Boolean compactionEnabled;

    @Schema(description = "触发压缩消息数")
    private Integer triggerMessages;

    @Schema(description = "压缩后保留消息数")
    private Integer keepMessages;

    @Schema(description = "触发压缩token数")
    private Integer triggerTokens;

    @Schema(description = "压缩后保留token数")
    private Integer keepTokens;

    @Schema(description = "是否启用大工具结果卸载：1启用，0关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否启用大工具结果卸载：1启用，0关闭不能为空")
    private Boolean toolResultEvictionEnabled;

    @Schema(description = "是否启用长期记忆:1启用,0禁用")
    private Boolean memoryEnable;

    @Schema(description = "是否启用Plan Mode：1启用，0关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否启用Plan Mode：1启用，0关闭不能为空")
    private Boolean planModeEnabled;

    @Schema(description = "Plan阶段是否允许shell工具：1允许，0禁止；生产环境建议关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Plan阶段是否允许shell工具：1允许，0禁止；生产环境建议关闭不能为空")
    private Boolean allowShellInPlanMode;

    @Schema(description = "人格设定")
    private String personality;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "Agent 分类编号集合")
    private List<@NotNull(message = "分类编号不能为空") Long> categoryIds;

    @Schema(description = "Agent 标签编号集合")
    private List<@NotNull(message = "标签编号不能为空") Long> tagIds;

}
