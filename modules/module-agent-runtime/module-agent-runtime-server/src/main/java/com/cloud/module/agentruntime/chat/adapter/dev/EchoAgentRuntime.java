package com.cloud.module.agentruntime.chat.adapter.dev;

import com.cloud.module.agentruntime.chat.domain.ChatMessage;
import com.cloud.module.agentruntime.chat.runtime.AgentInvocation;
import com.cloud.module.agentruntime.chat.runtime.AgentRuntime;
import com.cloud.module.agentruntime.chat.runtime.RuntimeEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Component
@Profile("dev")
@ConditionalOnProperty(
        prefix = "agent.runtime",
        name = "type",
        havingValue = "echo",
        matchIfMissing = true
)
public final class EchoAgentRuntime implements AgentRuntime {

    @Override
    public Flux<RuntimeEvent> stream(
            AgentInvocation invocation
    ) {
        return Flux.defer(() -> {
            List<ChatMessage> messages =
                    invocation.getMessages();

            ChatMessage currentUserMessage =
                    messages.get(messages.size() - 1);

            return Flux.just(
                            RuntimeEvent.started(),
                            RuntimeEvent.thinkingDelta(
                                    "正在理解用户问题..."
                            ),
                            RuntimeEvent.textDelta("Echo: "),
                            RuntimeEvent.textDelta(
                                    currentUserMessage.getContent()
                            ),
                            RuntimeEvent.completed()
                    )
                    .delayElements(Duration.ofMillis(120));
        });
    }
}