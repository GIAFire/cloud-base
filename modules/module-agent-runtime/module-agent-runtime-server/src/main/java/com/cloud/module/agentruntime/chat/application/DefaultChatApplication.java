package com.cloud.module.agentruntime.chat.application;

import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.application.model.RunFinalization;
import com.cloud.module.agentruntime.chat.application.model.StartedRun;
import com.cloud.module.agentruntime.chat.domain.ChatEvent;
import com.cloud.module.agentruntime.chat.runtime.AgentInvocation;
import com.cloud.module.agentruntime.chat.runtime.AgentRuntime;
import com.cloud.module.agentruntime.chat.runtime.RuntimeEvent;
import com.cloud.module.agentruntime.chat.store.ChatStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public final class DefaultChatApplication implements ChatApplication {

    private static final String RUNTIME_ERROR_CODE = "AGENT_RUNTIME_ERROR";
    private static final String RUNTIME_ERROR_MESSAGE = "智能体运行失败，请稍后重试";

    private final AgentRuntime agentRuntime;
    private final ChatStore chatStore;

    @Override
    public Flux<ChatEvent> stream(StartChatCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        return Flux.defer(() -> chatStore.begin(command)
                .switchIfEmpty(Mono.error(new IllegalStateException("ChatStore.begin returned empty")))
                .flatMapMany(startedRun -> executeRun(command, startedRun)));
    }

    private Flux<ChatEvent> executeRun(StartChatCommand command, StartedRun startedRun) {
        RunStreamState state = new RunStreamState(startedRun);

        Flux<ChatEvent> managedEvents = Flux.usingWhen(
                Mono.just(state),currentState -> Flux.defer(() -> {
                    AgentInvocation invocation = new AgentInvocation(
                            startedRun.getSessionId(),
                            startedRun.getRunId(),
                            command.getActor(),
                            command.getContent()
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

    private Mono<ChatEvent> mapRuntimeEvent(RunStreamState state,RuntimeEvent runtimeEvent) {
        if (runtimeEvent == null) {
            return Mono.error(new IllegalStateException("runtimeEvent must not be null"));
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

    private Mono<ChatEvent> mapThinkingDeltaEvent(
            RunStreamState state,
            RuntimeEvent runtimeEvent
    ) {
        if (!state.started) {
            return Mono.error(
                    new IllegalStateException(
                            "Runtime emitted THINKING_DELTA before STARTED"
                    )
            );
        }

        String delta = runtimeEvent.getDelta();

        if (delta == null || delta.isEmpty()) {
            return Mono.error(
                    new IllegalStateException(
                            "Runtime emitted an empty THINKING_DELTA"
                    )
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

    private Mono<ChatEvent> mapTextDeltaEvent(
            RunStreamState state,
            RuntimeEvent runtimeEvent
    ) {
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

    private Mono<Void> finishSucceeded(RunStreamState state) {
        return finishRun(
                state,
                RunFinalization.succeeded(
                        state.startedRun.getSessionId(),
                        state.startedRun.getRunId(),
                        state.assistantContent.toString()
                )
        );
    }

    private Mono<Void> finishFailed(RunStreamState state) {
        return finishRun(
                state,
                RunFinalization.failed(
                        state.startedRun.getSessionId(),
                        state.startedRun.getRunId(),
                        RUNTIME_ERROR_CODE,
                        RUNTIME_ERROR_MESSAGE
                )
        );
    }

    private Mono<Void> finishCancelled(RunStreamState state) {
        return finishRun(
                state,
                RunFinalization.cancelled(
                        state.startedRun.getSessionId(),
                        state.startedRun.getRunId()
                )
        );
    }

    private Mono<Void> finishRun(
            RunStreamState state,
            RunFinalization finalization
    ) {
        return Mono.defer(() -> chatStore.finish(finalization))
                .onErrorMap(error -> finalizationFailed(state, finalization, error));
    }

    private RuntimeExecutionException runtimeExecutionFailed(
            RunStreamState state,
            Throwable error
    ) {
        log.error(
                "Agent runtime failed, sessionId={}, runId={}, errorType={}",
                state.startedRun.getSessionId(),
                state.startedRun.getRunId(),
                error.getClass().getName()
        );

        return new RuntimeExecutionException();
    }

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

    private static final class RunStreamState {

        private final StartedRun startedRun;
        private final StringBuilder assistantContent = new StringBuilder();
        private final AtomicLong sequence = new AtomicLong();

        private boolean started;
        private boolean completed;

        private RunStreamState(StartedRun startedRun) {
            this.startedRun = Objects.requireNonNull(startedRun, "startedRun must not be null");
        }

        private long nextSequence() {
            return sequence.incrementAndGet();
        }
    }

    private static final class RuntimeExecutionException extends RuntimeException {

        private RuntimeExecutionException() {
            super(RUNTIME_ERROR_MESSAGE, null, false, false);
        }
    }

    private static final class RunFinalizationException extends RuntimeException {

        private RunFinalizationException() {
            super("Agent run finalization failed", null, false, false);
        }
    }
}
