package com.cloud.module.member.service.auth;

import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.member.dal.mysql.auth.MemberEmailCodeMapper;
import com.cloud.module.system.api.mail.MailSendApi;
import com.cloud.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberEmailCodeServiceTest {

    private final List<MailSendSingleToUserReqDTO> sent = new ArrayList<>();
    private boolean mailFailure;
    private final MemberEmailCodeServiceImpl service = new MemberEmailCodeServiceImpl();

    @BeforeEach
    void set_up() {
        MemberEmailCodeMapper mapper = (MemberEmailCodeMapper) Proxy.newProxyInstance(
                MemberEmailCodeMapper.class.getClassLoader(), new Class<?>[]{MemberEmailCodeMapper.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "selectLastByEmail" -> null;
                    case "selectCountSince" -> 0L;
                    case "insert" -> 1;
                    case "toString" -> "EmailCodeMapperTestPort";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        MailSendApi mailApi = new MailSendApi() {
            @Override
            public CommonResult<Long> sendSingleMailToAdmin(MailSendSingleToUserReqDTO request) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CommonResult<Long> sendSingleMailToMember(MailSendSingleToUserReqDTO request) {
                sent.add(request);
                return mailFailure ? CommonResult.error(500, "mail unavailable") : CommonResult.success(1L);
            }
        };
        PasswordEncoder encoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence raw) { return "encoded-code"; }
            @Override
            public boolean matches(CharSequence raw, String encoded) { return false; }
        };
        ReflectionTestUtils.setField(service, "emailCodeMapper", mapper);
        ReflectionTestUtils.setField(service, "mailSendApi", mailApi);
        ReflectionTestUtils.setField(service, "passwordEncoder", encoder);
    }

    @Test
    void sends_registration_code_through_member_mail_api() {
        service.sendCode(" Member@Example.Invalid ", 1, "127.0.0.1");

        assertThat(sent).hasSize(1);
        assertThat(sent.getFirst().getToMails()).containsExactly("member@example.invalid");
        assertThat(sent.getFirst().getTemplateCode()).isEqualTo("member-email-register-code");
        assertThat(sent.getFirst().getTemplateParams().get("code")).asString().matches("[0-9]{6}");
    }

    @Test
    void uses_password_reset_template_and_propagates_mail_failure() {
        mailFailure = true;
        assertThatThrownBy(() -> service.sendCode("member@example.invalid", 2, "127.0.0.1"))
                .hasMessageContaining("mail unavailable");
        assertThat(sent).hasSize(1);
        assertThat(sent.getFirst().getTemplateCode()).isEqualTo("member-email-reset-password-code");
    }
}
