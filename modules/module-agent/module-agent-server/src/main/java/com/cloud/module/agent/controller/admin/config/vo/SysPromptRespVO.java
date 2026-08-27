package com.cloud.module.agent.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 系统提示词表 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SysPromptRespVO {

    @Schema(description = "提示词模板主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2331")
    @ExcelProperty("提示词模板主键ID")
    private Long id;

    @Schema(description = "提示词模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("提示词模板名称")
    private String promptName;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "随便")
    @ExcelProperty("描述")
    private String description;

    @Schema(description = "提示词内容")
    @ExcelProperty("提示词内容")
    private String sysPrompt;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}