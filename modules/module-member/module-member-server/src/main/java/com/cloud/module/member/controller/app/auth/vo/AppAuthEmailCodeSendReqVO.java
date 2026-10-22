package com.cloud.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Schema(description = "用户 APP - 发送邮箱验证码 Request VO")
@Data
public class AppAuthEmailCodeSendReqVO {

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    @Size(max = 100, message = "邮箱长度不能超过 100 位")
    private String email;

    @NotNull(message = "验证码场景不能为空")
    @Min(value = 1, message = "验证码场景不正确")
    @Max(value = 2, message = "验证码场景不正确")
    private Integer scene;
}
