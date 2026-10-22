package com.cloud.module.trade.framework.aftersale.config;

import com.cloud.module.trade.framework.aftersale.core.aop.AfterSaleLogAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * trade 模块的 afterSaleLog 组件的 Configuration
 *
 * @author 陈賝
 * @since 2023/6/18 11:09
 */
@Configuration(proxyBeanMethods = false)
public class AfterSaleLogConfiguration {

    @Bean
    public AfterSaleLogAspect afterSaleLogAspect() {
        return new AfterSaleLogAspect();
    }

}
