package com.cloud.module.agent.chat.adapter.persistence;

import com.cloud.framework.mybatis.core.dataobject.BaseDO;
import com.cloud.module.agent.chat.application.command.StartChatCommand;
import com.cloud.module.agent.chat.application.model.RunFinalization;
import com.cloud.module.agent.chat.application.model.StartedRun;
import com.cloud.module.agent.chat.domain.AgentMessageType;
import com.cloud.module.agent.chat.domain.AgentRunStatus;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.domain.ChatRole;
import com.cloud.module.agent.chat.store.ChatStoreException;
import com.cloud.module.agent.dal.dataobject.chat.AgentMessageDO;
import com.cloud.module.agent.dal.dataobject.chat.AgentRunDO;
import com.cloud.module.agent.dal.dataobject.chat.AgentSessionDO;
import com.cloud.module.agent.dal.mysql.chat.AgentMessageMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentRunMapper;
import com.cloud.module.agent.dal.mysql.chat.AgentSessionMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class ChatStoreTransactions {

    private static final int TITLE_MAX_LENGTH = 100;

    private final AgentSessionMapper sessionMapper;

    private final AgentRunMapper runMapper;

    private final AgentMessageMapper messageMapper;

    private final Long agentId;

    public ChatStoreTransactions(
            AgentSessionMapper sessionMapper,
            AgentRunMapper runMapper,
            AgentMessageMapper messageMapper,
            Long agentId
    ) {
        this.sessionMapper = Objects.requireNonNull(
                sessionMapper,
                "sessionMapper must not be null"
        );
        this.runMapper = Objects.requireNonNull(
                runMapper,
                "runMapper must not be null"
        );
        this.messageMapper = Objects.requireNonNull(
                messageMapper,
                "messageMapper must not be null"
        );
        this.agentId = requirePositive(agentId, "agentId");
    }

    @Transactional(rollbackFor = Exception.class)
    public StartedRun begin(
            StartChatCommand command
    ) {
        Objects.requireNonNull(
                command,
                "command must not be null"
        );

        ChatActor actor = command.getActor();

        AgentRunDO duplicateRun =
                runMapper.selectByUserAndRequestId(
                        actor.getUserId(),
                        command.getRequestId()
                );

        if (duplicateRun != null) {
            throw ChatStoreException.duplicateRequest(
                    duplicateRun.getSessionId(),
                    duplicateRun.getId()
            );
        }

        LocalDateTime now = LocalDateTime.now();

        AgentSessionDO session = resolveSession(
                command,
                actor,
                now
        );

        rejectRunningSession(session);

        AgentRunDO run = createRun(
                command,
                session,
                actor,
                now
        );

        long userMessageSeq = nextSequence(session);

        insertMessage(
                session.getId(),
                run.getId(),
                userMessageSeq,
                ChatRole.USER,
                AgentMessageType.USER_TEXT,
                command.getContent(),
                actor,
                now
        );

        updateSessionMessagePosition(
                session,
                userMessageSeq,
                actor,
                now
        );

        return new StartedRun(
                session.getId(),
                run.getId()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void finish(
            RunFinalization finalization
    ) {
        Objects.requireNonNull(
                finalization,
                "finalization must not be null"
        );

        ChatActor actor = finalization.getActor();

        /*
         * begin 和 finish 始终按照 Session -> Run 的顺序加锁，
         * 避免并发 begin/finish 形成死锁。
         */
        AgentSessionDO session =
                sessionMapper.selectByIdForUpdate(
                        finalization.getSessionId()
                );

        if (session == null) {
            throw ChatStoreException.sessionNotFound(
                    finalization.getSessionId()
            );
        }

        requireSessionOwner(session, actor);

        AgentRunDO run =
                runMapper.selectByIdForUpdate(
                        finalization.getRunId()
                );

        if (run == null) {
            throw ChatStoreException.runNotFound(
                    finalization.getSessionId(),
                    finalization.getRunId()
            );
        }

        requireRunOwner(run, finalization);

        AgentRunStatus persistedStatus =
                parseRunStatus(run);

        if (persistedStatus != AgentRunStatus.RUNNING) {
            requireSameFinalization(
                    run,
                    persistedStatus,
                    finalization
            );
            return;
        }

        LocalDateTime endedAt = LocalDateTime.now();

        if (finalization.getStatus()
                == AgentRunStatus.SUCCEEDED) {
            long assistantMessageSeq =
                    nextSequence(session);

            insertMessage(
                    session.getId(),
                    run.getId(),
                    assistantMessageSeq,
                    ChatRole.ASSISTANT,
                    AgentMessageType.ASSISTANT_TEXT,
                    finalization.getAssistantContent(),
                    actor,
                    endedAt
            );

            updateSessionMessagePosition(
                    session,
                    assistantMessageSeq,
                    actor,
                    endedAt
            );
        }

        updateRunFinalization(
                run,
                finalization,
                actor,
                endedAt
        );
    }

    private AgentSessionDO resolveSession(
            StartChatCommand command,
            ChatActor actor,
            LocalDateTime now
    ) {
        if (command.getSessionId() == null) {
            return createSession(
                    command,
                    actor,
                    now
            );
        }

        AgentSessionDO session =
                sessionMapper.selectByIdForUpdate(
                        command.getSessionId()
                );

        if (session == null) {
            throw ChatStoreException.sessionNotFound(
                    command.getSessionId()
            );
        }

        requireSessionOwner(session, actor);

        return session;
    }

    private AgentSessionDO createSession(
            StartChatCommand command,
            ChatActor actor,
            LocalDateTime now
    ) {
        AgentSessionDO session = new AgentSessionDO();

        session.setAgentId(agentId);
        session.setUserId(actor.getUserId());
        session.setTitle(createTitle(command.getContent()));
        session.setLastMessageSeq(0L);
        session.setLastMessageAt(null);

        initializeInsertFields(
                session,
                actor,
                now
        );

        int inserted = sessionMapper.insert(session);

        requireAffected(
                inserted,
                "Failed to create chat session",
                null,
                null
        );

        if (session.getId() == null) {
            throw ChatStoreException.persistenceIntegrity(
                    "Chat session ID was not assigned",
                    null,
                    null
            );
        }

        return session;
    }

    private void rejectRunningSession(
            AgentSessionDO session
    ) {
        AgentRunDO runningRun =
                runMapper.selectRunningBySessionIdForUpdate(
                        session.getId()
                );

        if (runningRun != null) {
            throw ChatStoreException.sessionBusy(
                    session.getId(),
                    runningRun.getId()
            );
        }
    }

    private AgentRunDO createRun(
            StartChatCommand command,
            AgentSessionDO session,
            ChatActor actor,
            LocalDateTime now
    ) {
        AgentRunDO run = new AgentRunDO();

        run.setRequestId(command.getRequestId());
        run.setSessionId(session.getId());
        run.setUserId(actor.getUserId());
        run.setStatus(AgentRunStatus.RUNNING.name());
        run.setStartedAt(now);

        initializeInsertFields(
                run,
                actor,
                now
        );

        try {
            int inserted = runMapper.insert(run);

            requireAffected(
                    inserted,
                    "Failed to create agent run",
                    session.getId(),
                    null
            );
        } catch (DuplicateKeyException exception) {
            throw ChatStoreException.duplicateRequest(
                    session.getId(),
                    null
            );
        }

        if (run.getId() == null) {
            throw ChatStoreException.persistenceIntegrity(
                    "Agent run ID was not assigned",
                    session.getId(),
                    null
            );
        }

        return run;
    }

    private void insertMessage(
            Long sessionId,
            Long runId,
            long sequence,
            ChatRole role,
            AgentMessageType messageType,
            String content,
            ChatActor actor,
            LocalDateTime now
    ) {
        AgentMessageDO message = new AgentMessageDO();

        message.setSessionId(sessionId);
        message.setRunId(runId);
        message.setSeq(sequence);
        message.setRole(role.name());
        message.setMessageType(messageType.name());
        message.setTextContent(content);

        initializeInsertFields(
                message,
                actor,
                now
        );

        int inserted = messageMapper.insert(message);

        requireAffected(
                inserted,
                "Failed to persist chat message",
                sessionId,
                runId
        );
    }

    private void updateSessionMessagePosition(
            AgentSessionDO session,
            long sequence,
            ChatActor actor,
            LocalDateTime now
    ) {
        AgentSessionDO update = new AgentSessionDO();

        update.setId(session.getId());
        update.setLastMessageSeq(sequence);
        update.setLastMessageAt(now);
        initializeUpdateFields(update, actor, now);

        int affected = sessionMapper.updateById(update);

        requireAffected(
                affected,
                "Failed to update chat session position",
                session.getId(),
                null
        );

        session.setLastMessageSeq(sequence);
        session.setLastMessageAt(now);
    }

    private void updateRunFinalization(
            AgentRunDO run,
            RunFinalization finalization,
            ChatActor actor,
            LocalDateTime endedAt
    ) {
        AgentRunDO update = new AgentRunDO();

        update.setId(run.getId());
        update.setStatus(finalization.getStatus().name());
        update.setErrorCode(finalization.getErrorCode());
        update.setErrorMessage(finalization.getErrorMessage());
        update.setEndedAt(endedAt);
        update.setDurationMs(
                calculateDuration(
                        run.getStartedAt(),
                        endedAt
                )
        );

        initializeUpdateFields(update, actor, endedAt);

        int affected = runMapper.updateById(update);

        requireAffected(
                affected,
                "Failed to finalize agent run",
                run.getSessionId(),
                run.getId()
        );
    }

    private void requireSameFinalization(
            AgentRunDO run,
            AgentRunStatus persistedStatus,
            RunFinalization finalization
    ) {
        if (persistedStatus != finalization.getStatus()) {
            throw ChatStoreException.finalizationConflict(
                    run.getSessionId(),
                    run.getId()
            );
        }

        switch (persistedStatus) {
            case SUCCEEDED -> {
                AgentMessageDO assistantMessage =
                        messageMapper.selectAssistantTextByRunId(
                                run.getId()
                        );

                if (assistantMessage == null
                        || !Objects.equals(
                        assistantMessage.getTextContent(),
                        finalization.getAssistantContent()
                )) {
                    throw ChatStoreException.finalizationConflict(
                            run.getSessionId(),
                            run.getId()
                    );
                }
            }

            case FAILED -> {
                if (!Objects.equals(
                        run.getErrorCode(),
                        finalization.getErrorCode()
                ) || !Objects.equals(
                        run.getErrorMessage(),
                        finalization.getErrorMessage()
                )) {
                    throw ChatStoreException.finalizationConflict(
                            run.getSessionId(),
                            run.getId()
                    );
                }
            }

            case CANCELLED -> {
                // 相同 CANCELLED 结果可以幂等返回。
            }

            case RUNNING -> throw
                    ChatStoreException.persistenceIntegrity(
                            "Unexpected RUNNING finalization state",
                            run.getSessionId(),
                            run.getId()
                    );
        }
    }

    private void requireSessionOwner(
            AgentSessionDO session,
            ChatActor actor
    ) {
        if (!Objects.equals(
                session.getUserId(),
                actor.getUserId()
        )) {
            throw ChatStoreException.sessionAccessDenied(
                    session.getId()
            );
        }

        if (!Objects.equals(
                session.getTenantId(),
                actor.getTenantId()
        )) {
            throw ChatStoreException.sessionAccessDenied(
                    session.getId()
            );
        }

        if (!Objects.equals(
                session.getAgentId(),
                agentId
        )) {
            throw ChatStoreException.sessionAccessDenied(
                    session.getId()
            );
        }
    }

    private void requireRunOwner(
            AgentRunDO run,
            RunFinalization finalization
    ) {
        ChatActor actor = finalization.getActor();

        if (!Objects.equals(
                run.getSessionId(),
                finalization.getSessionId()
        ) || !Objects.equals(
                run.getUserId(),
                actor.getUserId()
        ) || !Objects.equals(
                run.getTenantId(),
                actor.getTenantId()
        )) {
            throw ChatStoreException.finalizationConflict(
                    finalization.getSessionId(),
                    finalization.getRunId()
            );
        }
    }

    private AgentRunStatus parseRunStatus(
            AgentRunDO run
    ) {
        try {
            return AgentRunStatus.valueOf(
                    run.getStatus()
            );
        } catch (RuntimeException exception) {
            throw ChatStoreException.persistenceIntegrity(
                    "Unknown agent run status: "
                            + run.getStatus(),
                    run.getSessionId(),
                    run.getId()
            );
        }
    }

    private long nextSequence(
            AgentSessionDO session
    ) {
        Long current = session.getLastMessageSeq();

        return current == null
                ? 1L
                : Math.addExact(current, 1L);
    }

    private long calculateDuration(
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        if (startedAt == null) {
            return 0L;
        }

        return Math.max(
                0L,
                Duration.between(
                        startedAt,
                        endedAt
                ).toMillis()
        );
    }

    private String createTitle(
            String content
    ) {
        String title = content.strip();

        if (title.length() <= TITLE_MAX_LENGTH) {
            return title;
        }

        return title.substring(
                0,
                TITLE_MAX_LENGTH
        );
    }

    private void initializeInsertFields(
            BaseDO dataObject,
            ChatActor actor,
            LocalDateTime now
    ) {
        dataObject.setCreator(
                String.valueOf(actor.getUserId())
        );
        dataObject.setUpdater(
                String.valueOf(actor.getUserId())
        );
        dataObject.setCreateTime(now);
        dataObject.setUpdateTime(now);
        dataObject.setDeleted(false);

        if (dataObject
                instanceof com.cloud.framework.tenant.core.db.TenantBaseDO tenantDataObject) {
            tenantDataObject.setTenantId(
                    actor.getTenantId()
            );
        }
    }

    private void initializeUpdateFields(
            BaseDO dataObject,
            ChatActor actor,
            LocalDateTime now
    ) {
        dataObject.setUpdater(
                String.valueOf(actor.getUserId())
        );
        dataObject.setUpdateTime(now);
    }

    private void requireAffected(
            int affected,
            String message,
            Long sessionId,
            Long runId
    ) {
        if (affected != 1) {
            throw ChatStoreException.persistenceIntegrity(
                    message,
                    sessionId,
                    runId
            );
        }
    }

    private Long requirePositive(
            Long value,
            String fieldName
    ) {
        Objects.requireNonNull(
                value,
                fieldName + " must not be null"
        );

        if (value <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than 0"
            );
        }

        return value;
    }
}
