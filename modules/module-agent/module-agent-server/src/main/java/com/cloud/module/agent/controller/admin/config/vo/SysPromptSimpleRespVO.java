package com.cloud.module.agent.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 系统提示词精简信息 Response VO")
@Data
public class SysPromptSimpleRespVO {

    @Schema(description = "提示词模板主键 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "提示词模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "通用助手")
    private String promptName;

}
