package com.cloud.module.agent.chat.adapter.agentscope;

import com.cloud.module.agent.chat.runtime.AgentInvocation;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.Objects;

/**
 * 在 Reactor Context 中传递单次聊天调用的观测标识。
 *
 * <p>HarnessAgent 的记忆刷新会切换到 boundedElastic 线程，不能依赖
 * ThreadLocal 或 MDC 继续取得当前 Run。Reactor Context 会随订阅链传播，
 * 因此记忆模型即使换线程执行，也能把耗时准确关联到 tenant、user、
 * session 和 run。</p>
 */
final class AgentTimingContext {

    /** 直接使用类型作为 Context key，避免与第三方字符串 key 冲突。 */
    private static final Object CONTEXT_KEY = AgentTimingContext.class;

    private final Long tenantId;

    private final Long userId;

    private final Long sessionId;

    private final Long runId;

    private AgentTimingContext(AgentInvocation invocation) {
        this.tenantId = invocation.getActor().getTenantId();
        this.userId = invocation.getActor().getUserId();
        this.sessionId = invocation.getSessionId();
        this.runId = invocation.getRunId();
    }

    /** 将当前调用标识写入上游可见的 Reactor Context。 */
    static Context write(Context context, AgentInvocation invocation) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(invocation, "invocation must not be null");
        return context.put(CONTEXT_KEY, new AgentTimingContext(invocation));
    }

    /**
     * 读取当前调用标识。
     *
     * <p>记忆模型也可能被框架在聊天链路之外调用，此时返回空标识而不是让
     * 观测代码影响真实模型调用。</p>
     */
    static AgentTimingContext read(ContextView contextView) {
        if (contextView == null || !contextView.hasKey(CONTEXT_KEY)) {
            return empty();
        }
        return contextView.get(CONTEXT_KEY);
    }

    private static AgentTimingContext empty() {
        return new AgentTimingContext();
    }

    private AgentTimingContext() {
        this.tenantId = null;
        this.userId = null;
        this.sessionId = null;
        this.runId = null;
    }

    Long getTenantId() {
        return tenantId;
    }

    Long getUserId() {
        return userId;
    }

    Long getSessionId() {
        return sessionId;
    }

    Long getRunId() {
        return runId;
    }
}
