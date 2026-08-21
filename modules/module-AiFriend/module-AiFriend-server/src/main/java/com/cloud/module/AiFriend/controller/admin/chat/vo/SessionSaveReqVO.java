package com.cloud.module.AiFriend.controller.admin.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import com.cloud.module.AiFriend.dal.dataobject.chat.MessageDO;

@Schema(description = "管理后台 - Agent会话表：保存用户和Agent之间的一段连续对话新增/修改 Request VO")
@Data
public class SessionSaveReqVO {

    @Schema(description = "会话主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6907")
    private Long id;

    @Schema(description = "Agent ID，关联智能体定义表", requiredMode = Schema.RequiredMode.REQUIRED, example = "24937")
    @NotNull(message = "Agent ID，关联智能体定义表不能为空")
    private Long agentId;

    @Schema(description = "用户ID，表示该会话归属哪个登录用户", requiredMode = Schema.RequiredMode.REQUIRED, example = "17070")
    @NotNull(message = "用户ID，表示该会话归属哪个登录用户不能为空")
    private Long userId;

    @Schema(description = "会话标题，可由第一条用户消息或大模型生成")
    private String title;

    @Schema(description = "Agent消息表：保存输入、思考、正文等消息列表")
    private List<MessageDO> messages;

}