package com.cloud.module.agent.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 模型配置精简信息 Response VO")
@Data
public class ModelSimpleRespVO {

    @Schema(description = "模型配置主键 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "qwen-plus")
    private String modelName;

}
