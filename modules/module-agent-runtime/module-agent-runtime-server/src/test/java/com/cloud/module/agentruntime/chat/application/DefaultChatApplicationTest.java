package com.cloud.module.agentruntime.chat.application;

import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.application.model.PreparedRun;
import com.cloud.module.agentruntime.chat.application.model.RunFinalization;
import com.cloud.module.agentruntime.chat.domain.AgentRunStatus;
import com.cloud.module.agentruntime.chat.domain.ChatActor;
import com.cloud.module.agentruntime.chat.domain.ChatEventType;
import com.cloud.module.agentruntime.chat.domain.ChatMessage;
import com.cloud.module.agentruntime.chat.domain.ChatRole;
import com.cloud.module.agentruntime.chat.runtime.RuntimeEvent;
import com.cloud.module.agentruntime.support.RecordingChatStore;
import com.cloud.module.agentruntime.support.ScriptedAgentRuntime;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultChatApplicationTest {

    private static final Long SESSION_ID = 101L;
    private static final Long RUN_ID = 202L;

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
                        preparedRun()
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
                        preparedRun()
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
    }

    @Test
    void marks_run_cancelled_when_subscriber_cancels() {
        ScriptedAgentRuntime runtime =
                new ScriptedAgentRuntime(
                        Flux.concat(
                                Flux.just(
                                        RuntimeEvent.started()
                                ),
                                Flux.<RuntimeEvent>never()
                        )
                );

        RecordingChatStore store =
                new RecordingChatStore(
                        preparedRun()
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
    }

    private StartChatCommand command() {
        return new StartChatCommand(
                SESSION_ID,
                "request-001",
                "你好",
                new ChatActor(1L, 2L)
        );
    }

    private PreparedRun preparedRun() {
        return new PreparedRun(
                SESSION_ID,
                RUN_ID,
                List.of(
                        new ChatMessage(
                                ChatRole.USER,
                                "你好"
                        )
                )
        );
    }
}