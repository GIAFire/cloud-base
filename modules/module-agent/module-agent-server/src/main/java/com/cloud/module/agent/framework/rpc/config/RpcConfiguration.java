package com.cloud.module.agent.framework.rpc.config;

import com.cloud.module.system.api.logger.LoginLogApi;
import com.cloud.module.system.api.sms.SmsCodeApi;
import com.cloud.module.system.api.social.SocialClientApi;
import com.cloud.module.system.api.social.SocialUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Agent Server 所需系统模块 Feign 客户端的显式注册。
 *
 * <p>使用 clients 白名单，避免扫描并实例化本模块不使用的远程接口。</p>
 */
@Configuration(value = "agentRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {SmsCodeApi.class, LoginLogApi.class, SocialUserApi.class, SocialClientApi.class})
public class RpcConfiguration {
}
