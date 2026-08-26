package com.cloud.module.agent.chat.application.command;

import com.cloud.module.agent.chat.domain.ChatActor;
import lombok.Getter;

import java.util.Objects;

/**
 * Web Adapter 提交给聊天应用层的不可变命令。
 *
 * <p>{@code sessionId == null} 表示创建新会话；非空时表示在已有会话继续对话。
 * {@code requestId} 由客户端生成，是租户和用户范围内的幂等键。</p>
 */
@Getter
public class StartChatCommand {

    /** 会话标识；首次对话允许为空。 */
    private final Long sessionId;

    /** 客户端请求幂等键，防止重试导致 Agent 重复执行。 */
    private final String requestId;

    /** 本轮原始用户文本。 */
    private final String content;

    /** 从认证上下文解析出的租户和用户身份。 */
    private final ChatActor actor;

    /**
     * Controller 方法入口处取得的单调时钟值，用于跨层计算接口端到端耗时。
     */
    private final long requestStartedNanos;

    /**
     * 创建并立即校验命令，确保无效数据不会进入 ChatStore 或 AgentRuntime。
     */
    public StartChatCommand(Long sessionId, String requestId, String content, ChatActor actor) {
        this(sessionId, requestId, content, actor, System.nanoTime());
    }

    /**
     * 创建携带接口入口时间的命令。
     *
     * <p>该时间只用于计算 JVM 内耗时，不作为业务时间戳保存或传输。</p>
     */
    public StartChatCommand(
            Long sessionId,
            String requestId,
            String content,
            ChatActor actor,
            long requestStartedNanos
    ) {
        this.sessionId = sessionId;
        this.requestId = Objects.requireNonNull(requestId, "requestId 不能为空");
        this.content = Objects.requireNonNull(content, "content 不能为空");
        this.actor = Objects.requireNonNull(actor, "actor 不能为空");
        this.requestStartedNanos = requestStartedNanos;
        if (sessionId != null && sessionId <= 0) {
            throw new IllegalArgumentException("sessionId 不能为空");
        }
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("requestId 不能为空");
        }
        if (content.isBlank()) {
            throw new IllegalArgumentException("content 不能为空");
        }
    }
}
