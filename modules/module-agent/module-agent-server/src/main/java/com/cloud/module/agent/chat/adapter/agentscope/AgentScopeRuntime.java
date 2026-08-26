package com.cloud.module.agent.chat.adapter.agentscope;

import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.runtime.AgentInvocation;
import com.cloud.module.agent.chat.runtime.AgentRuntime;
import com.cloud.module.agent.chat.runtime.RuntimeEvent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ThinkingBlockDeltaEvent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.harness.agent.HarnessAgent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.util.Objects;

public final class AgentScopeRuntime implements AgentRuntime {

    private final HarnessAgent harnessAgent;

    public AgentScopeRuntime(HarnessAgent harnessAgent) {
        this.harnessAgent = Objects.requireNonNull(
                harnessAgent,
                "harnessAgent must not be null"
        );
    }

    @Override
    public Flux<RuntimeEvent> stream(
            AgentInvocation invocation
    ) {
        Objects.requireNonNull(
                invocation,
                "invocation must not be null"
        );

        return Flux.defer(() -> {
            RuntimeContext runtimeContext =
                    createRuntimeContext(invocation);

            UserMessage userMessage = new UserMessage("user", invocation.getContent());

            /*
             * 这里只传当前用户消息。
             *
             * HarnessAgent 会根据 RuntimeContext 中的 userId、sessionId，
             * 从 AgentScope StateStore 加载之前的 AgentState，
             * 并在运行完成后保存新的上下文。
             */
            return harnessAgent
                    .streamEvents(userMessage, runtimeContext)
                    .<RuntimeEvent>handle(this::mapAgentEvent);
        });
    }

    private RuntimeContext createRuntimeContext(
            AgentInvocation invocation
    ) {
        ChatActor actor = invocation.getActor();

        /*
         * AgentScope StateStore 使用 userId + sessionId 隔离上下文。
         */
        return RuntimeContext.builder()
                .userId(
                        actor.getTenantId()
                                + "-"
                                + actor.getUserId()
                )
                .sessionId(String.valueOf(invocation.getSessionId()))
                .build();
    }

    private void mapAgentEvent(
            AgentEvent event,
            SynchronousSink<RuntimeEvent> sink
    ) {
        switch (event.getType()) {
            case AGENT_START ->
                    sink.next(RuntimeEvent.started());

            case THINKING_BLOCK_DELTA ->
                    mapThinkingDelta(
                            (ThinkingBlockDeltaEvent) event,
                            sink
                    );

            case TEXT_BLOCK_DELTA ->
                    mapTextDelta(
                            (TextBlockDeltaEvent) event,
                            sink
                    );

            case AGENT_END ->
                    sink.next(RuntimeEvent.completed());
            /*
             * ModelCall、BlockStart、BlockEnd 等内部事件暂时不向上暴露。
             */
            default -> {
            }
        }
    }

    private void mapThinkingDelta(
            ThinkingBlockDeltaEvent event,
            SynchronousSink<RuntimeEvent> sink
    ) {
        String delta = event.getDelta();

        if (delta != null && !delta.isEmpty()) {
            sink.next(RuntimeEvent.thinkingDelta(delta));
        }
    }

    private void mapTextDelta(
            TextBlockDeltaEvent event,
            SynchronousSink<RuntimeEvent> sink
    ) {
        String delta = event.getDelta();

        if (delta != null && !delta.isEmpty()) {
            sink.next(RuntimeEvent.textDelta(delta));
        }
    }
}
