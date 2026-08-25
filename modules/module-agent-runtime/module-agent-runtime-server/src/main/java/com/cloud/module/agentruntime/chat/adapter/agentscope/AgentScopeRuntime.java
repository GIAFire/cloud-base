package com.cloud.module.agentruntime.chat.adapter.agentscope;

import com.cloud.module.agentruntime.chat.domain.ChatActor;
import com.cloud.module.agentruntime.chat.domain.ChatMessage;
import com.cloud.module.agentruntime.chat.domain.ChatRole;
import com.cloud.module.agentruntime.chat.runtime.AgentInvocation;
import com.cloud.module.agentruntime.chat.runtime.AgentRuntime;
import com.cloud.module.agentruntime.chat.runtime.RuntimeEvent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ThinkingBlockDeltaEvent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.harness.agent.HarnessAgent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.util.List;
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
            ChatMessage currentUserMessage =
                    getCurrentUserMessage(invocation);

            RuntimeContext runtimeContext =
                    createRuntimeContext(invocation);

            UserMessage userMessage = new UserMessage("user",currentUserMessage.getContent());

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

    private ChatMessage getCurrentUserMessage(
            AgentInvocation invocation
    ) {
        List<ChatMessage> messages = invocation.getMessages();

        ChatMessage currentMessage =
                messages.get(messages.size() - 1);

        if (currentMessage.getRole() != ChatRole.USER) {
            throw new IllegalStateException(
                    "the current invocation message must be a user message"
            );
        }

        return currentMessage;
    }

    private RuntimeContext createRuntimeContext(
            AgentInvocation invocation
    ) {
        ChatActor actor = invocation.getActor();

        /*
         * AgentScope StateStore 使用 userId + sessionId 隔离上下文。
         */
        return RuntimeContext.builder()
                .userId(String.valueOf(actor.getUserId()))
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
             * 当前基座明确不支持工具、人工确认和外部执行。
             * 如果出现这些事件，说明 Agent 配置发生了预期外变化。
             */
            case TOOL_CALL_START,
                 TOOL_CALL_DELTA,
                 TOOL_CALL_END,
                 TOOL_RESULT_START,
                 TOOL_RESULT_TEXT_DELTA,
                 TOOL_RESULT_DATA_DELTA,
                 TOOL_RESULT_END,
                 REQUIRE_USER_CONFIRM,
                 REQUIRE_EXTERNAL_EXECUTION,
                 USER_CONFIRM_RESULT,
                 EXTERNAL_EXECUTION_RESULT,
                 SUBAGENT_EXPOSED ->
                    sink.error(
                            new IllegalStateException(
                                    "Unsupported AgentScope event: "
                                            + event.getType()
                            )
                    );

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