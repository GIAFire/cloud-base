package com.cloud.module.AiFriend.controller.admin.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - Agent会话表：保存用户和Agent之间的一段连续对话 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SessionRespVO {

    @Schema(description = "会话主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6907")
    @ExcelProperty("会话主键ID")
    private Long id;

    @Schema(description = "Agent ID，关联智能体定义表", requiredMode = Schema.RequiredMode.REQUIRED, example = "24937")
    @ExcelProperty("Agent ID，关联智能体定义表")
    private Long agentId;

    @Schema(description = "用户ID，表示该会话归属哪个登录用户", requiredMode = Schema.RequiredMode.REQUIRED, example = "17070")
    @ExcelProperty("用户ID，表示该会话归属哪个登录用户")
    private Long userId;

    @Schema(description = "会话标题，可由第一条用户消息或大模型生成")
    @ExcelProperty("会话标题，可由第一条用户消息或大模型生成")
    private String title;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}