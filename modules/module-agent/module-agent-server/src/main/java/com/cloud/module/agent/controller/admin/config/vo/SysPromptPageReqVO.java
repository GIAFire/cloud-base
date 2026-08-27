package com.cloud.module.agent.controller.admin.config.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.cloud.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.cloud.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 系统提示词表分页 Request VO")
@Data
public class SysPromptPageReqVO extends PageParam {

    @Schema(description = "提示词模板名称", example = "王五")
    private String promptName;

    @Schema(description = "描述", example = "随便")
    private String description;

    @Schema(description = "提示词内容")
    private String sysPrompt;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}