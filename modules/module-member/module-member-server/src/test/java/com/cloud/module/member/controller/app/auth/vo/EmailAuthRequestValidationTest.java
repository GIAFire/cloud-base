package com.cloud.module.member.controller.app.auth.vo;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import com.cloud.module.system.api.logger.dto.LoginLogCreateReqDTO;

import static org.assertj.core.api.Assertions.assertThat;

class EmailAuthRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void accepts_valid_registration_request() {
        AppAuthEmailRegisterReqVO request = new AppAuthEmailRegisterReqVO();
        request.setEmail("member@example.com");
        request.setCode("123456");
        request.setPassword("12345678");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejects_invalid_email_code_and_short_password() {
        AppAuthEmailRegisterReqVO request = new AppAuthEmailRegisterReqVO();
        request.setEmail("not-an-email");
        request.setCode("12345");
        request.setPassword("short");

        assertThat(validator.validate(request)).hasSize(3);
    }

    @Test
    void accepts_password_without_character_class_requirements() {
        AppAuthEmailResetPasswordReqVO request = new AppAuthEmailResetPasswordReqVO();
        request.setEmail("member@example.com");
        request.setCode("123456");
        request.setPassword("!!!!!!!!");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void restricts_email_code_scene() {
        AppAuthEmailCodeSendReqVO request = new AppAuthEmailCodeSendReqVO();
        request.setEmail("member@example.com");
        request.setScene(3);

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void accepts_full_length_email_in_login_audit() {
        LoginLogCreateReqDTO request = new LoginLogCreateReqDTO();
        request.setLogType(104);
        request.setUserType(1);
        request.setResult(10);
        request.setUserIp("127.0.0.1");
        request.setUsername("a".repeat(88) + "@example.com");

        assertThat(validator.validate(request)).isEmpty();
    }
}
