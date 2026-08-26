package com.cloud.module.agent.framework.web.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Spring MVC 异步 SSE 执行参数。
 *
 * <p>该线程池承载 Servlet 异步请求生命周期，不用于执行阻塞 JDBC；MyBatis
 * 由 MybatisChatStore 单独调度到 boundedElastic。</p>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "agent.sse")
public class AgentSseProperties {

    /** 常驻 SSE 工作线程数。 */
    @Min(1)
    private int corePoolSize = 16;

    /** 高峰期允许扩展到的最大线程数。 */
    @Min(1)
    private int maxPoolSize = 64;

    /** 等待执行的异步请求容量；满载后执行拒绝策略。 */
    @Min(0)
    private int queueCapacity = 256;

    /** 空闲扩展线程的存活秒数。 */
    @Min(1)
    private int keepAliveSeconds = 60;

    /** 单个 SSE 请求的 Servlet 异步超时。 */
    @NotNull
    private Duration requestTimeout = Duration.ofMinutes(10);
}
