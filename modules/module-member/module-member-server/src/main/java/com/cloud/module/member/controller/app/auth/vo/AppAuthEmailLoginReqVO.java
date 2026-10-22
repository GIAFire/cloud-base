package com.cloud.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "用户 APP - 邮箱密码登录 Request VO")
@Data
public class AppAuthEmailLoginReqVO {

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    @Size(max = 100, message = "邮箱长度不能超过 100 位")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(max = 32, message = "密码长度不能超过 32 位")
    private String password;
}
