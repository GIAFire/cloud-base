package com.cloud.module.agentruntime.chat.adapter.dev;

import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.application.model.RunFinalization;
import com.cloud.module.agentruntime.chat.application.model.StartedRun;
import com.cloud.module.agentruntime.chat.domain.ChatActor;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryChatStoreTest {

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
                .expectErrorMatches(error ->
                        error instanceof IllegalStateException
                                && error.getMessage().contains("different result")
                )
                .verify();
    }

    private StartChatCommand command() {
        return new StartChatCommand(
                null,
                "request-001",
                "你好",
                new ChatActor(1L, 2L)
        );
    }
}
