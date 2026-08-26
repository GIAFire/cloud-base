package com.cloud.module.agent.chat.application;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.cloud.module.agent.chat.application.command.StartChatCommand;
import com.cloud.module.agent.chat.application.model.RunFinalization;
import com.cloud.module.agent.chat.application.model.StartedRun;
import com.cloud.module.agent.chat.domain.AgentRunStatus;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.domain.ChatEventType;
import com.cloud.module.agent.chat.runtime.AgentInvocation;
import com.cloud.module.agent.chat.runtime.AgentRuntime;
import com.cloud.module.agent.chat.runtime.RuntimeEvent;
import com.cloud.module.agent.chat.store.ChatStore;
import com.cloud.module.agent.support.RecordingChatStore;
import com.cloud.module.agent.support.ScriptedAgentRuntime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultChatApplicationTest {

    private static final Long SESSION_ID = 101L;
    private static final Long RUN_ID = 202L;

    private Logger applicationLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUpLogCapture() {
        applicationLogger = (Logger) LoggerFactory.getLogger(DefaultChatApplication.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        applicationLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDownLogCapture() {
        applicationLogger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void streams_started_deltas_and_completed_in_order() {
        ScriptedAgentRuntime runtime = new ScriptedAgentRuntime(
                        Flux.just(
                                RuntimeEvent.started(),
                                RuntimeEvent.textDelta("你"),
                                RuntimeEvent.textDelta("好"),
                                RuntimeEvent.completed()
                        ));

        RecordingChatStore store =
                new RecordingChatStore(
                        startedRun()
                );

        ChatApplication application =
                new DefaultChatApplication(
                        runtime,
                        store
                );

        StepVerifier.create(
                        application.stream(command())
                )
                .assertNext(event -> {
                    assertThat(event.getType())
                            .isEqualTo(
                                    ChatEventType.RUN_STARTED
                            );
                    assertThat(event.getSequence())
                            .isEqualTo(1);
                })
                .assertNext(event -> {
                    assertThat(event.getType())
                            .isEqualTo(
                                    ChatEventType.MESSAGE_DELTA
                            );
                    assertThat(event.getSequence())
                            .isEqualTo(2);
                    assertThat(event.getDelta())
                            .isEqualTo("你");
                })
                .assertNext(event -> {
                    assertThat(event.getType())
                            .isEqualTo(
                                    ChatEventType.MESSAGE_DELTA
                            );
                    assertThat(event.getSequence())
                            .isEqualTo(3);
                    assertThat(event.getDelta())
                            .isEqualTo("好");
                })
                .assertNext(event -> {
                    assertThat(event.getType())
                            .isEqualTo(
                                    ChatEventType.RUN_COMPLETED
                            );
                    assertThat(event.getSequence())
                            .isEqualTo(4);
                })
                .verifyComplete();

        RunFinalization finalization =
                store.getSingleFinalization();

        assertThat(finalization.getStatus())
                .isEqualTo(
                        AgentRunStatus.SUCCEEDED
                );

        assertThat(
                finalization.getAssistantContent()
        ).isEqualTo("你好");
    }

    @Test
    void passes_only_current_command_content_to_runtime() {
        AtomicReference<AgentInvocation> capturedInvocation = new AtomicReference<>();

        AgentRuntime runtime = invocation -> {
            capturedInvocation.set(invocation);
            return Flux.just(
                    RuntimeEvent.started(),
                    RuntimeEvent.textDelta("已收到"),
                    RuntimeEvent.completed()
            );
        };

        RecordingChatStore store = new RecordingChatStore(startedRun());
        ChatApplication application = new DefaultChatApplication(runtime, store);

        StepVerifier.create(application.stream(command()))
                .expectNextCount(3)
                .verifyComplete();

        AgentInvocation invocation = capturedInvocation.get();
        assertThat(invocation).isNotNull();
        assertThat(invocation.getSessionId()).isEqualTo(SESSION_ID);
        assertThat(invocation.getRunId()).isEqualTo(RUN_ID);
        assertThat(invocation.getActor().getTenantId()).isEqualTo(1L);
        assertThat(invocation.getActor().getUserId()).isEqualTo(2L);
        assertThat(invocation.getContent()).isEqualTo("你好");
    }

    @Test
    void waits_for_finish_before_emitting_run_completed() {
        ScriptedAgentRuntime runtime = new ScriptedAgentRuntime(
                Flux.just(
                        RuntimeEvent.started(),
                        RuntimeEvent.textDelta("你好"),
                        RuntimeEvent.completed()
                )
        );

        AtomicBoolean finishCalled = new AtomicBoolean();
        AtomicReference<RunFinalization> recordedFinalization = new AtomicReference<>();
        Sinks.Empty<Void> finishGate = Sinks.empty();

        ChatStore store = new ChatStore() {

            @Override
            public Mono<StartedRun> begin(StartChatCommand command) {
                return Mono.just(startedRun());
            }

            @Override
            public Mono<Void> finish(RunFinalization finalization) {
                finishCalled.set(true);
                recordedFinalization.set(finalization);
                return finishGate.asMono();
            }
        };

        ChatApplication application = new DefaultChatApplication(runtime, store);

        StepVerifier.create(application.stream(command()))
                .expectNextMatches(event -> event.getType() == ChatEventType.RUN_STARTED)
                .expectNextMatches(event -> event.getType() == ChatEventType.MESSAGE_DELTA)
                /*
                 * finishGate 尚未完成；如果 run.completed 被提前发送，
                 * expectNoEvent 会立即失败。
                 */
                .expectNoEvent(Duration.ofMillis(100))
                .then(() -> {
                    assertThat(finishCalled).isTrue();
                    assertThat(recordedFinalization.get()).isNotNull();
                    assertThat(recordedFinalization.get().getStatus())
                            .isEqualTo(AgentRunStatus.SUCCEEDED);
                    finishGate.tryEmitEmpty();
                })
                .expectNextMatches(event -> event.getType() == ChatEventType.RUN_COMPLETED)
                .verifyComplete();
    }

    @Test
    void converts_runtime_error_to_failed_event() {
        ScriptedAgentRuntime runtime =
                new ScriptedAgentRuntime(
                        Flux.concat(
                                Flux.just(
                                        RuntimeEvent.started()
                                ),
                                Flux.error(
                                        new RuntimeException(
                                                "api-key=secret-value"
                                        )
                                )
                        )
                );

        RecordingChatStore store =
                new RecordingChatStore(
                        startedRun()
                );

        ChatApplication application =
                new DefaultChatApplication(
                        runtime,
                        store
                );

        StepVerifier.create(
                        application.stream(command())
                )
                .assertNext(event -> {
                    assertThat(event.getType())
                            .isEqualTo(
                                    ChatEventType.RUN_STARTED
                            );
                    assertThat(event.getSequence())
                            .isEqualTo(1);
                })
                .assertNext(event -> {
                    assertThat(event.getType())
                            .isEqualTo(
                                    ChatEventType.RUN_FAILED
                            );
                    assertThat(event.getSequence())
                            .isEqualTo(2);
                    assertThat(event.getErrorCode())
                            .isEqualTo(
                                    "AGENT_RUNTIME_ERROR"
                            );
                    assertThat(event.getErrorMessage())
                            .doesNotContain("secret-value");
                })
                .verifyComplete();

        RunFinalization finalization =
                store.getSingleFinalization();

        assertThat(finalization.getStatus())
                .isEqualTo(
                        AgentRunStatus.FAILED
                );

        assertThat(finalization.getErrorMessage())
                .doesNotContain("secret-value");

        assertLogsDoNotContain("secret-value");
    }

    @Test
    void propagates_safe_error_when_success_finalization_fails() {
        ScriptedAgentRuntime runtime = new ScriptedAgentRuntime(
                Flux.just(
                        RuntimeEvent.started(),
                        RuntimeEvent.textDelta("你好"),
                        RuntimeEvent.completed()
                )
        );

        RecordingChatStore store = new RecordingChatStore(
                startedRun(),
                new RuntimeException("api-key=secret-finalization-value")
        );

        ChatApplication application = new DefaultChatApplication(runtime, store);

        StepVerifier.create(application.stream(command()))
                .assertNext(event -> assertThat(event.getType())
                        .isEqualTo(ChatEventType.RUN_STARTED))
                .assertNext(event -> assertThat(event.getType())
                        .isEqualTo(ChatEventType.MESSAGE_DELTA))
                .expectErrorSatisfies(error -> {
                    assertThrowableDoesNotContain(error, "secret-finalization-value");
                    assertThat(rootCauseOf(error).getMessage()).contains("finalization failed");
                })
                .verify();

        assertThat(store.getFinalizations()).isEmpty();
        assertLogsDoNotContain("secret-finalization-value");
    }

    @Test
    void marks_run_cancelled_when_subscriber_cancels() {
        AtomicBoolean upstreamCancelled = new AtomicBoolean();

        ScriptedAgentRuntime runtime =
                new ScriptedAgentRuntime(
                        Flux.concat(
                                Flux.just(
                                        RuntimeEvent.started()
                                ),
                                Flux.<RuntimeEvent>never()
                        ).doOnCancel(() -> upstreamCancelled.set(true))
                );

        RecordingChatStore store =
                new RecordingChatStore(
                        startedRun()
                );

        ChatApplication application =
                new DefaultChatApplication(
                        runtime,
                        store
                );

        StepVerifier.create(
                        application.stream(command())
                )
                .assertNext(event ->
                        assertThat(event.getType())
                                .isEqualTo(
                                        ChatEventType.RUN_STARTED
                                )
                )
                .thenCancel()
                .verify();

        assertThat(
                store.awaitFinalization(
                        Duration.ofSeconds(1)
                )
        ).isTrue();

        RunFinalization finalization =
                store.getSingleFinalization();

        assertThat(finalization.getStatus())
                .isEqualTo(
                        AgentRunStatus.CANCELLED
                );

        assertThat(upstreamCancelled).isTrue();
    }

    private StartChatCommand command() {
        return new StartChatCommand(
                SESSION_ID,
                "request-001",
                "你好",
                new ChatActor(1L, 2L)
        );
    }

    private StartedRun startedRun() {
        return new StartedRun(SESSION_ID, RUN_ID);
    }

    private void assertLogsDoNotContain(String sensitiveValue) {
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .allSatisfy(message -> assertThat(message).doesNotContain(sensitiveValue));

        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getThrowableProxy)
                .allMatch(throwableProxy -> throwableProxy == null);
    }

    private void assertThrowableDoesNotContain(Throwable error, String sensitiveValue) {
        Throwable current = error;

        while (current != null) {
            assertThat(current.getMessage()).doesNotContain(sensitiveValue);
            current = current.getCause();
        }
    }

    private Throwable rootCauseOf(Throwable error) {
        Throwable current = error;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current;
    }
}
