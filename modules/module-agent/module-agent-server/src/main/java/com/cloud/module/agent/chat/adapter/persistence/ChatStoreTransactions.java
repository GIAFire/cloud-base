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

/**
 * MySQL ChatStore 的同步事务实现。
 *
 * <p>该类位于 MybatisChatStore 内部，不形成新的外部 seam。公开方法保持同步，
 * 使 Spring {@code @Transactional} 能覆盖真正执行的 Mapper 调用；Reactor 调度
 * 由外层 Adapter 负责。</p>
 *
 * <p>并发不变量：</p>
 * <ul>
 *     <li>requestId 由数据库唯一键和事务内预检查共同保证幂等；</li>
 *     <li>同一 Session 同时只允许一个 RUNNING Run；</li>
 *     <li>Session 行锁保护 lastMessageSeq 的分配；</li>
 *     <li>begin/finish 均按 Session 再 Run 的顺序获取行锁，降低死锁风险；</li>
 *     <li>相同 finalization 可重复提交，不同 finalization 必须冲突。</li>
 * </ul>
 */
public class ChatStoreTransactions {

    /** 首条用户消息生成的默认标题最大字符数。 */
    private static final int TITLE_MAX_LENGTH = 100;

    /** Session 表读写和行锁查询。 */
    private final AgentSessionMapper sessionMapper;

    /** Run 表读写、幂等查询和行锁查询。 */
    private final AgentRunMapper runMapper;

    /** 最终业务消息的读写。 */
    private final AgentMessageMapper messageMapper;

    /** 当前基座固定 Agent 标识，新会话必须绑定到它。 */
    private final Long agentId;

    /** 使用显式依赖构造事务实现，便于测试中替换 Mapper。 */
    public ChatStoreTransactions(
            AgentSessionMapper sessionMapper,
            AgentRunMapper runMapper,
            AgentMessageMapper messageMapper,
            Long agentId
    ) {
        this.sessionMapper = Objects.requireNonNull(
                sessionMapper, "sessionMapper must not be null"
        );
        this.runMapper = Objects.requireNonNull(
                runMapper, "runMapper must not be null"
        );
        this.messageMapper = Objects.requireNonNull(
                messageMapper, "messageMapper must not be null"
        );
        this.agentId = requirePositive(agentId, "agentId");
    }

    /**
     * 原子开始一次 Run。
     *
     * <p>事务提交前依次完成：requestId 查重、解析并锁定 Session、检查活动
     * Run、创建 RUNNING Run、分配消息序号、保存 USER_TEXT、更新 Session
     * 游标。任一步失败都会整体回滚。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public StartedRun begin(StartChatCommand command) {
        Objects.requireNonNull(command,"command 不能为空");

        ChatActor actor = command.getActor();

        AgentRunDO duplicateRun = runMapper.selectByUserAndRequestId(actor.getUserId(),command.getRequestId());

        if (duplicateRun != null) {
            throw ChatStoreException.duplicateRequest(duplicateRun.getSessionId(),duplicateRun.getId());
        }

        LocalDateTime now = LocalDateTime.now();

        AgentSessionDO session = resolveSession(command, actor, now);

        rejectRunningSession(session);

        AgentRunDO run = createRun(command, session, actor, now);

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

        updateSessionMessagePosition(session, userMessageSeq, actor, now);

        return new StartedRun(session.getId(), run.getId());
    }

    /**
     * 原子结束一次 Run。
     *
     * <p>先锁 Session 再锁 Run。SUCCEEDED 会在同一事务中保存完整
     * ASSISTANT_TEXT 并推进 Session 游标；FAILED/CANCELLED 只更新 Run。
     * 事务成功返回后，应用层才允许发送终止 SSE。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void finish(RunFinalization finalization) {
        Objects.requireNonNull(finalization, "finalization must not be null");

        ChatActor actor = finalization.getActor();

        /*
         * begin 和 finish 始终按照 Session -> Run 的顺序加锁，
         * 避免并发 begin/finish 形成死锁。
         */
        AgentSessionDO session = sessionMapper.selectByIdForUpdate(finalization.getSessionId());

        if (session == null) {
            throw ChatStoreException.sessionNotFound(finalization.getSessionId());
        }

        requireSessionOwner(session, actor);

        AgentRunDO run = runMapper.selectByIdForUpdate(finalization.getRunId());

        if (run == null) {
            throw ChatStoreException.runNotFound(finalization.getSessionId(), finalization.getRunId());
        }

        requireRunOwner(run, finalization);

        AgentRunStatus persistedStatus = parseRunStatus(run);

        if (persistedStatus != AgentRunStatus.RUNNING) {
            requireSameFinalization(run, persistedStatus, finalization);
            return;
        }

        LocalDateTime endedAt = LocalDateTime.now();

        if (finalization.getStatus() == AgentRunStatus.SUCCEEDED) {
            long assistantMessageSeq = nextSequence(session);

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

            updateSessionMessagePosition(session, assistantMessageSeq, actor, endedAt);
        }

        updateRunFinalization(run, finalization, actor, endedAt);
    }

    /** 创建新 Session，或锁定并校验已有 Session。 */
    private AgentSessionDO resolveSession(StartChatCommand command, ChatActor actor,LocalDateTime now) {
        if (command.getSessionId() == null) {
            return createSession(command, actor, now);
        }

        AgentSessionDO session = sessionMapper.selectByIdForUpdate(command.getSessionId());

        if (session == null) {
            throw ChatStoreException.sessionNotFound(command.getSessionId());
        }

        requireSessionOwner(session, actor);

        return session;
    }

    /**
     * 创建属于当前 Agent 和 actor 的 Session，并以 0 初始化消息序号游标。
     */
    private AgentSessionDO createSession(StartChatCommand command, ChatActor actor, LocalDateTime now) {
        AgentSessionDO session = new AgentSessionDO();

        session.setAgentId(agentId);
        session.setUserId(actor.getUserId());
        session.setTitle(createTitle(command.getContent()));
        session.setLastMessageSeq(0L);
        session.setLastMessageAt(null);

        initializeInsertFields(session, actor, now);

        int inserted = sessionMapper.insert(session);

        requireAffected(inserted, "Failed to create chat session", null, null);

        if (session.getId() == null) {
            throw ChatStoreException.persistenceIntegrity("Chat session ID 不能为空", null, null);
        }

        return session;
    }

    /**
     * 在 Session 锁范围内检查是否已有 RUNNING Run，禁止同一会话并发生成。
     */
    private void rejectRunningSession(AgentSessionDO session) {
        AgentRunDO runningRun = runMapper.selectRunningBySessionIdForUpdate(session.getId());

        if (runningRun != null) {
            throw ChatStoreException.sessionBusy(session.getId(), runningRun.getId());
        }
    }

    /**
     * 创建 RUNNING Run；数据库唯一键负责兜住并发 requestId 竞争。
     */
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

        initializeInsertFields(run, actor, now);

        try {
            int inserted = runMapper.insert(run);

            requireAffected(inserted,"创建agent run出错", session.getId(), null);
        } catch (DuplicateKeyException exception) {
            throw ChatStoreException.duplicateRequest(session.getId(), null);
        }

        if (run.getId() == null) {
            throw ChatStoreException.persistenceIntegrity("Agent run ID 不能为空", session.getId(), null);
        }

        return run;
    }

    /** 保存一条完整业务消息；流式 delta 不经过该方法。 */
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

        initializeInsertFields(message, actor, now);

        int inserted = messageMapper.insert(message);

        requireAffected(inserted,"Failed to persist chat message", sessionId, runId);
    }

    /**
     * 推进 Session 的最后消息序号和时间，并同步更新当前事务内对象。
     */
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

        requireAffected(affected,"Failed to update chat session position",session.getId(),null);

        session.setLastMessageSeq(sequence);
        session.setLastMessageAt(now);
    }

    /** 写入 Run 终态、已脱敏错误和基于应用时钟计算的持续时间。 */
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
        update.setDurationMs(calculateDuration(run.getStartedAt(), endedAt)
        );

        initializeUpdateFields(update, actor, endedAt);

        int affected = runMapper.updateById(update);

        requireAffected(affected, "Failed to finalize agent run", run.getSessionId(), run.getId());
    }

    /**
     * 校验重复 finish 是否与已保存结果完全一致。
     *
     * <p>SUCCEEDED 需要额外读取最终助手消息比较正文；FAILED 比较错误码和
     * 文案；CANCELLED 没有附加结果。</p>
     */
    private void requireSameFinalization(
            AgentRunDO run,
            AgentRunStatus persistedStatus,
            RunFinalization finalization
    ) {
        if (persistedStatus != finalization.getStatus()) {
            throw ChatStoreException.finalizationConflict(run.getSessionId(), run.getId());
        }

        switch (persistedStatus) {
            case SUCCEEDED -> {
                AgentMessageDO assistantMessage = messageMapper.selectAssistantTextByRunId(run.getId());

                if (assistantMessage == null || !Objects.equals(assistantMessage.getTextContent(), finalization.getAssistantContent())) {
                    throw ChatStoreException.finalizationConflict(run.getSessionId(),run.getId());
                }
            }

            case FAILED -> {
                if (!Objects.equals(run.getErrorCode(), finalization.getErrorCode()) || !Objects.equals(run.getErrorMessage(), finalization.getErrorMessage())) {
                    throw ChatStoreException.finalizationConflict(run.getSessionId(), run.getId());
                }
            }

            case CANCELLED -> {
                // 相同 CANCELLED 结果可以幂等返回。
            }

            case RUNNING -> throw ChatStoreException.persistenceIntegrity(
                            "Unexpected RUNNING finalization state",
                            run.getSessionId(), run.getId());
        }
    }

    /** 校验 Session 同时属于当前租户、用户和固定 Agent。 */
    private void requireSessionOwner(AgentSessionDO session, ChatActor actor) {
        if (!Objects.equals(session.getUserId(), actor.getUserId())) {
            throw ChatStoreException.sessionAccessDenied(session.getId());
        }

        if (!Objects.equals(session.getTenantId(),actor.getTenantId())) {
            throw ChatStoreException.sessionAccessDenied(session.getId());
        }

        if (!Objects.equals(session.getAgentId(),agentId)) {
            throw ChatStoreException.sessionAccessDenied(session.getId());
        }
    }

    /** 校验 Run 与 finalization 的 Session 和 actor 完全一致。 */
    private void requireRunOwner(AgentRunDO run, RunFinalization finalization) {
        ChatActor actor = finalization.getActor();

        if (!Objects.equals(run.getSessionId(), finalization.getSessionId())
                || !Objects.equals(run.getUserId(), actor.getUserId())
                || !Objects.equals(run.getTenantId(), actor.getTenantId())) {
            throw ChatStoreException.finalizationConflict(
                    finalization.getSessionId(),
                    finalization.getRunId()
            );
        }
    }

    /**
     * 将数据库字符串解析为领域状态；未知值视为数据完整性错误而非运行失败。
     */
    private AgentRunStatus parseRunStatus(AgentRunDO run) {
        try {
            return AgentRunStatus.valueOf(run.getStatus());
        } catch (RuntimeException exception) {
            throw ChatStoreException.persistenceIntegrity(
                    "Unknown agent run status: "
                            + run.getStatus(),
                    run.getSessionId(),
                    run.getId()
            );
        }
    }

    /**
     * 在已锁定的 Session 快照上计算下一个全局消息序号。
     */
    private long nextSequence(AgentSessionDO session) {
        Long current = session.getLastMessageSeq();

        return current == null ? 1L : Math.addExact(current, 1L);
    }

    /** 防御时钟回拨，将异常的负持续时间收敛为 0。 */
    private long calculateDuration(LocalDateTime startedAt, LocalDateTime endedAt) {
        if (startedAt == null) {
            return 0L;
        }

        return Math.max(0L,Duration.between(startedAt, endedAt).toMillis());
    }

    /** 使用首条用户消息生成安全截断的默认会话标题。 */
    private String createTitle(String content) {
        String title = content.strip();

        if (title.length() <= TITLE_MAX_LENGTH) {
            return title;
        }

        return title.substring(0, TITLE_MAX_LENGTH);
    }

    /**
     * 显式填充租户和审计字段，使异步线程中的写入不依赖原 HTTP ThreadLocal。
     */
    private void initializeInsertFields(BaseDO dataObject, ChatActor actor, LocalDateTime now) {
        dataObject.setCreator(String.valueOf(actor.getUserId()));
        dataObject.setUpdater(String.valueOf(actor.getUserId()));
        dataObject.setCreateTime(now);
        dataObject.setUpdateTime(now);
        dataObject.setDeleted(false);

        if (dataObject instanceof com.cloud.framework.tenant.core.db.TenantBaseDO tenantDataObject) {
            tenantDataObject.setTenantId(actor.getTenantId());
        }
    }

    /** 为局部更新对象填充更新人和更新时间。 */
    private void initializeUpdateFields(BaseDO dataObject, ChatActor actor, LocalDateTime now) {
        dataObject.setUpdater(String.valueOf(actor.getUserId()));
        dataObject.setUpdateTime(now);
    }

    /** 所有单行写操作都必须恰好影响一行，否则事务回滚。 */
    private void requireAffected(int affected, String message, Long sessionId, Long runId) {
        if (affected != 1) {
            throw ChatStoreException.persistenceIntegrity(message, sessionId, runId);
        }
    }

    private Long requirePositive(Long value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }

        return value;
    }
}
