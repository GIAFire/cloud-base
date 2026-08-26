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

/**
 * 使用 AgentScope HarnessAgent 实现 AgentRuntime 的 Adapter。
 *
 * <p>该类只负责三件事：把业务身份转换为 RuntimeContext、把当前用户文本
 * 转换为 UserMessage、把 AgentEvent 压缩为模块认可的 RuntimeEvent。历史
 * 对话由 HarnessAgent 依据 RuntimeContext 从 AgentScope StateStore 管理。</p>
 *
 * <p>HarnessAgent 是线程安全的单例资源，本 Adapter 不在单次调用结束时关闭
 * 它。AgentScope 异常和取消信号保持 Reactor 原语义向上传播。</p>
 */
public final class AgentScopeRuntime implements AgentRuntime {

    /** 由 Spring 容器管理生命周期的单例 HarnessAgent。 */
    private final HarnessAgent harnessAgent;

    public AgentScopeRuntime(HarnessAgent harnessAgent) {
        this.harnessAgent = Objects.requireNonNull(harnessAgent, "harnessAgent 不能为空");
    }

    /**
     * 为每次订阅延迟创建 RuntimeContext 和 UserMessage，避免未订阅时修改
     * AgentScope 会话状态。
     */
    @Override
    public Flux<RuntimeEvent> stream(AgentInvocation invocation) {
        Objects.requireNonNull(invocation, "invocation must not be null");

        return Flux.defer(() -> {
            RuntimeContext runtimeContext = createRuntimeContext(invocation);

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

    /**
     * 使用“tenant-user + session”构造状态槽，保证同一 Agent 下多租户隔离。
     */
    private RuntimeContext createRuntimeContext(AgentInvocation invocation) {
        ChatActor actor = invocation.getActor();

        /*
         * AgentScope StateStore 使用 userId + sessionId 隔离上下文。
         */
        return RuntimeContext.builder()
                .userId(actor.getTenantId() + "-" + actor.getUserId())
                .sessionId(String.valueOf(invocation.getSessionId()))
                .build();
    }

    /**
     * 将 AgentScope 丰富事件集投影为聊天基座的最小事件协议。
     * ModelCall、BlockStart/End 等观测事件留在 Adapter 内部，不向应用层扩散。
     */
    private void mapAgentEvent(AgentEvent event, SynchronousSink<RuntimeEvent> sink) {
        switch (event.getType()) {
            case AGENT_START -> sink.next(RuntimeEvent.started());

            case THINKING_BLOCK_DELTA ->
                    mapThinkingDelta((ThinkingBlockDeltaEvent) event, sink);

            case TEXT_BLOCK_DELTA ->
                    mapTextDelta((TextBlockDeltaEvent) event, sink);

            case AGENT_END ->
                    sink.next(RuntimeEvent.completed());
            /*
             * ModelCall、BlockStart、BlockEnd 等内部事件暂时不向上暴露。
             */
            default -> {
            }
        }
    }

    /** 忽略供应商可能发送的空 Thinking chunk。 */
    private void mapThinkingDelta(
            ThinkingBlockDeltaEvent event,
            SynchronousSink<RuntimeEvent> sink
    ) {
        String delta = event.getDelta();

        if (delta != null && !delta.isEmpty()) {
            sink.next(RuntimeEvent.thinkingDelta(delta));
        }
    }

    /** 忽略供应商可能发送的空文本 chunk。 */
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
