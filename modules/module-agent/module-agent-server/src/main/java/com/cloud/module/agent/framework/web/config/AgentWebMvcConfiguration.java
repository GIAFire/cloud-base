package com.cloud.module.agent.framework.web.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Agent SSE 的 Spring MVC 异步支持配置。
 *
 * <p>显式线程池替代 MVC 默认的简单执行器，提供容量上限、可观测线程名和
 * 明确拒绝策略；默认超时作用于整个流式 HTTP 请求。</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AgentSseProperties.class)
public class AgentWebMvcConfiguration {

    /** SSE AsyncTaskExecutor Bean 名称。 */
    public static final String SSE_TASK_EXECUTOR = "agentSseTaskExecutor";

    /** 创建有界的 Servlet 异步执行器。 */
    @Bean(SSE_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor agentSseTaskExecutor(
            AgentSseProperties properties
    ) {
        Assert.isTrue(
                properties.getMaxPoolSize() >= properties.getCorePoolSize(),
                "agent.sse.max-pool-size must be greater than or equal to core-pool-size"
        );

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
        executor.setAllowCoreThreadTimeOut(true);
        executor.setThreadNamePrefix("agent-sse-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }

    /** 把专用执行器和请求超时安装到 Spring MVC 异步支持。 */
    @Bean
    public WebMvcConfigurer agentSseWebMvcConfigurer(
            @Qualifier(SSE_TASK_EXECUTOR) AsyncTaskExecutor taskExecutor,
            AgentSseProperties properties
    ) {
        return new SseAsyncWebMvcConfigurer(taskExecutor, properties.getRequestTimeout());
    }

    /** 只修改异步执行参数的最小 WebMvcConfigurer。 */
    private static final class SseAsyncWebMvcConfigurer implements WebMvcConfigurer, Ordered {

        private final AsyncTaskExecutor taskExecutor;
        private final long requestTimeoutMillis;

        private SseAsyncWebMvcConfigurer(
                AsyncTaskExecutor taskExecutor,
                java.time.Duration requestTimeout
        ) {
            Assert.isTrue(!requestTimeout.isNegative() && !requestTimeout.isZero(),
                    "agent.sse.request-timeout must be greater than zero");
            this.taskExecutor = taskExecutor;
            this.requestTimeoutMillis = requestTimeout.toMillis();
        }

        /** 设置全局 MVC 异步执行器和默认超时。 */
        @Override
        public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
            configurer.setTaskExecutor(taskExecutor);
            configurer.setDefaultTimeout(requestTimeoutMillis);
        }

        /** 最后应用，减少覆盖其他模块 MVC 定制的风险。 */
        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }
}
