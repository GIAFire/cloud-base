package com.cloud.module.agent.controller.admin.chat.vo;

import com.cloud.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 管理后台 - 当前用户会话分页请求。 */
@Schema(description = "管理后台 - 当前用户会话分页请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatSessionPageRequest extends PageParam {

    @Schema(description = "Agent ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "agentId 不能为空")
    @Pattern(regexp = "^[1-9]\\d*$", message = "agentId 格式不正确")
    @DecimalMax(value = "9223372036854775807", message = "agentId 超出有效范围")
    private String agentId;
}
