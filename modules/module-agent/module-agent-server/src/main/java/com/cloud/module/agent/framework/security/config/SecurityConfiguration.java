package com.cloud.module.agent.framework.security.config;

import com.cloud.framework.security.config.AuthorizeRequestsCustomizer;
import com.cloud.module.agent.enums.ApiConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Agent Server 的安全规则补充。
 *
 * <p>仅放行文档、监控和内部 RPC 路径；聊天接口不在白名单中，必须由全局
 * 安全链认证，ChatController 还会再次要求 LoginUser 非空。</p>
 */
@Configuration("agentSecurityConfiguration")
public class SecurityConfiguration {

    /** 注册模块级路径授权规则。 */
    @Bean("agentAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                // Swagger 接口文档
                registry.requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/swagger-ui").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll();
                // Spring Boot Actuator 的安全配置
                registry.requestMatchers("/actuator").permitAll()
                        .requestMatchers("/actuator/**").permitAll();
                // Druid 监控
                registry.requestMatchers("/druid/**").permitAll();
                // RPC 服务的安全配置
                registry.requestMatchers(ApiConstants.PREFIX + "/**").permitAll();
            }

        };
    }

}
