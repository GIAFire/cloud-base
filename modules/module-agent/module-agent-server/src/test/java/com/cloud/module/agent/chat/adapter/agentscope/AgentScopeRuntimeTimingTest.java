package com.cloud.module.agent.chat.adapter.agentscope;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.runtime.AgentInvocation;
import io.agentscope.core.event.ModelCallEndEvent;
import io.agentscope.core.event.ModelCallStartEvent;
import io.agentscope.core.event.ToolResultEndEvent;
import io.agentscope.core.event.ToolResultStartEvent;
import io.agentscope.core.message.ToolResultState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class AgentScopeRuntimeTimingTest {

    private Logger runtimeLogger;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUpLogCapture() {
        runtimeLogger = (Logger) LoggerFactory.getLogger(AgentScopeRuntime.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        runtimeLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDownLogCapture() {
        runtimeLogger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void logs_real_tool_execution_duration_and_model_call_index() {
        AgentScopeRuntime.RuntimeTimingState state = timingState();
        state.startModelCall(new ModelCallStartEvent("reply-1"));
        state.finishModelCall(new ModelCallEndEvent("reply-1", null));

        state.startToolCall(new ToolResultStartEvent(
                "reply-1",
                "tool-call-1",
                "session_search"
        ));
        state.finishToolCall(new ToolResultEndEvent(
                "reply-1",
                "tool-call-1",
                "session_search",
                ToolResultState.SUCCESS
        ));

        assertSingleMetricContains(
                "metric=agent.tool.duration",
                "outcome=SUCCESS",
                "toolName=session_search",
                "callIndex=1",
                "modelCallIndex=1",
                "toolCallId=tool-call-1",
                "replyId=reply-1",
                "sessionId=101",
                "runId=202"
        );
    }

    @Test
    void logs_api_to_first_text_token_separately() {
        AgentScopeRuntime.RuntimeTimingState state = timingState();

        state.firstTextTokenEmitted();
        state.firstTextTokenEmitted();

        assertSingleMetricContains(
                "metric=chat.api-to-first-text-token.duration",
                "outcome=FIRST_TEXT_TOKEN",
                "tenantId=1",
                "userId=2",
                "sessionId=101",
                "runId=202"
        );
    }

    private AgentScopeRuntime.RuntimeTimingState timingState() {
        AgentInvocation invocation = new AgentInvocation(
                100L,
                101L,
                202L,
                new ChatActor(1L, 2L),
                "你好"
        );
        return new AgentScopeRuntime.RuntimeTimingState(invocation);
    }

    private void assertSingleMetricContains(String metric, String... fragments) {
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .filteredOn(message -> message.contains(metric))
                .singleElement()
                .satisfies(message -> assertThat(message).contains(fragments));
    }
}
