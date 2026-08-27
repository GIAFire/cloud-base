package com.cloud.module.agent.chat.application;

import com.cloud.module.agent.chat.application.command.StartChatCommand;
import com.cloud.module.agent.chat.application.model.RunFinalization;
import com.cloud.module.agent.chat.application.model.StartedRun;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.domain.ChatEvent;
import com.cloud.module.agent.chat.runtime.AgentInvocation;
import com.cloud.module.agent.chat.runtime.AgentRuntime;
import com.cloud.module.agent.chat.runtime.RuntimeEvent;
import com.cloud.module.agent.chat.store.ChatStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.cloud.module.agent.chat.observability.ChatTiming.elapsedMillis;

/**
 * 默认聊天应用实现，是完整运行链路的编排中心。
 *
 * <p>该类刻意不包含 AgentScope、MyBatis 或 HTTP 类型，只依赖 AgentRuntime
 * 和 ChatStore 两个 seam。一次订阅依次执行：</p>
 *
 * <ol>
 *     <li>ChatStore.begin 原子创建/校验 Session 和 Run，并保存用户消息；</li>
 *     <li>AgentRuntime.stream 启动框架事件流；</li>
 *     <li>校验运行时事件顺序并转换为 ChatEvent；</li>
 *     <li>成功、失败或取消时通过 ChatStore.finish 可靠收尾；</li>
 *     <li>仅在收尾成功后向客户端发布终止事件。</li>
 * </ol>
 *
 * <p>应用层不信任 Runtime 自然完成：正常流必须显式发布 STARTED 和
 * COMPLETED，并且至少产生一段非空助手正文。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class DefaultChatApplication implements ChatApplication {

    /** 对外稳定、不会泄漏模型细节的运行错误码。 */
    private static final String RUNTIME_ERROR_CODE = "AGENT_RUNTIME_ERROR";

    /** 对外稳定、已脱敏的运行错误文案。 */
    private static final String RUNTIME_ERROR_MESSAGE = "智能体运行失败，请稍后重试";

    /** Agent 框架 seam，由 AgentScope Adapter 实现。 */
    private final AgentRuntime agentRuntime;

    /** 业务状态 seam，可由内存或 MySQL Adapter 实现。 */
    private final ChatStore chatStore;

    /**
     * 延迟到订阅时才执行 begin，避免 Controller 构造 Flux 时提前产生数据库
     * 副作用。begin 返回后，Run 已经处于 RUNNING 状态。
     */
    @Override
    public Flux<ChatEvent> stream(StartChatCommand command) {
        Objects.requireNonNull(command, "command 不能为空");

        return Flux.defer(() -> {
            long beginStartedNanos = System.nanoTime();
            AtomicReference<StartedRun> observedRun = new AtomicReference<>();

            return chatStore.begin(command)
                    .switchIfEmpty(Mono.error(new IllegalStateException("ChatStore.begin 不能为空")))
                    .doOnNext(observedRun::set)
                    .doFinally(signalType -> log.info(
                            "chat-timing metric=chat.store.begin.duration durationMs={} outcome={} "
                                    + "tenantId={} userId={} requestedSessionId={} sessionId={} runId={}",
                            elapsedMillis(beginStartedNanos),
                            signalType,
                            command.getActor().getTenantId(),
                            command.getActor().getUserId(),
                            command.getSessionId(),
                            observedRun.get() == null ? null : observedRun.get().getSessionId(),
                            observedRun.get() == null ? null : observedRun.get().getRunId()
                    ))
                    .flatMapMany(startedRun -> executeRun(command, startedRun));
        });
    }

    /**
     * 将单次 Run 绑定到 Reactor 资源生命周期。
     *
     * <p>usingWhen 的三个异步清理分支分别对应 SUCCEEDED、FAILED、CANCELLED，
     * 因而客户端断开连接也不会遗留一个无法解释的活动 Run。</p>
     */
    private Flux<ChatEvent> executeRun(StartChatCommand command, StartedRun startedRun) {
        RunStreamState state = new RunStreamState(startedRun, command.getActor());

        Flux<ChatEvent> managedEvents = Flux.usingWhen(
                Mono.just(state),currentState -> Flux.defer(() -> {
                    AgentInvocation invocation = new AgentInvocation(
                            startedRun.getAgentId(),
                            startedRun.getSessionId(),
                            startedRun.getRunId(),
                            command.getActor(),
                            command.getContent(),
                            command.getRequestStartedNanos()
                    );

                    return streamRuntime(currentState, invocation);
                }),

                this::finishSucceeded,

                (currentState, error) -> finishFailed(currentState),

                this::finishCancelled
        );

        /*
         * usingWhen 会等待成功收尾完成后再发送完成信号。
         * 因此 run.completed 一定发生在 ChatStore.finish() 成功之后。
         */
        return managedEvents
                .concatWith(Mono.fromSupplier(() ->
                        ChatEvent.runCompleted(
                                startedRun.getSessionId(),
                                startedRun.getRunId(),
                                state.nextSequence()
                        )
                ))
                /*
                 * 只有 Agent 运行失败且 FAILED 状态已经成功落库，才转换为
                 * run.failed。收尾失败会继续作为错误向下游传播，避免客户端
                 * 误以为 Run 已经可靠结束。
                 */
                .onErrorResume(RuntimeExecutionException.class, error -> Mono.just(
                        ChatEvent.runFailed(
                                startedRun.getSessionId(),
                                startedRun.getRunId(),
                                state.nextSequence(),
                                RUNTIME_ERROR_CODE,
                                RUNTIME_ERROR_MESSAGE
                        )
                ));
    }

    /**
     * 消费 RuntimeEvent、保持原始顺序，并在自然完成前验证显式终止协议。
     *
     * <p>运行时异常在这里被替换为无 cause、无堆栈的内部异常，防止 API Key、
     * Authorization 或供应商响应沿 Reactor 链进入客户端。</p>
     */
    private Flux<ChatEvent> streamRuntime(RunStreamState state,AgentInvocation invocation) {
        return Flux.defer(() -> agentRuntime.stream(invocation))
                /*
                 * concatMap 保证事件顺序不变，并防止后面的事件越过前面的处理。
                 */
                .concatMap(runtimeEvent -> mapRuntimeEvent(state, runtimeEvent))
                /*
                 * AgentRuntime 必须显式发送 COMPLETED。
                 * 仅仅让 Flux 自然结束不能代表一次合法的 Agent 完成。
                 */
                .concatWith(Mono.defer(() -> validateRuntimeCompletion(state)))
                /*
                 * 原始异常可能包含 API Key、Authorization 或模型响应。
                 * 在进入收尾链路前将其替换为不携带 message、cause 和堆栈的
                 * 内部异常，同时日志只记录异常类型和 Run 标识。
                 */
                .onErrorMap(error -> runtimeExecutionFailed(state, error));
    }

    /** 根据当前状态机分派单个运行时事件。 */
    private Mono<ChatEvent> mapRuntimeEvent(RunStreamState state,RuntimeEvent runtimeEvent) {
        if (runtimeEvent == null) {
            return Mono.error(new IllegalStateException("runtimeEvent 不能为空"));
        }

        if (state.completed) {
            return Mono.error(new IllegalStateException("Runtime emitted an event after COMPLETED"));
        }

        return switch (runtimeEvent.getType()) {
            case STARTED ->
                    mapStartedEvent(state);

            case THINKING_DELTA ->
                    mapThinkingDeltaEvent(state, runtimeEvent);

            case TEXT_DELTA ->
                    mapTextDeltaEvent(state, runtimeEvent);

            case COMPLETED ->
                    mapCompletedEvent(state);
        };
    }

    /**
     * 转发 Thinking 增量，但不追加到 assistantContent，因此不会作为最终助手
     * 消息写库。
     */
    private Mono<ChatEvent> mapThinkingDeltaEvent(
            RunStreamState state,
            RuntimeEvent runtimeEvent
    ) {
        if (!state.started) {
            return Mono.error(
                    new IllegalStateException("Runtime emitted THINKING_DELTA before STARTED")
            );
        }

        String delta = runtimeEvent.getDelta();

        if (delta == null || delta.isEmpty()) {
            return Mono.error(
                    new IllegalStateException("Runtime emitted an empty THINKING_DELTA")
            );
        }

        /*
         * 思考内容只发送给前端，不写入最终 assistantContent。
         */
        return Mono.just(
                ChatEvent.messageThinkingDelta(
                        state.startedRun.getSessionId(),
                        state.startedRun.getRunId(),
                        state.nextSequence(),
                        delta
                )
        );
    }

    /** 接受唯一的 STARTED，并生成 Run 内第一条业务事件。 */
    private Mono<ChatEvent> mapStartedEvent(RunStreamState state) {
        if (state.started) {
            return Mono.error(new IllegalStateException("Runtime emitted STARTED more than once"));
        }

        state.started = true;

        return Mono.just(
                ChatEvent.runStarted(
                        state.startedRun.getSessionId(),
                        state.startedRun.getRunId(),
                        state.nextSequence()
                )
        );
    }

    /** 校验并累积最终正文增量，同时生成 SSE 业务事件。 */
    private Mono<ChatEvent> mapTextDeltaEvent(RunStreamState state, RuntimeEvent runtimeEvent) {
        if (!state.started) {
            return Mono.error(new IllegalStateException("Runtime emitted TEXT_DELTA before STARTED"));
        }

        String delta = runtimeEvent.getDelta();

        if (delta == null || delta.isEmpty()) {
            return Mono.error(new IllegalStateException("Runtime emitted an empty TEXT_DELTA"));
        }

        state.assistantContent.append(delta);

        return Mono.just(
                ChatEvent.messageDelta(
                        state.startedRun.getSessionId(),
                        state.startedRun.getRunId(),
                        state.nextSequence(),
                        delta
                )
        );
    }

    /**
     * 记录 Runtime 已正常完成，但暂不发布 run.completed；必须先等待成功收尾
     * 事务提交。
     */
    private Mono<ChatEvent> mapCompletedEvent(
            RunStreamState state
    ) {
        if (!state.started) {
            return Mono.error(new IllegalStateException("Runtime emitted COMPLETED before STARTED"));
        }

        state.completed = true;

        /*
         * 这里不立即生成 run.completed。
         * 必须先让 usingWhen 执行成功收尾。
         */
        return Mono.empty();
    }

    /** 在 Flux 自然结束时验证 Runtime 是否满足完整协议。 */
    private Mono<ChatEvent> validateRuntimeCompletion(RunStreamState state) {
        if (!state.started) {
            return Mono.error(new IllegalStateException("Runtime completed without STARTED"));
        }

        if (!state.completed) {
            return Mono.error(new IllegalStateException("Runtime completed without COMPLETED event"));
        }

        if (state.assistantContent.toString().isBlank()) {
            return Mono.error(new IllegalStateException("Runtime completed without assistant content"));
        }

        return Mono.empty();
    }

    /** 构造 SUCCEEDED 最终结果并保存完整助手正文。 */
    private Mono<Void> finishSucceeded(RunStreamState state) {
        return finishRun(
                state,
                RunFinalization.succeeded(
                        state.startedRun.getSessionId(),
                        state.startedRun.getRunId(),
                        state.actor,
                        state.assistantContent.toString()
                )
        );
    }

    /** 构造 FAILED 最终结果，只保存安全错误信息。 */
    private Mono<Void> finishFailed(RunStreamState state) {
        return finishRun(
                state,
                RunFinalization.failed(
                        state.startedRun.getSessionId(),
                        state.startedRun.getRunId(),
                        state.actor,
                        RUNTIME_ERROR_CODE,
                        RUNTIME_ERROR_MESSAGE
                )
        );
    }

    /** 构造 CANCELLED 最终结果，不保存尚未完成的助手正文。 */
    private Mono<Void> finishCancelled(RunStreamState state) {
        return finishRun(
                state,
                RunFinalization.cancelled(
                        state.startedRun.getSessionId(),
                        state.startedRun.getRunId(),
                        state.actor
                )
        );
    }

    /**
     * 延迟执行 ChatStore.finish，并把收尾故障与 Agent 运行故障分开处理。
     */
    private Mono<Void> finishRun(RunStreamState state, RunFinalization finalization) {
        return Mono.defer(() -> {
                    long finishStartedNanos = System.nanoTime();

                    return chatStore.finish(finalization)
                            .doFinally(signalType -> log.info(
                                    "chat-timing metric=chat.store.finish.duration durationMs={} "
                                            + "outcome={} status={} tenantId={} userId={} "
                                            + "sessionId={} runId={}",
                                    elapsedMillis(finishStartedNanos),
                                    signalType,
                                    finalization.getStatus(),
                                    finalization.getActor().getTenantId(),
                                    finalization.getActor().getUserId(),
                                    finalization.getSessionId(),
                                    finalization.getRunId()
                            ));
                })
                .onErrorMap(error -> finalizationFailed(state, finalization, error));
    }

    /** 记录最小安全诊断信息，并生成不会泄密的运行异常。 */
    private RuntimeExecutionException runtimeExecutionFailed(RunStreamState state, Throwable error) {
        log.error(
                "Agent runtime failed, sessionId={}, runId={}, errorType={}",
                state.startedRun.getSessionId(),
                state.startedRun.getRunId(),
                error.getClass().getName()
        );

        return new RuntimeExecutionException();
    }

    /**
     * 收尾失败表示最终状态不可靠，不能转换成 run.failed 或 run.completed。
     */
    private RunFinalizationException finalizationFailed(
            RunStreamState state,
            RunFinalization finalization,
            Throwable error
    ) {
        log.error(
                "Agent run finalization failed, status={}, sessionId={}, runId={}, errorType={}",
                finalization.getStatus(),
                state.startedRun.getSessionId(),
                state.startedRun.getRunId(),
                error.getClass().getName()
        );

        return new RunFinalizationException();
    }

    /**
     * 单次订阅私有的可变状态。
     *
     * <p>实例不会跨 Run 共享；concatMap 串行更新状态，sequence 为前端提供
     * Run 内稳定顺序。</p>
     */
    private static final class RunStreamState {

        private final StartedRun startedRun;

        private final ChatActor actor;

        private final StringBuilder assistantContent = new StringBuilder();

        private final AtomicLong sequence = new AtomicLong();

        private boolean started;

        private boolean completed;

        private RunStreamState(StartedRun startedRun, ChatActor actor) {
            this.startedRun = Objects.requireNonNull(startedRun,"startedRun must not be null");
            this.actor = Objects.requireNonNull(actor, "actor must not be null");
        }

        private long nextSequence() {
            return sequence.incrementAndGet();
        }
    }

    /** 已脱敏的 Agent 运行失败标记，仅在 FAILED 收尾成功后转换为业务事件。 */
    private static final class RuntimeExecutionException extends RuntimeException {

        private RuntimeExecutionException() {
            super(RUNTIME_ERROR_MESSAGE, null, false, false);
        }
    }

    /** ChatStore 最终收尾失败标记，必须作为真正的流错误向上传播。 */
    private static final class RunFinalizationException extends RuntimeException {

        private RunFinalizationException() {
            super("Agent run finalization failed", null, false, false);
        }
    }
}
