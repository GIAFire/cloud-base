package com.cloud.module.agent.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 系统提示词表新增/修改 Request VO")
@Data
public class SysPromptSaveReqVO {

    @Schema(description = "提示词模板主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2331")
    private Long id;

    @Schema(description = "提示词模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "提示词模板名称不能为空")
    private String promptName;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "随便")
    @NotEmpty(message = "描述不能为空")
    private String description;

    @Schema(description = "提示词内容")
    private String sysPrompt;

}