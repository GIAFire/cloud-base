package com.cloud.module.agent.chat.adapter.agentscope;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/** 与具体 Agent 无关的运行策略配置。 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "agent.runtime")
public class AgentRuntimeProperties {

    /** 单次模型 HTTP 请求超时。 */
    @NotNull
    private Duration modelTimeout = Duration.ofMinutes(2);

    /** 包含首次请求在内的最大尝试次数。 */
    @Min(1)
    private int modelMaxAttempts = 1;

    /** AgentScope 会话状态根目录。 */
    @NotNull
    private Path stateRoot = Paths.get(
            System.getProperty("user.home"),
            ".agentscope",
            "state"
    );

    /** Bean Validation 不直接支持 Duration 正数约束，因此显式校验。 */
    @AssertTrue(message = "agent.runtime.model-timeout 必须大于 0")
    public boolean isModelTimeoutPositive() {
        return modelTimeout != null
                && !modelTimeout.isZero()
                && !modelTimeout.isNegative();
    }
}
