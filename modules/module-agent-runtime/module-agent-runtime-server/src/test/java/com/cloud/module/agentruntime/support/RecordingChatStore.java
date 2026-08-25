package com.cloud.module.agentruntime.support;

import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.application.model.PreparedRun;
import com.cloud.module.agentruntime.chat.application.model.RunFinalization;
import com.cloud.module.agentruntime.chat.store.ChatStore;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class RecordingChatStore
        implements ChatStore {

    private final PreparedRun preparedRun;

    private final List<RunFinalization> finalizations =
            new CopyOnWriteArrayList<>();

    private final CountDownLatch finalizationLatch =
            new CountDownLatch(1);

    public RecordingChatStore(
            PreparedRun preparedRun
    ) {
        this.preparedRun = Objects.requireNonNull(
                preparedRun,
                "preparedRun must not be null"
        );
    }

    @Override
    public Mono<PreparedRun> begin(
            StartChatCommand command
    ) {
        return Mono.just(preparedRun);
    }

    @Override
    public Mono<Void> finish(
            RunFinalization finalization
    ) {
        return Mono.fromRunnable(() -> {
            finalizations.add(finalization);
            finalizationLatch.countDown();
        });
    }

    public List<RunFinalization> getFinalizations() {
        return List.copyOf(finalizations);
    }

    public RunFinalization getSingleFinalization() {
        if (finalizations.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one finalization, actual: "
                            + finalizations.size()
            );
        }

        return finalizations.get(0);
    }

    public boolean awaitFinalization(
            Duration timeout
    ) {
        try {
            return finalizationLatch.await(
                    timeout.toMillis(),
                    TimeUnit.MILLISECONDS
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while waiting for finalization",
                    exception
            );
        }
    }
}