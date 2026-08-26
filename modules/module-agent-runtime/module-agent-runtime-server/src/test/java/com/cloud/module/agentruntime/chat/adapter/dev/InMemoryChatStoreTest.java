package com.cloud.module.agentruntime.chat.adapter.dev;

import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.application.model.RunFinalization;
import com.cloud.module.agentruntime.chat.application.model.StartedRun;
import com.cloud.module.agentruntime.chat.domain.ChatActor;
import com.cloud.module.agentruntime.chat.store.ChatStoreException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryChatStoreTest {

    @Test
    void rejects_duplicate_request_for_same_actor() {
        InMemoryChatStore store = new InMemoryChatStore();
        StartedRun startedRun = store.begin(command()).block();

        assertThat(startedRun).isNotNull();

        StepVerifier.create(store.begin(command()))
                .expectErrorSatisfies(error -> assertStoreError(
                        error,
                        ChatStoreException.Reason.DUPLICATE_REQUEST,
                        startedRun.getSessionId(),
                        startedRun.getRunId()
                ))
                .verify();
    }

    @Test
    void rejects_second_run_while_session_is_running() {
        InMemoryChatStore store = new InMemoryChatStore();
        StartedRun firstRun = store.begin(command()).block();

        assertThat(firstRun).isNotNull();

        StartChatCommand secondCommand = command(
                firstRun.getSessionId(),
                "request-002",
                command().getActor()
        );

        StepVerifier.create(store.begin(secondCommand))
                .expectErrorSatisfies(error -> assertStoreError(
                        error,
                        ChatStoreException.Reason.SESSION_BUSY,
                        firstRun.getSessionId(),
                        firstRun.getRunId()
                ))
                .verify();

        RunFinalization cancelled = RunFinalization.cancelled(
                firstRun.getSessionId(),
                firstRun.getRunId(),
                command().getActor()
        );

        StepVerifier.create(
                        store.finish(cancelled)
                                .then(store.begin(secondCommand))
                )
                .expectNextMatches(secondRun ->
                        secondRun.getSessionId().equals(firstRun.getSessionId())
                                && !secondRun.getRunId().equals(firstRun.getRunId())
                )
                .verifyComplete();
    }

    @Test
    void accepts_repeated_identical_finalization() {
        InMemoryChatStore store = new InMemoryChatStore();
        StartedRun startedRun = store.begin(command()).block();

        assertThat(startedRun).isNotNull();

        RunFinalization finalization = RunFinalization.succeeded(
                startedRun.getSessionId(),
                startedRun.getRunId(),
                command().getActor(),
                "你好"
        );

        StepVerifier.create(
                        store.finish(finalization)
                                .then(store.finish(finalization))
                )
                .verifyComplete();
    }

    @Test
    void rejects_conflicting_finalization() {
        InMemoryChatStore store = new InMemoryChatStore();
        StartedRun startedRun = store.begin(command()).block();

        assertThat(startedRun).isNotNull();

        RunFinalization succeeded = RunFinalization.succeeded(
                startedRun.getSessionId(),
                startedRun.getRunId(),
                command().getActor(),
                "你好"
        );
        RunFinalization failed = RunFinalization.failed(
                startedRun.getSessionId(),
                startedRun.getRunId(),
                command().getActor(),
                "AGENT_RUNTIME_ERROR",
                "智能体运行失败，请稍后重试"
        );

        StepVerifier.create(
                        store.finish(succeeded)
                                .then(store.finish(failed))
                )
                .expectErrorSatisfies(error -> assertStoreError(
                        error,
                        ChatStoreException.Reason.FINALIZATION_CONFLICT,
                        startedRun.getSessionId(),
                        startedRun.getRunId()
                ))
                .verify();
    }

    @Test
    void validates_actor_before_accepting_repeated_finalization() {
        InMemoryChatStore store = new InMemoryChatStore();
        StartedRun startedRun = store.begin(command()).block();

        assertThat(startedRun).isNotNull();

        RunFinalization ownerFinalization = RunFinalization.succeeded(
                startedRun.getSessionId(),
                startedRun.getRunId(),
                command().getActor(),
                "你好"
        );
        RunFinalization otherUserFinalization = RunFinalization.succeeded(
                startedRun.getSessionId(),
                startedRun.getRunId(),
                new ChatActor(1L, 3L),
                "你好"
        );

        StepVerifier.create(
                        store.finish(ownerFinalization)
                                .then(store.finish(otherUserFinalization))
                )
                .expectErrorSatisfies(error -> assertStoreError(
                        error,
                        ChatStoreException.Reason.SESSION_ACCESS_DENIED,
                        startedRun.getSessionId(),
                        null
                ))
                .verify();
    }

    private StartChatCommand command() {
        return command(
                null,
                "request-001",
                new ChatActor(1L, 2L)
        );
    }

    private StartChatCommand command(
            Long sessionId,
            String requestId,
            ChatActor actor
    ) {
        return new StartChatCommand(
                sessionId,
                requestId,
                "你好",
                actor
        );
    }

    private void assertStoreError(
            Throwable error,
            ChatStoreException.Reason reason,
            Long sessionId,
            Long runId
    ) {
        assertThat(error).isInstanceOf(ChatStoreException.class);

        ChatStoreException storeError = (ChatStoreException) error;

        assertThat(storeError.getReason()).isEqualTo(reason);
        assertThat(storeError.getSessionId()).isEqualTo(sessionId);
        assertThat(storeError.getRunId()).isEqualTo(runId);
    }
}
