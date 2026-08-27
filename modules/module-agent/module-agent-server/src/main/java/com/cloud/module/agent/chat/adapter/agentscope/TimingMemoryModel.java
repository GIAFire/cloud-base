package com.cloud.module.agent.chat.adapter.agentscope;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ToolSchema;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

import static com.cloud.module.agent.chat.observability.ChatTiming.elapsedMillis;

/**
 * 只用于 Harness 长期记忆管线的模型耗时装饰器。
 *
 * <p>AgentScope 的 MemoryFlushMiddleware 和 MemoryMaintenanceMiddleware 会直接
 * 调用 Model，不会发布主 Agent 的 MODEL_CALL_START/END 事件。如果仍然复用
 * 主模型而不加装饰，这些隐藏调用就会落入 SSE 总耗时，却不会出现在模型
 * 指标中。</p>
 *
 * <p>本装饰器根据框架传入的系统提示词区分两类内部调用：</p>
 * <ul>
 *     <li>{@code agent.memory.flush.model.duration}：从对话中提取长期记忆；</li>
 *     <li>{@code agent.memory.maintenance.model.duration}：合并、整理长期记忆。</li>
 * </ul>
 *
 * <p>指标名显式包含 {@code model}，表示这里只计算模型流本身。文件读取、
 * 写入、消息 offload 等完整后处理由
 * {@code agent.memory.post-processing.duration} 统一覆盖。</p>
 */
@Slf4j
final class TimingMemoryModel implements Model {

    private static final String UNKNOWN_MEMORY_METRIC =
            "agent.memory.unknown.model.duration";

    private final Model delegate;

    private final String flushSystemPrompt;

    private final String maintenanceSystemPrompt;

    TimingMemoryModel(
            Model delegate,
            String flushSystemPrompt,
            String maintenanceSystemPrompt
    ) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.flushSystemPrompt = Objects.requireNonNull(
                flushSystemPrompt,
                "flushSystemPrompt must not be null"
        );
        this.maintenanceSystemPrompt = Objects.requireNonNull(
                maintenanceSystemPrompt,
                "maintenanceSystemPrompt must not be null"
        );
    }

    /**
     * 延迟到订阅时开始计时，确保等待、重试和响应流消费都计入模型耗时。
     */
    @Override
    public Flux<ChatResponse> stream(
            List<Msg> messages,
            List<ToolSchema> tools,
            GenerateOptions options
    ) {
        MemoryOperation operation = identifyOperation(messages);

        return Flux.deferContextual(contextView -> {
            long startedNanos = System.nanoTime();
            AgentTimingContext timingContext = AgentTimingContext.read(contextView);

            /*
             * 再套一层 Flux.defer：delegate.stream 如果同步抛出异常，也会转成
             * Reactor onError，确保 doFinally 仍能打印失败耗时。
             */
            return Flux.defer(() -> delegate.stream(messages, tools, options))
                    .doFinally(signalType -> log.info(
                            "chat-timing metric={} durationMs={} outcome={} "
                                    + "operation={} modelName={} tenantId={} userId={} "
                                    + "sessionId={} runId={}",
                            operation.metric,
                            elapsedMillis(startedNanos),
                            signalType,
                            operation.logValue,
                            delegate.getModelName(),
                            timingContext.getTenantId(),
                            timingContext.getUserId(),
                            timingContext.getSessionId(),
                            timingContext.getRunId()
                    ));
        });
    }

    /** 使用第一个系统消息识别 AgentScope 发起的记忆操作。 */
    private MemoryOperation identifyOperation(List<Msg> messages) {
        if (messages == null) {
            return MemoryOperation.UNKNOWN;
        }

        String systemPrompt = messages.stream()
                .filter(Objects::nonNull)
                .filter(message -> message.getRole() == MsgRole.SYSTEM)
                .map(Msg::getTextContent)
                .findFirst()
                .orElse(null);

        if (flushSystemPrompt.equals(systemPrompt)) {
            return MemoryOperation.FLUSH;
        }
        if (maintenanceSystemPrompt.equals(systemPrompt)) {
            return MemoryOperation.MAINTENANCE;
        }
        return MemoryOperation.UNKNOWN;
    }

    @Override
    public String getModelName() {
        return delegate.getModelName();
    }

    @Override
    public boolean supportsNativeStructuredOutput() {
        return delegate.supportsNativeStructuredOutput();
    }

    @Override
    public boolean supportsNativeStructuredOutputWithTools() {
        return delegate.supportsNativeStructuredOutputWithTools();
    }

    @Override
    public int getContextWindowSize() {
        return delegate.getContextWindowSize();
    }

    /** 记忆模型调用类型及其稳定日志名称。 */
    private enum MemoryOperation {
        FLUSH("agent.memory.flush.model.duration", "FLUSH"),
        MAINTENANCE("agent.memory.maintenance.model.duration", "MAINTENANCE"),
        UNKNOWN(UNKNOWN_MEMORY_METRIC, "UNKNOWN");

        private final String metric;

        private final String logValue;

        MemoryOperation(String metric, String logValue) {
            this.metric = metric;
            this.logValue = logValue;
        }
    }
}
