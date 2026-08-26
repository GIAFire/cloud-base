package com.cloud.module.agent.chat.adapter.agentscope;

import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.runtime.AgentInvocation;
import com.cloud.module.agent.chat.runtime.AgentRuntime;
import com.cloud.module.agent.chat.runtime.RuntimeEvent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ModelCallEndEvent;
import io.agentscope.core.event.ModelCallStartEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ThinkingBlockDeltaEvent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.SynchronousSink;

import java.util.Objects;

import static com.cloud.module.agent.chat.observability.ChatTiming.elapsedMillis;

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
@Slf4j
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
            RuntimeTimingState timingState = new RuntimeTimingState(invocation);

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
                    .<RuntimeEvent>handle((event, sink) ->
                            mapAgentEvent(event, sink, timingState))
                    .doFinally(timingState::finishInvocation);
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
    private void mapAgentEvent(
            AgentEvent event,
            SynchronousSink<RuntimeEvent> sink,
            RuntimeTimingState timingState
    ) {
        switch (event.getType()) {
            case AGENT_START -> sink.next(RuntimeEvent.started());

            case MODEL_CALL_START ->
                    timingState.startModelCall((ModelCallStartEvent) event);

            case MODEL_CALL_END ->
                    timingState.finishModelCall((ModelCallEndEvent) event);

            case THINKING_BLOCK_DELTA -> {
                ThinkingBlockDeltaEvent deltaEvent = (ThinkingBlockDeltaEvent) event;
                if (hasText(deltaEvent.getDelta())) {
                    timingState.firstTokenEmitted();
                }
                mapThinkingDelta(deltaEvent, sink);
            }

            case TEXT_BLOCK_DELTA -> {
                TextBlockDeltaEvent deltaEvent = (TextBlockDeltaEvent) event;
                if (hasText(deltaEvent.getDelta())) {
                    timingState.firstTokenEmitted();
                }
                mapTextDelta(deltaEvent, sink);
            }

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

    /** 判断框架增量是否代表真实可见的 Token 内容。 */
    private boolean hasText(String delta) {
        return delta != null && !delta.isEmpty();
    }

    /**
     * 单次 AgentInvocation 的模型计时状态。
     *
     * <p>MODEL_CALL_START/END 可能出现多次，因此模型指标按 callIndex 分别
     * 记录；接口入口到首 Token 只在整个请求的第一段有效内容到达时记录一次。</p>
     */
    private static final class RuntimeTimingState {

        private final AgentInvocation invocation;

        private int modelCallIndex;

        private long modelCallStartedNanos;

        private String replyId;

        private boolean modelCallActive;

        private boolean modelFirstTokenLogged;

        private boolean apiFirstTokenLogged;

        private RuntimeTimingState(AgentInvocation invocation) {
            this.invocation = invocation;
        }

        /** 开始计量一次真实模型调用。 */
        private void startModelCall(ModelCallStartEvent event) {
            if (modelCallActive) {
                logModelCallFinished("REPLACED_BY_NEXT_MODEL_CALL");
            }

            modelCallIndex++;
            modelCallStartedNanos = System.nanoTime();
            replyId = event.getReplyId();
            modelCallActive = true;
            modelFirstTokenLogged = false;
        }

        /** 在首个非空 Thinking/Text delta 到达时记录两种首 Token 指标。 */
        private void firstTokenEmitted() {
            if (!apiFirstTokenLogged) {
                apiFirstTokenLogged = true;
                log.info(
                        "chat-timing metric=chat.api-to-model-first-token.duration durationMs={} "
                                + "outcome=FIRST_TOKEN tenantId={} userId={} sessionId={} runId={}",
                        elapsedMillis(invocation.getRequestStartedNanos()),
                        invocation.getActor().getTenantId(),
                        invocation.getActor().getUserId(),
                        invocation.getSessionId(),
                        invocation.getRunId()
                );
            }

            if (modelCallActive && !modelFirstTokenLogged) {
                modelFirstTokenLogged = true;
                log.info(
                        "chat-timing metric=agent.model.first-token.duration durationMs={} "
                                + "outcome=FIRST_TOKEN callIndex={} replyId={} sessionId={} runId={}",
                        elapsedMillis(modelCallStartedNanos),
                        modelCallIndex,
                        replyId,
                        invocation.getSessionId(),
                        invocation.getRunId()
                );
            }
        }

        /** 正常结束一次模型调用并记录完整调用时间。 */
        private void finishModelCall(ModelCallEndEvent event) {
            if (!modelCallActive) {
                return;
            }

            if (event.getReplyId() != null) {
                replyId = event.getReplyId();
            }
            logModelCallFinished("SUCCESS");
        }

        /**
         * 流失败或取消时补记没有正常收到 MODEL_CALL_END 的模型调用。
         */
        private void finishInvocation(SignalType signalType) {
            if (modelCallActive) {
                logModelCallFinished(signalType.name());
            }

            if (!apiFirstTokenLogged) {
                log.info(
                        "chat-timing metric=chat.api-to-model-first-token.duration durationMs={} "
                                + "outcome=NO_TOKEN_{} tenantId={} userId={} sessionId={} runId={}",
                        elapsedMillis(invocation.getRequestStartedNanos()),
                        signalType,
                        invocation.getActor().getTenantId(),
                        invocation.getActor().getUserId(),
                        invocation.getSessionId(),
                        invocation.getRunId()
                );
            }
        }

        /** 输出模型首 Token 缺失状态和模型总耗时，并关闭当前调用计时。 */
        private void logModelCallFinished(String outcome) {
            if (!modelFirstTokenLogged) {
                log.info(
                        "chat-timing metric=agent.model.first-token.duration durationMs={} "
                                + "outcome=NO_TOKEN_{} callIndex={} replyId={} sessionId={} runId={}",
                        elapsedMillis(modelCallStartedNanos),
                        outcome,
                        modelCallIndex,
                        replyId,
                        invocation.getSessionId(),
                        invocation.getRunId()
                );
            }

            log.info(
                    "chat-timing metric=agent.model.total.duration durationMs={} outcome={} "
                            + "callIndex={} replyId={} sessionId={} runId={}",
                    elapsedMillis(modelCallStartedNanos),
                    outcome,
                    modelCallIndex,
                    replyId,
                    invocation.getSessionId(),
                    invocation.getRunId()
            );

            modelCallActive = false;
        }
    }
}
