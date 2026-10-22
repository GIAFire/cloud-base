package com.cloud.module.member.framework.rpc.config;

import com.cloud.module.system.api.logger.LoginLogApi;
import com.cloud.module.system.api.mail.MailSendApi;
import com.cloud.module.system.api.sms.SmsCodeApi;
import com.cloud.module.system.api.social.SocialClientApi;
import com.cloud.module.system.api.social.SocialUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "memberRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {SmsCodeApi.class, LoginLogApi.class, SocialUserApi.class, SocialClientApi.class,
        MailSendApi.class})
public class RpcConfiguration {
}
