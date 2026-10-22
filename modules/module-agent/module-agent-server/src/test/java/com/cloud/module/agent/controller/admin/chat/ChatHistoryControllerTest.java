package com.cloud.module.agent.controller.admin.chat;

import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.chat.application.ChatHistoryApplication;
import com.cloud.module.agent.chat.domain.ChatActor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatHistoryControllerTest {

    private ChatHistoryApplication chatHistoryApplication;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        chatHistoryApplication = mock(ChatHistoryApplication.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ChatHistoryController(chatHistoryApplication))
                .build();

        LoginUser loginUser = new LoginUser();
        loginUser.setId(20L);
        loginUser.setTenantId(10L);
        loginUser.setUserType(1);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        loginUser,
                        null,
                        Collections.emptyList()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deletes_session_as_current_tenant_user() throws Exception {
        mockMvc.perform(delete("/agent/chat/session/delete").param("id", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        ArgumentCaptor<ChatActor> actorCaptor = ArgumentCaptor.forClass(ChatActor.class);
        verify(chatHistoryApplication).deleteSession(
                org.mockito.ArgumentMatchers.eq(100L),
                actorCaptor.capture()
        );
        assertThat(actorCaptor.getValue().getTenantId()).isEqualTo(10L);
        assertThat(actorCaptor.getValue().getUserId()).isEqualTo(20L);
    }
}
