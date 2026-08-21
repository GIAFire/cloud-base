package com.cloud.module.AiFriend.controller.admin.chat.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.cloud.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.cloud.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - Agent会话表：保存用户和Agent之间的一段连续对话分页 Request VO")
@Data
public class SessionPageReqVO extends PageParam {

    @Schema(description = "Agent ID，关联智能体定义表", example = "24937")
    private Long agentId;

    @Schema(description = "用户ID，表示该会话归属哪个登录用户", example = "17070")
    private Long userId;

    @Schema(description = "会话标题，可由第一条用户消息或大模型生成")
    private String title;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}