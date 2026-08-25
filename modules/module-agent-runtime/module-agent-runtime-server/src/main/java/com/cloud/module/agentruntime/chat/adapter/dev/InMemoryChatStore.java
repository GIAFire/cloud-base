package com.cloud.module.agentruntime.chat.adapter.dev;

import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.application.model.RunFinalization;
import com.cloud.module.agentruntime.chat.application.model.StartedRun;
import com.cloud.module.agentruntime.chat.domain.AgentRunStatus;
import com.cloud.module.agentruntime.chat.domain.ChatActor;
import com.cloud.module.agentruntime.chat.domain.ChatMessage;
import com.cloud.module.agentruntime.chat.domain.ChatRole;
import com.cloud.module.agentruntime.chat.store.ChatStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Profile("dev")
public final class InMemoryChatStore implements ChatStore {

    private final AtomicLong sessionSequence = new AtomicLong(1_000);

    private final AtomicLong runSequence = new AtomicLong(2_000);

    private final ConcurrentMap<Long, SessionState> sessions = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, Long> runSessions = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, RunFinalization> finalizations = new ConcurrentHashMap<>();

    @Override
    public Mono<StartedRun> begin(StartChatCommand command) {
        return Mono.fromSupplier(() -> {
            SessionState sessionState = resolveSession(command);

            synchronized (sessionState) {
                sessionState.messages.add(new ChatMessage(ChatRole.USER, command.getContent()));

                Long runId = runSequence.incrementAndGet();

                runSessions.put(runId, sessionState.sessionId);

                return new StartedRun(sessionState.sessionId, runId);
            }
        });
    }

    @Override
    public Mono<Void> finish(RunFinalization finalization) {
        return Mono.fromRunnable(() -> {
            RunFinalization previousFinalization =
                    finalizations.get(finalization.getRunId());

            if (previousFinalization != null) {
                requireSameFinalization(previousFinalization, finalization);
                return;
            }

            Long expectedSessionId = runSessions.get(finalization.getRunId());

            if (expectedSessionId == null) {
                throw new IllegalStateException("Run does not exist: " + finalization.getRunId());
            }

            if (!Objects.equals(expectedSessionId,finalization.getSessionId())) {
                throw new IllegalStateException("Run does not belong to session");
            }

            SessionState sessionState =
                    sessions.get(finalization.getSessionId());

            if (sessionState == null) {
                throw new IllegalStateException("Session does not exist: " + finalization.getSessionId()
                );
            }

            synchronized (sessionState) {
                previousFinalization = finalizations.get(finalization.getRunId());

                if (previousFinalization != null) {
                    requireSameFinalization(previousFinalization, finalization);
                    return;
                }

                if (finalization.getStatus() == AgentRunStatus.SUCCEEDED) {
                    sessionState.messages.add(
                            new ChatMessage(ChatRole.ASSISTANT, finalization.getAssistantContent())
                    );
                }

                finalizations.put(finalization.getRunId(), finalization);
                runSessions.remove(finalization.getRunId());
            }
        });
    }

    private void requireSameFinalization(
            RunFinalization previous,
            RunFinalization current
    ) {
        if (previous.getStatus() != current.getStatus()
                || !Objects.equals(previous.getSessionId(), current.getSessionId())
                || !Objects.equals(previous.getRunId(), current.getRunId())
                || !Objects.equals(previous.getAssistantContent(), current.getAssistantContent())
                || !Objects.equals(previous.getErrorCode(), current.getErrorCode())
                || !Objects.equals(previous.getErrorMessage(), current.getErrorMessage())) {
            throw new IllegalStateException(
                    "Run has already been finalized with a different result: "
                            + current.getRunId()
            );
        }
    }

    private SessionState resolveSession(StartChatCommand command) {
        if (command.getSessionId() == null) {
            return createSession(command.getActor());
        }

        SessionState sessionState = sessions.get(command.getSessionId());

        if (sessionState == null) {
            throw new IllegalArgumentException("Session does not exist: " + command.getSessionId());
        }

        sessionState.requireOwner(command.getActor());

        return sessionState;
    }

    private SessionState createSession(ChatActor actor) {
        Long sessionId = sessionSequence.incrementAndGet();

        SessionState sessionState = new SessionState(
                sessionId,
                actor.getTenantId(),
                actor.getUserId()
        );

        sessions.put(
                sessionId,
                sessionState
        );

        return sessionState;
    }

    private static final class SessionState {

        private final Long sessionId;
        private final Long tenantId;
        private final Long userId;
        private final List<ChatMessage> messages = new ArrayList<>();

        private SessionState(
                Long sessionId,
                Long tenantId,
                Long userId ) {
            this.sessionId = sessionId;
            this.tenantId = tenantId;
            this.userId = userId;
        }

        private void requireOwner(ChatActor actor) {
            if (!Objects.equals(tenantId,actor.getTenantId())) {
                throw new IllegalArgumentException("Session does not belong to tenant");
            }

            if (!Objects.equals(userId,actor.getUserId())) {
                throw new IllegalArgumentException("Session does not belong to user");
            }
        }
    }
}
