package com.cloud.module.agentruntime.chat.adapter.dev;

import com.cloud.module.agentruntime.chat.domain.ChatMessage;
import com.cloud.module.agentruntime.chat.runtime.AgentInvocation;
import com.cloud.module.agentruntime.chat.runtime.AgentRuntime;
import com.cloud.module.agentruntime.chat.runtime.RuntimeEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Component
@Profile("dev")
public final class EchoAgentRuntime implements AgentRuntime {

    @Override
    public Flux<RuntimeEvent> stream(
            AgentInvocation invocation
    ) {
        return Flux.defer(() -> {
            List<ChatMessage> messages = invocation.getMessages();

            ChatMessage currentUserMessage = messages.get(messages.size() - 1);

            return Flux.just(
                            RuntimeEvent.started(),
                            RuntimeEvent.textDelta("Echo: "),
                            RuntimeEvent.textDelta("Echo: 1"),
                            RuntimeEvent.textDelta("Echo: 2"),
                            RuntimeEvent.textDelta("Echo: 3"),
                            RuntimeEvent.textDelta("Echo: 4"),
                            RuntimeEvent.textDelta(
                                    currentUserMessage.getContent()
                            ),
                            RuntimeEvent.completed()
                    )
                    /*
                     * 仅用于开发环境观察流式效果。
                     */
                    .delayElements(Duration.ofMillis(120));
        });
    }
}