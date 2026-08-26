package com.cloud.module.agent.framework.web.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "agent.runtime.sse")
public class AgentRuntimeSseProperties {

    @Min(1)
    private int corePoolSize = 16;

    @Min(1)
    private int maxPoolSize = 64;

    @Min(0)
    private int queueCapacity = 256;

    @Min(1)
    private int keepAliveSeconds = 60;

    @NotNull
    private Duration requestTimeout = Duration.ofMinutes(10);
}
