package com.cloud.module.agent.chat.store;

import lombok.Getter;

@Getter
public final class ChatStoreException extends IllegalStateException {

    private final Reason reason;

    private final Long sessionId;

    private final Long runId;

    private ChatStoreException(
            Reason reason,
            String message,
            Long sessionId,
            Long runId,
            Throwable cause
    ) {
        super(message, cause);
        this.reason = reason;
        this.sessionId = sessionId;
        this.runId = runId;
    }

    public static ChatStoreException duplicateRequest(
            Long sessionId,
            Long runId
    ) {
        return new ChatStoreException(
                Reason.DUPLICATE_REQUEST,
                "The request has already been submitted",
                sessionId,
                runId,
                null
        );
    }

    public static ChatStoreException sessionNotFound(
            Long sessionId
    ) {
        return new ChatStoreException(
                Reason.SESSION_NOT_FOUND,
                "Chat session does not exist: " + sessionId,
                sessionId,
                null,
                null
        );
    }

    public static ChatStoreException sessionAccessDenied(
            Long sessionId
    ) {
        return new ChatStoreException(
                Reason.SESSION_ACCESS_DENIED,
                "Chat session does not belong to the current actor",
                sessionId,
                null,
                null
        );
    }

    public static ChatStoreException sessionBusy(
            Long sessionId,
            Long runId
    ) {
        return new ChatStoreException(
                Reason.SESSION_BUSY,
                "Chat session already has a running run",
                sessionId,
                runId,
                null
        );
    }

    public static ChatStoreException runNotFound(
            Long sessionId,
            Long runId
    ) {
        return new ChatStoreException(
                Reason.RUN_NOT_FOUND,
                "Agent run does not exist: " + runId,
                sessionId,
                runId,
                null
        );
    }

    public static ChatStoreException finalizationConflict(
            Long sessionId,
            Long runId
    ) {
        return new ChatStoreException(
                Reason.FINALIZATION_CONFLICT,
                "Run has already been finalized with a different result: "
                        + runId,
                sessionId,
                runId,
                null
        );
    }

    public static ChatStoreException persistenceIntegrity(
            String message,
            Long sessionId,
            Long runId
    ) {
        return new ChatStoreException(
                Reason.PERSISTENCE_INTEGRITY_ERROR,
                message,
                sessionId,
                runId,
                null
        );
    }

    public enum Reason {
        DUPLICATE_REQUEST,
        SESSION_NOT_FOUND,
        SESSION_ACCESS_DENIED,
        SESSION_BUSY,
        RUN_NOT_FOUND,
        FINALIZATION_CONFLICT,
        PERSISTENCE_INTEGRITY_ERROR
    }
}