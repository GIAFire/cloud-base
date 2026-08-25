package com.cloud.module.agentruntime.support;

import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.application.model.RunFinalization;
import com.cloud.module.agentruntime.chat.application.model.StartedRun;
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

    private final StartedRun startedRun;

    private final Throwable finishError;

    private final List<RunFinalization> finalizations =
            new CopyOnWriteArrayList<>();

    private final CountDownLatch finalizationLatch =
            new CountDownLatch(1);

    public RecordingChatStore(
            StartedRun startedRun
    ) {
        this(startedRun, null);
    }

    public RecordingChatStore(
            StartedRun startedRun,
            Throwable finishError
    ) {
        this.startedRun = Objects.requireNonNull(
                startedRun,
                "startedRun must not be null"
        );
        this.finishError = finishError;
    }

    @Override
    public Mono<StartedRun> begin(
            StartChatCommand command
    ) {
        return Mono.just(startedRun);
    }

    @Override
    public Mono<Void> finish(
            RunFinalization finalization
    ) {
        if (finishError != null) {
            return Mono.error(finishError);
        }

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
