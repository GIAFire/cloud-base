package com.cloud.module.member.framework.rpc.config;

import com.cloud.module.system.api.mail.MailSendApi;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.annotation.AnnotatedElementUtils;

import static org.assertj.core.api.Assertions.assertThat;

class RpcConfigurationTest {

    @Test
    void shouldRegisterMailSendApi() {
        EnableFeignClients annotation = AnnotatedElementUtils.findMergedAnnotation(
                RpcConfiguration.class, EnableFeignClients.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.clients()).contains(MailSendApi.class);
    }

}
