package com.cloud.module.agent.controller.admin.chat.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 流式聊天 HTTP 请求。
 *
 * <p>Long 标识使用 String 接收，先通过 Bean Validation 限定为正十进制数，
 * 再由 Controller 转换，避免 JavaScript 大整数精度问题。</p>
 */
@Data
public class ChatStreamRequest {

    /**
     * 第一次对话可以不传。
     */
    @Pattern(regexp = "^[1-9]\\d*$",message = "sessionId 格式不正确")
    @DecimalMax(value = "9223372036854775807",message = "sessionId 超出有效范围")
    private String sessionId;

    /**
     * 前端生成的幂等请求标识。
     */
    @NotBlank(message = "requestId 不能为空")
    @Size(max = 64,message = "requestId 长度不能超过 64")
    private String requestId;

    /** 当前用户文本；限制长度以保护模型和持久化链路。 */
    @NotBlank(message = "content 不能为空")
    @Size(max = 32_768,message = "content 长度不能超过 32768")
    private String content;
}
