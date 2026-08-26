package com.cloud.module.agent.controller.admin.chat;

import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.chat.application.ChatApplication;
import com.cloud.module.agent.chat.domain.ChatEvent;
import com.cloud.module.agent.framework.web.config.AgentRuntimeWebMvcConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {

    private MockMvc mockMvc;
    private AnnotationConfigWebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        ChatApplication chatApplication =
                command -> Flux.just(
                        ChatEvent.runStarted(
                                101L,
                                202L,
                                1
                        ),
                        ChatEvent.messageDelta(
                                101L,
                                202L,
                                2,
                                "你好"
                        ),
                        ChatEvent.runCompleted(
                                101L,
                                202L,
                                3
                        )
                );

        webApplicationContext = new AnnotationConfigWebApplicationContext();
        webApplicationContext.setServletContext(new MockServletContext());
        webApplicationContext.register(
                ControllerTestWebConfiguration.class,
                AgentRuntimeWebMvcConfiguration.class
        );
        webApplicationContext.register((registry, environment) ->
                registry.registerBean(
                        ChatApplication.class,
                        specification -> specification.supplier(context -> chatApplication)
                )
        );
        webApplicationContext.refresh();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        LoginUser loginUser =
                new LoginUser();

        loginUser.setId(2L);
        loginUser.setTenantId(1L);
        loginUser.setUserType(1);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        loginUser,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        webApplicationContext.close();
    }

    @Test
    void returns_chat_events_as_sse() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        post(
                                "/agent/chat/stream"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .accept(
                                        MediaType.TEXT_EVENT_STREAM
                                )
                                .content("""
                                        {
                                          "sessionId": "101",
                                          "requestId": "request-001",
                                          "content": "你好"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        mvcResult.getAsyncResult(2_000);

        MvcResult completedResult =
                mockMvc.perform(
                                asyncDispatch(mvcResult)
                        )
                        .andExpect(status().isOk())
                        .andExpect(
                                content()
                                        .contentTypeCompatibleWith(
                                                MediaType.TEXT_EVENT_STREAM
                                        )
                        )
                        .andReturn();

        String body = completedResult
                .getResponse()
                .getContentAsString(
                        StandardCharsets.UTF_8
                );

        assertThat(body)
                .contains("event:run.started")
                .contains("event:message.delta")
                .contains("event:run.completed")
                .contains("id:202:1")
                .contains("id:202:2")
                .contains("id:202:3")
                .contains("\"sessionId\":\"101\"")
                .contains("\"runId\":\"202\"")
                .contains("\"delta\":\"你好\"");

        int startedIndex =
                body.indexOf(
                        "event:run.started"
                );

        int deltaIndex =
                body.indexOf(
                        "event:message.delta"
                );

        int completedIndex =
                body.indexOf(
                        "event:run.completed"
                );

        assertThat(startedIndex)
                .isLessThan(deltaIndex);

        assertThat(deltaIndex)
                .isLessThan(completedIndex);
    }

    @Test
    void rejects_session_id_above_long_max_value() throws Exception {
        mockMvc.perform(
                        post("/agent/chat/stream")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.TEXT_EVENT_STREAM)
                                .content("""
                                        {
                                          "sessionId": "9223372036854775808",
                                          "requestId": "request-001",
                                          "content": "你好"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(request().asyncNotStarted());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    static class ControllerTestWebConfiguration {

        @Bean
        ChatController chatController(ChatApplication chatApplication) {
            return new ChatController(chatApplication);
        }
    }
}
