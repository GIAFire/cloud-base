package com.cloud.module.agent.chat.adapter.agentscope;

import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.runtime.AgentInvocation;
import com.cloud.module.agent.chat.runtime.AgentRuntime;
import com.cloud.module.agent.chat.runtime.RuntimeEvent;
import com.cloud.module.agent.chat.runtime.definition.AgentDefinitionLoader;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ModelCallEndEvent;
import io.agentscope.core.event.ModelCallStartEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ThinkingBlockDeltaEvent;
import io.agentscope.core.event.ToolResultEndEvent;
import io.agentscope.core.event.ToolResultStartEvent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.SynchronousSink;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.cloud.module.agent.chat.observability.ChatTiming.elapsedMillis;

/**
 * 使用 AgentScope HarnessAgent 实现 AgentRuntime 的 Adapter。
 *
 * <p>该类只负责三件事：把业务身份转换为 RuntimeContext、把当前用户文本
 * 转换为 UserMessage、把 AgentEvent 压缩为模块认可的 RuntimeEvent。历史
 * 对话由 HarnessAgent 依据 RuntimeContext 从 AgentScope StateStore 管理。</p>
 *
 * <p>每次调用先按 agentId 加载数据库定义，再创建独立 HarnessAgent；完成、
 * 失败和取消都会关闭该实例。AgentScope 异常和取消信号保持 Reactor 原语义
 * 向上传播。</p>
 */
@Slf4j
public final class AgentScopeRuntime implements AgentRuntime {

    private final AgentDefinitionLoader definitionLoader;

    private final HarnessAgentFactory harnessAgentFactory;

    public AgentScopeRuntime(
            AgentDefinitionLoader definitionLoader,
            HarnessAgentFactory harnessAgentFactory
    ) {
        this.definitionLoader = Objects.requireNonNull(
                definitionLoader,
                "definitionLoader 不能为空"
        );
        this.harnessAgentFactory = Objects.requireNonNull(
                harnessAgentFactory,
                "harnessAgentFactory 不能为空"
        );
    }

    /**
     * 为每次订阅延迟创建 RuntimeContext 和 UserMessage，避免未订阅时修改
     * AgentScope 会话状态。
     */
    @Override
    public Flux<RuntimeEvent> stream(AgentInvocation invocation) {
        Objects.requireNonNull(invocation, "invocation must not be null");

        return definitionLoader.load(invocation.getAgentId(), invocation.getActor())
                .switchIfEmpty(Mono.error(
                        new IllegalStateException("AgentDefinitionLoader returned empty")
                ))
                .flatMapMany(definition -> Flux.using(
                        () -> harnessAgentFactory.create(definition, invocation.getActor()),
                        harnessAgent -> streamHarnessAgent(harnessAgent, invocation),
                        HarnessAgent::close
                ));
    }

    /** 将单次 HarnessAgent 调用延迟到订阅时执行。 */
    private Flux<RuntimeEvent> streamHarnessAgent(
            HarnessAgent harnessAgent,
            AgentInvocation invocation
    ) {
        return Flux.defer(() -> {
            RuntimeContext runtimeContext = createRuntimeContext(invocation);
            RuntimeTimingState timingState = new RuntimeTimingState(invocation);

            UserMessage userMessage = new UserMessage("user", invocation.getContent());

            /*
             * 这里只传当前用户消息。
             *
             * HarnessAgent 会根据 RuntimeContext 中的 userId、sessionId，
             * 从 AgentScope StateStore 加载之前的 AgentState，
             * 并在运行完成后保存新的上下文。
             */
            return harnessAgent
                    .streamEvents(userMessage, runtimeContext)
                    .<RuntimeEvent>handle((event, sink) ->
                            mapAgentEvent(event, sink, timingState))
                    .doFinally(timingState::finishInvocation)
                    /*
                     * 记忆模型会在 boundedElastic 上执行。将调用标识写入 Reactor
                     * Context，保证换线程后仍能关联到当前 tenant/session/run。
                     */
                    .contextWrite(context ->
                            AgentTimingContext.write(context, invocation));
        });
    }

    /**
     * 使用“tenant-user + session”构造状态槽，保证同一 Agent 下多租户隔离。
     */
    private RuntimeContext createRuntimeContext(AgentInvocation invocation) {
        ChatActor actor = invocation.getActor();

        /*
         * AgentScope StateStore 使用 userId + sessionId 隔离上下文。
         */
        return RuntimeContext.builder()
                .userId(actor.getTenantId() + "-" + actor.getUserId())
                .sessionId(String.valueOf(invocation.getSessionId()))
                .build();
    }

    /**
     * 将 AgentScope 丰富事件集投影为聊天基座的最小事件协议。
     * ModelCall、BlockStart/End 等观测事件留在 Adapter 内部，不向应用层扩散。
     */
    private void mapAgentEvent(
            AgentEvent event,
            SynchronousSink<RuntimeEvent> sink,
            RuntimeTimingState timingState
    ) {
        switch (event.getType()) {
            case AGENT_START -> sink.next(RuntimeEvent.started());

            case MODEL_CALL_START ->
                    timingState.startModelCall((ModelCallStartEvent) event);

            case MODEL_CALL_END ->
                    timingState.finishModelCall((ModelCallEndEvent) event);

            /*
             * TOOL_CALL_START/END 只代表模型正在生成工具调用参数；真正的工具
             * 执行区间由 TOOL_RESULT_START/END 表示，因此在这里成对计时。
             */
            case TOOL_RESULT_START ->
                    timingState.startToolCall((ToolResultStartEvent) event);

            case TOOL_RESULT_END ->
                    timingState.finishToolCall((ToolResultEndEvent) event);

            case THINKING_BLOCK_DELTA -> {
                ThinkingBlockDeltaEvent deltaEvent = (ThinkingBlockDeltaEvent) event;
                if (hasText(deltaEvent.getDelta())) {
                    timingState.firstTokenEmitted();
                }
                mapThinkingDelta(deltaEvent, sink);
            }

            case TEXT_BLOCK_DELTA -> {
                TextBlockDeltaEvent deltaEvent = (TextBlockDeltaEvent) event;
                if (hasText(deltaEvent.getDelta())) {
                    timingState.firstTokenEmitted();
                    timingState.firstTextTokenEmitted();
                }
                mapTextDelta(deltaEvent, sink);
            }

            case AGENT_END -> {
                timingState.agentEnded();
                sink.next(RuntimeEvent.completed());
            }
            /*
             * ModelCall、BlockStart、BlockEnd 等内部事件暂时不向上暴露。
             */
            default -> {
            }
        }
    }

    /** 忽略供应商可能发送的空 Thinking chunk。 */
    private void mapThinkingDelta(
            ThinkingBlockDeltaEvent event,
            SynchronousSink<RuntimeEvent> sink
    ) {
        String delta = event.getDelta();

        if (delta != null && !delta.isEmpty()) {
            sink.next(RuntimeEvent.thinkingDelta(delta));
        }
    }

    /** 忽略供应商可能发送的空文本 chunk。 */
    private void mapTextDelta(
            TextBlockDeltaEvent event,
            SynchronousSink<RuntimeEvent> sink
    ) {
        String delta = event.getDelta();

        if (delta != null && !delta.isEmpty()) {
            sink.next(RuntimeEvent.textDelta(delta));
        }
    }

    /** 判断框架增量是否代表真实可见的 Token 内容。 */
    private boolean hasText(String delta) {
        return delta != null && !delta.isEmpty();
    }

    /**
     * 单次 AgentInvocation 的模型计时状态。
     *
     * <p>MODEL_CALL_START/END 可能出现多次，因此模型指标按 callIndex 分别
     * 记录；接口入口到首 Token 只在整个请求的第一段有效内容到达时记录一次。</p>
     */
    static final class RuntimeTimingState {

        private final AgentInvocation invocation;

        private int modelCallIndex;

        private long modelCallStartedNanos;

        private String replyId;

        private boolean modelCallActive;

        private boolean modelFirstTokenLogged;

        private boolean apiFirstTokenLogged;

        /** 接口入口到第一个非空正文 TextDelta 只记录一次。 */
        private boolean apiFirstTextTokenLogged;

        /** 单次 Run 内按工具真正开始执行的顺序递增。 */
        private int toolCallIndex;

        /**
         * 工具可能并行执行，必须按 toolCallId 分别保存起点，不能复用单个计时字段。
         */
        private final Map<String, ActiveToolCall> activeToolCalls = new HashMap<>();

        /** 最后一次主 Agent 模型调用结束的时刻。 */
        private long lastModelCallFinishedNanos;

        /** Agent 核心逻辑发布 AGENT_END 的时刻，即记忆后处理的起点。 */
        private long agentEndedNanos;

        RuntimeTimingState(AgentInvocation invocation) {
            this.invocation = invocation;
        }

        /** 开始计量一次真实模型调用。 */
        void startModelCall(ModelCallStartEvent event) {
            if (modelCallActive) {
                logModelCallFinished("REPLACED_BY_NEXT_MODEL_CALL");
            }

            modelCallIndex++;
            modelCallStartedNanos = System.nanoTime();
            replyId = event.getReplyId();
            modelCallActive = true;
            modelFirstTokenLogged = false;
        }

        /** 在首个非空 Thinking/Text delta 到达时记录两种首 Token 指标。 */
        private void firstTokenEmitted() {
            if (!apiFirstTokenLogged) {
                apiFirstTokenLogged = true;
                log.info(
                        "chat-timing metric=chat.api-to-model-first-token.duration durationMs={} "
                                + "outcome=FIRST_TOKEN tenantId={} userId={} sessionId={} runId={}",
                        elapsedMillis(invocation.getRequestStartedNanos()),
                        invocation.getActor().getTenantId(),
                        invocation.getActor().getUserId(),
                        invocation.getSessionId(),
                        invocation.getRunId()
                );
            }

            if (modelCallActive && !modelFirstTokenLogged) {
                modelFirstTokenLogged = true;
                log.info(
                        "chat-timing metric=agent.model.first-token.duration durationMs={} "
                                + "outcome=FIRST_TOKEN callIndex={} replyId={} sessionId={} runId={}",
                        elapsedMillis(modelCallStartedNanos),
                        modelCallIndex,
                        replyId,
                        invocation.getSessionId(),
                        invocation.getRunId()
                );
            }
        }

        /** 记录接口入口到首个非空助手正文 Token 的耗时。 */
        void firstTextTokenEmitted() {
            if (apiFirstTextTokenLogged) {
                return;
            }

            apiFirstTextTokenLogged = true;
            log.info(
                    "chat-timing metric=chat.api-to-first-text-token.duration durationMs={} "
                            + "outcome=FIRST_TEXT_TOKEN tenantId={} userId={} sessionId={} runId={}",
                    elapsedMillis(invocation.getRequestStartedNanos()),
                    invocation.getActor().getTenantId(),
                    invocation.getActor().getUserId(),
                    invocation.getSessionId(),
                    invocation.getRunId()
            );
        }

        /** 正常结束一次模型调用并记录完整调用时间。 */
        void finishModelCall(ModelCallEndEvent event) {
            if (!modelCallActive) {
                return;
            }

            if (event.getReplyId() != null) {
                replyId = event.getReplyId();
            }
            logModelCallFinished("SUCCESS");
        }

        /** 开始计量一次真正的工具执行，而不是模型生成工具参数的时间。 */
        void startToolCall(ToolResultStartEvent event) {
            String key = toolCallKey(
                    event.getSource(),
                    event.getReplyId(),
                    event.getToolCallId(),
                    event.getToolCallName()
            );

            toolCallIndex++;
            ActiveToolCall current = new ActiveToolCall(
                    toolCallIndex,
                    modelCallIndex,
                    System.nanoTime(),
                    event.getReplyId(),
                    event.getToolCallId(),
                    event.getToolCallName(),
                    event.getSource()
            );
            ActiveToolCall replaced = activeToolCalls.put(key, current);

            if (replaced != null) {
                logToolCallFinished(replaced, "REPLACED_BY_DUPLICATE_START");
            }
        }

        /** 使用框架返回的 ToolResultState 作为工具调用结果。 */
        void finishToolCall(ToolResultEndEvent event) {
            String key = toolCallKey(
                    event.getSource(),
                    event.getReplyId(),
                    event.getToolCallId(),
                    event.getToolCallName()
            );
            ActiveToolCall activeToolCall = activeToolCalls.remove(key);

            if (activeToolCall == null) {
                log.warn(
                        "chat-timing-unmatched event=TOOL_RESULT_END toolName={} toolCallId={} "
                                + "replyId={} source={} sessionId={} runId={}",
                        event.getToolCallName(),
                        event.getToolCallId(),
                        event.getReplyId(),
                        event.getSource(),
                        invocation.getSessionId(),
                        invocation.getRunId()
                );
                return;
            }

            String outcome = event.getState() == null
                    ? "UNKNOWN"
                    : event.getState().getValue().toUpperCase(Locale.ROOT);
            logToolCallFinished(activeToolCall, outcome);
        }

        /**
         * 记录 Agent 核心结束边界。
         *
         * <p>Harness 的 MemoryMaintenance/MemoryFlush 使用 concatWith 追加在
         * AGENT_END 之后，所以 AGENT_END 到 Flux 终止正好覆盖记忆后处理。</p>
         */
        private void agentEnded() {
            if (agentEndedNanos == 0L) {
                agentEndedNanos = System.nanoTime();
            }
        }

        /**
         * 流失败或取消时补记没有正常收到 MODEL_CALL_END 的模型调用。
         */
        private void finishInvocation(SignalType signalType) {
            if (modelCallActive) {
                logModelCallFinished(signalType.name());
            }

            if (!activeToolCalls.isEmpty()) {
                activeToolCalls.values().forEach(activeToolCall ->
                        logToolCallFinished(
                                activeToolCall,
                                "NO_END_" + signalType.name()
                        ));
                activeToolCalls.clear();
            }

            if (lastModelCallFinishedNanos != 0L) {
                log.info(
                        "chat-timing metric=agent.runtime.after-model.duration durationMs={} "
                                + "outcome={} tenantId={} userId={} sessionId={} runId={}",
                        elapsedMillis(lastModelCallFinishedNanos),
                        signalType,
                        invocation.getActor().getTenantId(),
                        invocation.getActor().getUserId(),
                        invocation.getSessionId(),
                        invocation.getRunId()
                );
            }

            if (agentEndedNanos != 0L) {
                log.info(
                        "chat-timing metric=agent.memory.post-processing.duration durationMs={} "
                                + "outcome={} tenantId={} userId={} sessionId={} runId={}",
                        elapsedMillis(agentEndedNanos),
                        signalType,
                        invocation.getActor().getTenantId(),
                        invocation.getActor().getUserId(),
                        invocation.getSessionId(),
                        invocation.getRunId()
                );
            }

            if (!apiFirstTokenLogged) {
                log.info(
                        "chat-timing metric=chat.api-to-model-first-token.duration durationMs={} "
                                + "outcome=NO_TOKEN_{} tenantId={} userId={} sessionId={} runId={}",
                        elapsedMillis(invocation.getRequestStartedNanos()),
                        signalType,
                        invocation.getActor().getTenantId(),
                        invocation.getActor().getUserId(),
                        invocation.getSessionId(),
                        invocation.getRunId()
                );
            }

            if (!apiFirstTextTokenLogged) {
                log.info(
                        "chat-timing metric=chat.api-to-first-text-token.duration durationMs={} "
                                + "outcome=NO_TEXT_TOKEN_{} tenantId={} userId={} "
                                + "sessionId={} runId={}",
                        elapsedMillis(invocation.getRequestStartedNanos()),
                        signalType,
                        invocation.getActor().getTenantId(),
                        invocation.getActor().getUserId(),
                        invocation.getSessionId(),
                        invocation.getRunId()
                );
            }
        }

        /** 输出工具执行耗时及其所属模型轮次，并移除活动计时状态。 */
        private void logToolCallFinished(ActiveToolCall toolCall, String outcome) {
            log.info(
                    "chat-timing metric=agent.tool.duration durationMs={} outcome={} "
                            + "toolName={} callIndex={} modelCallIndex={} toolCallId={} "
                            + "replyId={} source={} tenantId={} userId={} sessionId={} runId={}",
                    elapsedMillis(toolCall.startedNanos),
                    outcome,
                    toolCall.toolName,
                    toolCall.callIndex,
                    toolCall.modelCallIndex,
                    toolCall.toolCallId,
                    toolCall.replyId,
                    toolCall.source,
                    invocation.getActor().getTenantId(),
                    invocation.getActor().getUserId(),
                    invocation.getSessionId(),
                    invocation.getRunId()
            );
        }

        /**
         * source 参与 key，避免父 Agent 与转发进来的子 Agent 复用 toolCallId。
         * 框架通常总会提供 toolCallId；缺失时退化为 replyId + toolName。
         */
        private String toolCallKey(
                String source,
                String replyId,
                String toolCallId,
                String toolName
        ) {
            String effectiveId = toolCallId != null && !toolCallId.isBlank()
                    ? toolCallId
                    : String.valueOf(replyId) + ":" + String.valueOf(toolName);
            return String.valueOf(source) + ":" + effectiveId;
        }

        /** 输出模型首 Token 缺失状态和模型总耗时，并关闭当前调用计时。 */
        private void logModelCallFinished(String outcome) {
            if (!modelFirstTokenLogged) {
                log.info(
                        "chat-timing metric=agent.model.first-token.duration durationMs={} "
                                + "outcome=NO_TOKEN_{} callIndex={} replyId={} sessionId={} runId={}",
                        elapsedMillis(modelCallStartedNanos),
                        outcome,
                        modelCallIndex,
                        replyId,
                        invocation.getSessionId(),
                        invocation.getRunId()
                );
            }

            log.info(
                    "chat-timing metric=agent.model.total.duration durationMs={} outcome={} "
                            + "callIndex={} replyId={} sessionId={} runId={}",
                    elapsedMillis(modelCallStartedNanos),
                    outcome,
                    modelCallIndex,
                    replyId,
                    invocation.getSessionId(),
                    invocation.getRunId()
            );

            lastModelCallFinishedNanos = System.nanoTime();
            modelCallActive = false;
        }

        /** 单个活动工具调用的不可变计时快照。 */
        private static final class ActiveToolCall {

            private final int callIndex;

            private final int modelCallIndex;

            private final long startedNanos;

            private final String replyId;

            private final String toolCallId;

            private final String toolName;

            private final String source;

            private ActiveToolCall(
                    int callIndex,
                    int modelCallIndex,
                    long startedNanos,
                    String replyId,
                    String toolCallId,
                    String toolName,
                    String source
            ) {
                this.callIndex = callIndex;
                this.modelCallIndex = modelCallIndex;
                this.startedNanos = startedNanos;
                this.replyId = replyId;
                this.toolCallId = toolCallId;
                this.toolName = toolName;
                this.source = source;
            }
        }
    }
}
