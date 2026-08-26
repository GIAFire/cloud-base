package com.cloud.module.agent.chat.application.model;

import com.cloud.module.agent.chat.domain.AgentRunStatus;
import com.cloud.module.agent.chat.domain.ChatActor;
import lombok.Getter;

import java.util.Objects;

/**
 * 应用层提交给 ChatStore 的 Run 最终结果。
 *
 * <p>该类型通过静态工厂保证三种终态互斥：</p>
 * <ul>
 *     <li>SUCCEEDED：必须携带完整助手正文，不能携带错误；</li>
 *     <li>FAILED：必须携带已脱敏的错误码和错误信息；</li>
 *     <li>CANCELLED：不携带正文或错误。</li>
 * </ul>
 *
 * <p>actor 必须随最终结果传递，因为 Agent 流和 JDBC 可能运行在不同线程，
 * 存储 Adapter 需要据此恢复租户上下文并校验数据归属。</p>
 */
@Getter
public final class RunFinalization {

    /** 被收尾的业务会话。 */
    private final Long sessionId;

    /** 被收尾的单次运行。 */
    private final Long runId;

    /** 发起本次运行的租户用户身份。 */
    private final ChatActor actor;

    /** 最终状态，不允许为 RUNNING。 */
    private final AgentRunStatus status;

    /** SUCCEEDED 独有的完整助手正文。 */
    private final String assistantContent;

    /** FAILED 独有的安全错误码。 */
    private final String errorCode;

    /** FAILED 独有的已脱敏错误信息。 */
    private final String errorMessage;

    private RunFinalization(
            Long sessionId,
            Long runId,
            ChatActor actor,
            AgentRunStatus status,
            String assistantContent,
            String errorCode,
            String errorMessage
    ) {
        this.sessionId = requirePositive(sessionId,"sessionId");
        this.runId = requirePositive(runId, "runId");
        this.actor = Objects.requireNonNull(actor, "actor must not be null");
        this.status = Objects.requireNonNull(status,"status must not be null");

        if (status == AgentRunStatus.RUNNING) {
            throw new IllegalArgumentException(
                    "finalization status must not be RUNNING"
            );
        }

        validateContent(
                status,
                assistantContent,
                errorCode,
                errorMessage
        );

        this.assistantContent = assistantContent;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static RunFinalization succeeded(
            Long sessionId,
            Long runId,
            ChatActor actor,
            String assistantContent
    ) {
        return new RunFinalization(
                sessionId,
                runId,
                actor,
                AgentRunStatus.SUCCEEDED,
                assistantContent,
                null,
                null
        );
    }

    public static RunFinalization failed(
            Long sessionId,
            Long runId,
            ChatActor actor,
            String errorCode,
            String errorMessage
    ) {
        return new RunFinalization(
                sessionId,
                runId,
                actor,
                AgentRunStatus.FAILED,
                null,
                errorCode,
                errorMessage
        );
    }

    public static RunFinalization cancelled(Long sessionId, Long runId, ChatActor actor) {
        return new RunFinalization(
                sessionId,
                runId,
                actor,
                AgentRunStatus.CANCELLED,
                null,
                null,
                null
        );
    }

    private static void validateContent(
            AgentRunStatus status,
            String assistantContent,
            String errorCode,
            String errorMessage
    ) {
        switch (status) {
            case SUCCEEDED -> {
                requireText(assistantContent,"assistantContent");

                if (errorCode != null || errorMessage != null) {
                    throw new IllegalArgumentException(
                            "successful finalization " + "must not contain an error"
                    );
                }
            }

            case FAILED -> {
                requireText(errorCode, "errorCode");
                requireText(errorMessage, "errorMessage");

                if (assistantContent != null) {
                    throw new IllegalArgumentException(
                            "failed finalization " + "must not contain assistantContent"
                    );
                }
            }

            case CANCELLED -> {
                if (assistantContent != null || errorCode != null || errorMessage != null) {
                    throw new IllegalArgumentException(
                            "cancelled finalization " + "must not contain result content"
                    );
                }
            }

            case RUNNING -> throw
                    new IllegalArgumentException("RUNNING is not a final status");
        }
    }

    private static Long requirePositive(Long value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }

        return value;
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
    }
}
