package com.cloud.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Schema(description = "用户 APP - 邮箱重置密码 Request VO")
@Data
public class AppAuthEmailResetPasswordReqVO {

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    @Size(max = 100, message = "邮箱长度不能超过 100 位")
    private String email;

    @Pattern(regexp = "^[0-9]{6}$", message = "邮箱验证码必须为 6 位数字")
    @NotBlank(message = "邮箱验证码不能为空")
    private String code;

    @Size(min = 8, max = 32, message = "密码长度为 8-32 位")
    @NotBlank(message = "密码不能为空")
    private String password;
}
