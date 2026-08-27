package com.cloud.module.agent.chat.adapter.agentscope;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.runtime.AgentInvocation;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ToolSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimingMemoryModelTest {

    private static final String FLUSH_PROMPT = "flush-memory";

    private static final String MAINTENANCE_PROMPT = "maintain-memory";

    private Logger modelLogger;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUpLogCapture() {
        modelLogger = (Logger) LoggerFactory.getLogger(TimingMemoryModel.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        modelLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDownLogCapture() {
        modelLogger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void logs_flush_model_duration_with_chat_identity() {
        TimingMemoryModel model = timingModel();

        StepVerifier.create(
                        model.stream(systemMessages(FLUSH_PROMPT), null, null)
                                .contextWrite(context ->
                                        AgentTimingContext.write(context, invocation()))
                )
                .verifyComplete();

        assertSingleLogContains(
                "metric=agent.memory.flush.model.duration",
                "operation=FLUSH",
                "modelName=memory-model",
                "tenantId=1",
                "userId=2",
                "sessionId=101",
                "runId=202"
        );
    }

    @Test
    void logs_maintenance_model_duration() {
        TimingMemoryModel model = timingModel();

        StepVerifier.create(
                        model.stream(systemMessages(MAINTENANCE_PROMPT), null, null)
                                .contextWrite(context ->
                                        AgentTimingContext.write(context, invocation()))
                )
                .verifyComplete();

        assertSingleLogContains(
                "metric=agent.memory.maintenance.model.duration",
                "operation=MAINTENANCE",
                "sessionId=101",
                "runId=202"
        );
    }

    /** 构造只验证装饰行为、不发出任何内容的模型。 */
    private TimingMemoryModel timingModel() {
        Model delegate = new Model() {

            @Override
            public Flux<ChatResponse> stream(
                    List<Msg> messages,
                    List<ToolSchema> tools,
                    GenerateOptions options
            ) {
                return Flux.empty();
            }

            @Override
            public String getModelName() {
                return "memory-model";
            }
        };

        return new TimingMemoryModel(
                delegate,
                FLUSH_PROMPT,
                MAINTENANCE_PROMPT
        );
    }

    private List<Msg> systemMessages(String prompt) {
        return List.of(
                Msg.builder()
                        .role(MsgRole.SYSTEM)
                        .textContent(prompt)
                        .build()
        );
    }

    private AgentInvocation invocation() {
        return new AgentInvocation(
                101L,
                202L,
                new ChatActor(1L, 2L),
                "你好"
        );
    }

    private void assertSingleLogContains(String... fragments) {
        assertThat(logAppender.list).hasSize(1);
        String message = logAppender.list.getFirst().getFormattedMessage();

        assertThat(message).contains(fragments);
    }
}
