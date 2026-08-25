package com.cloud.module.agentruntime.controller.admin.chat.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatStreamRequest {

    /**
     * 第一次对话可以不传。
     */
    @Pattern(regexp = "^[1-9]\\d*$",message = "sessionId 格式不正确")
    private String sessionId;

    /**
     * 前端生成的幂等请求标识。
     */
    @NotBlank(message = "requestId 不能为空")
    @Size(max = 64,message = "requestId 长度不能超过 64")
    private String requestId;

    @NotBlank(message = "content 不能为空")
    @Size(max = 32_768,message = "content 长度不能超过 32768")
    private String content;
}