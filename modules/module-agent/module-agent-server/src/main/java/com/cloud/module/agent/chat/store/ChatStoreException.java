package com.cloud.module.agent.chat.store;

import lombok.Getter;

/**
 * ChatStore 可预期业务冲突和持久化不变量错误。
 *
 * <p>Reason 是跨内存/MySQL Adapter 的稳定错误分类；sessionId/runId 用于
 * 安全日志和后续错误映射。该异常不承载数据库 SQL、模型响应或凭证信息。</p>
 */
@Getter
public final class ChatStoreException extends IllegalStateException {

    /** 机器可判定的失败原因。 */
    private final Reason reason;

    /** 已知时返回相关 Session。 */
    private final Long sessionId;

    /** 已知时返回相关 Run。 */
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

    public static ChatStoreException duplicateRequest(Long sessionId, Long runId) {
        return new ChatStoreException(
                Reason.DUPLICATE_REQUEST, "该请求已经提交,请勿重复提交", sessionId, runId, null);
    }

    public static ChatStoreException sessionNotFound(Long sessionId) {
        return new ChatStoreException(
                Reason.SESSION_NOT_FOUND,
                "该会话不存在: " + sessionId,
                sessionId, null, null);
    }

    public static ChatStoreException sessionAccessDenied(Long sessionId) {
        return new ChatStoreException(
                Reason.SESSION_ACCESS_DENIED,
                "该会话不属于当前用户", sessionId, null, null );
    }

    public static ChatStoreException sessionBusy(Long sessionId, Long runId) {
        return new ChatStoreException(
                Reason.SESSION_BUSY,
                "该会话已经存在一个正在运行中的状态",
                sessionId, runId, null
        );
    }

    public static ChatStoreException runNotFound(Long sessionId, Long runId) {
        return new ChatStoreException(
                Reason.RUN_NOT_FOUND,
                "Agent run does not exist: " + runId, sessionId, runId, null
        );
    }

    public static ChatStoreException finalizationConflict(Long sessionId, Long runId) {
        return new ChatStoreException(
                Reason.FINALIZATION_CONFLICT,
                "Run has already been finalized with a different result: "
                        + runId,
                sessionId, runId, null
        );
    }

    public static ChatStoreException persistenceIntegrity(String message, Long sessionId, Long runId) {
        return new ChatStoreException(
                Reason.PERSISTENCE_INTEGRITY_ERROR, message,
                sessionId, runId, null
        );
    }

    /** ChatStore 接口定义的稳定错误分类。 */
    public enum Reason {
        /** 同一租户、用户和 requestId 已创建过 Run。 */
        DUPLICATE_REQUEST,
        /** 指定 Session 不存在或已不可见。 */
        SESSION_NOT_FOUND,
        /** Session 不属于当前租户、用户或 Agent。 */
        SESSION_ACCESS_DENIED,
        /** Session 已有 RUNNING Run，拒绝并发执行。 */
        SESSION_BUSY,
        /** 指定 Run 不存在。 */
        RUN_NOT_FOUND,
        /** Run 已被不同结果收尾。 */
        FINALIZATION_CONFLICT,
        /** 数据行数、状态值或主键分配破坏了持久化不变量。 */
        PERSISTENCE_INTEGRITY_ERROR
    }
}
