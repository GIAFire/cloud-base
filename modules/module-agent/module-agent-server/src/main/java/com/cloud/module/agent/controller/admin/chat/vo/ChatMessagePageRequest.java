package com.cloud.module.agent.controller.admin.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 管理后台 - 会话消息游标请求。 */
@Schema(description = "管理后台 - 会话消息游标请求")
@Data
public class ChatMessagePageRequest {

    @Schema(description = "会话 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "sessionId 不能为空")
    @Pattern(regexp = "^[1-9]\\d*$", message = "sessionId 格式不正确")
    @DecimalMax(value = "9223372036854775807", message = "sessionId 超出有效范围")
    private String sessionId;

    @Schema(description = "查询该消息序号之前的消息；首次不传")
    @Pattern(regexp = "^[1-9]\\d*$", message = "beforeSeq 格式不正确")
    @DecimalMax(value = "9223372036854775807", message = "beforeSeq 超出有效范围")
    private String beforeSeq;

    @Schema(description = "本次查询数量", defaultValue = "50")
    @Min(value = 1, message = "pageSize 最小值为 1")
    @Max(value = 100, message = "pageSize 最大值为 100")
    private Integer pageSize = 50;
}
