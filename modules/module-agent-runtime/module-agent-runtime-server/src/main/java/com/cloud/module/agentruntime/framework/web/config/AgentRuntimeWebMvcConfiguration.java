package com.cloud.module.agentruntime.framework.web.config;

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

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AgentRuntimeSseProperties.class)
public class AgentRuntimeWebMvcConfiguration {

    public static final String SSE_TASK_EXECUTOR = "agentRuntimeSseTaskExecutor";

    @Bean(SSE_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor agentRuntimeSseTaskExecutor(
            AgentRuntimeSseProperties properties
    ) {
        Assert.isTrue(
                properties.getMaxPoolSize() >= properties.getCorePoolSize(),
                "agent.runtime.sse.max-pool-size must be greater than or equal to core-pool-size"
        );

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
        executor.setAllowCoreThreadTimeOut(true);
        executor.setThreadNamePrefix("agent-runtime-sse-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }

    @Bean
    public WebMvcConfigurer agentRuntimeSseWebMvcConfigurer(
            @Qualifier(SSE_TASK_EXECUTOR) AsyncTaskExecutor taskExecutor,
            AgentRuntimeSseProperties properties
    ) {
        return new SseAsyncWebMvcConfigurer(taskExecutor, properties.getRequestTimeout());
    }

    private static final class SseAsyncWebMvcConfigurer implements WebMvcConfigurer, Ordered {

        private final AsyncTaskExecutor taskExecutor;
        private final long requestTimeoutMillis;

        private SseAsyncWebMvcConfigurer(
                AsyncTaskExecutor taskExecutor,
                java.time.Duration requestTimeout
        ) {
            Assert.isTrue(!requestTimeout.isNegative() && !requestTimeout.isZero(),
                    "agent.runtime.sse.request-timeout must be greater than zero");
            this.taskExecutor = taskExecutor;
            this.requestTimeoutMillis = requestTimeout.toMillis();
        }

        @Override
        public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
            configurer.setTaskExecutor(taskExecutor);
            configurer.setDefaultTimeout(requestTimeoutMillis);
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }
}
