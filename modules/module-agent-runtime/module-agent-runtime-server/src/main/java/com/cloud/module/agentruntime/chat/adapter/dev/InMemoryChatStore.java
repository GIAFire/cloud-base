package com.cloud.module.agentruntime.chat.adapter.dev;

import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.application.model.RunFinalization;
import com.cloud.module.agentruntime.chat.application.model.StartedRun;
import com.cloud.module.agentruntime.chat.domain.AgentRunStatus;
import com.cloud.module.agentruntime.chat.domain.ChatActor;
import com.cloud.module.agentruntime.chat.domain.ChatMessage;
import com.cloud.module.agentruntime.chat.domain.ChatRole;
import com.cloud.module.agentruntime.chat.store.ChatStore;
import com.cloud.module.agentruntime.chat.store.ChatStoreException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Component
@ConditionalOnProperty(
        prefix = "agent.runtime.store",
        name = "type",
        havingValue = "memory",
        matchIfMissing = true
)
public final class InMemoryChatStore implements ChatStore {

    private final AtomicLong sessionSequence = new AtomicLong(1_000);

    private final AtomicLong runSequence = new AtomicLong(2_000);

    /**
     * 内存 Adapter 的全部状态都由同一把锁保护。
     *
     * 这是开发和测试实现，优先保证它与 MySQL Adapter 具有完全相同的
     * 原子性和状态约束，而不是追求跨会话并行吞吐。
     */
    private final Object stateMonitor = new Object();

    private final Map<Long, SessionState> sessions = new HashMap<>();

    private final Map<Long, RunState> runs = new HashMap<>();

    private final Map<RequestKey, RunState> requestRuns = new HashMap<>();

    @Override
    public Mono<StartedRun> begin(StartChatCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        return Mono.fromSupplier(() -> {
            synchronized (stateMonitor) {
                RequestKey requestKey = RequestKey.from(command);
                RunState duplicateRun = requestRuns.get(requestKey);

                if (duplicateRun != null) {
                    throw ChatStoreException.duplicateRequest(
                            duplicateRun.sessionId,
                            duplicateRun.runId
                    );
                }

                SessionState sessionState = resolveSession(command);

                if (sessionState.activeRunId != null) {
                    throw ChatStoreException.sessionBusy(
                            sessionState.sessionId,
                            sessionState.activeRunId
                    );
                }

                sessionState.messages.add(new ChatMessage(ChatRole.USER, command.getContent()));

                Long runId = runSequence.incrementAndGet();
                RunState runState = new RunState(
                        runId,
                        sessionState.sessionId,
                        command.getActor()
                );

                sessionState.activeRunId = runId;
                runs.put(runId, runState);
                requestRuns.put(requestKey, runState);

                return new StartedRun(sessionState.sessionId, runId);
            }
        });
    }

    @Override
    public Mono<Void> finish(RunFinalization finalization) {
        Objects.requireNonNull(finalization, "finalization must not be null");

        return Mono.fromRunnable(() -> {
            synchronized (stateMonitor) {
                SessionState sessionState = sessions.get(finalization.getSessionId());

                if (sessionState == null) {
                    throw ChatStoreException.sessionNotFound(finalization.getSessionId());
                }

                sessionState.requireOwner(finalization.getActor());

                RunState runState = runs.get(finalization.getRunId());

                if (runState == null) {
                    throw ChatStoreException.runNotFound(
                            finalization.getSessionId(),
                            finalization.getRunId()
                    );
                }

                runState.requireOwner(finalization);

                RunFinalization previousFinalization = runState.finalization;

                if (previousFinalization != null) {
                    requireSameFinalization(previousFinalization, finalization);
                    return;
                }

                if (finalization.getStatus() == AgentRunStatus.SUCCEEDED) {
                    sessionState.messages.add(
                            new ChatMessage(ChatRole.ASSISTANT, finalization.getAssistantContent())
                    );
                }

                runState.finalization = finalization;
                sessionState.activeRunId = null;
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
            throw ChatStoreException.finalizationConflict(
                    current.getSessionId(),
                    current.getRunId()
            );
        }
    }

    private SessionState resolveSession(StartChatCommand command) {
        if (command.getSessionId() == null) {
            return createSession(command.getActor());
        }

        SessionState sessionState = sessions.get(command.getSessionId());

        if (sessionState == null) {
            throw ChatStoreException.sessionNotFound(command.getSessionId());
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

        private Long activeRunId;

        private SessionState(
                Long sessionId,
                Long tenantId,
                Long userId
        ) {
            this.sessionId = sessionId;
            this.tenantId = tenantId;
            this.userId = userId;
        }

        private void requireOwner(ChatActor actor) {
            if (!Objects.equals(tenantId, actor.getTenantId())
                    || !Objects.equals(userId, actor.getUserId())) {
                throw ChatStoreException.sessionAccessDenied(sessionId);
            }
        }
    }

    private static final class RunState {

        private final Long runId;
        private final Long sessionId;
        private final Long tenantId;
        private final Long userId;

        private RunFinalization finalization;

        private RunState(
                Long runId,
                Long sessionId,
                ChatActor actor
        ) {
            this.runId = runId;
            this.sessionId = sessionId;
            this.tenantId = actor.getTenantId();
            this.userId = actor.getUserId();
        }

        private void requireOwner(RunFinalization current) {
            ChatActor actor = current.getActor();

            if (!Objects.equals(sessionId, current.getSessionId())
                    || !Objects.equals(tenantId, actor.getTenantId())
                    || !Objects.equals(userId, actor.getUserId())) {
                throw ChatStoreException.finalizationConflict(
                        current.getSessionId(),
                        current.getRunId()
                );
            }
        }
    }

    private static final class RequestKey {

        private final Long tenantId;
        private final Long userId;
        private final String requestId;

        private RequestKey(
                Long tenantId,
                Long userId,
                String requestId
        ) {
            this.tenantId = tenantId;
            this.userId = userId;
            this.requestId = requestId;
        }

        private static RequestKey from(StartChatCommand command) {
            ChatActor actor = command.getActor();

            return new RequestKey(
                    actor.getTenantId(),
                    actor.getUserId(),
                    command.getRequestId()
            );
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof RequestKey that)) {
                return false;
            }

            return Objects.equals(tenantId, that.tenantId)
                    && Objects.equals(userId, that.userId)
                    && Objects.equals(requestId, that.requestId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    tenantId,
                    userId,
                    requestId
            );
        }
    }
}
