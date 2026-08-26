package com.cloud.module.agent.chat.adapter.dev;

import com.cloud.module.agent.chat.runtime.AgentInvocation;
import com.cloud.module.agent.chat.runtime.AgentRuntime;
import com.cloud.module.agent.chat.runtime.RuntimeEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * 开发环境使用的确定性 AgentRuntime Adapter。
 *
 * <p>不访问模型或 AgentScope StateStore，用固定延迟模拟 STARTED、Thinking、
 * 正文和 COMPLETED，便于前端联调 SSE 时序。仅在 dev Profile 且 runtime.type
 * 为 echo 时启用。</p>
 */
@Component
@Profile("dev")
@ConditionalOnProperty(
        prefix = "agent.runtime",
        name = "type",
        havingValue = "echo",
        matchIfMissing = true
)
public final class EchoAgentRuntime implements AgentRuntime {

    /** 返回冷流，确保每次订阅都从完整生命周期重新开始。 */
    @Override
    public Flux<RuntimeEvent> stream(
            AgentInvocation invocation
    ) {
        return Flux.defer(() -> Flux.just(
                            RuntimeEvent.started(),
                            RuntimeEvent.thinkingDelta(
                                    "正在理解用户问题..."
                            ),
                            RuntimeEvent.textDelta("Echo: "),
                            RuntimeEvent.textDelta(
                                    invocation.getContent()
                            ),
                            RuntimeEvent.completed()
                    )
                    .delayElements(Duration.ofMillis(120)));
    }
}
