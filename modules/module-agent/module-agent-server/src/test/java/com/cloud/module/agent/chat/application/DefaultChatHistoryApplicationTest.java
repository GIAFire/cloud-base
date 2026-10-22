package com.cloud.module.agent.chat.application;

import com.cloud.framework.common.exception.ServiceException;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.store.ChatHistoryStore;
import com.cloud.module.agent.chat.store.ChatStoreException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultChatHistoryApplicationTest {

    @Test
    void delegates_session_deletion_with_actor() {
        ChatHistoryStore store = mock(ChatHistoryStore.class);
        DefaultChatHistoryApplication application =
                new DefaultChatHistoryApplication(store);
        ChatActor actor = new ChatActor(10L, 20L);

        application.deleteSession(100L, actor);

        verify(store).deleteSession(100L, actor);
    }

    @Test
    void rejects_non_positive_session_id_before_reaching_store() {
        ChatHistoryStore store = mock(ChatHistoryStore.class);
        DefaultChatHistoryApplication application =
                new DefaultChatHistoryApplication(store);

        assertThatThrownBy(() -> application.deleteSession(
                0L,
                new ChatActor(10L, 20L)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void maps_not_found_and_access_denied_to_same_public_error() {
        ChatActor actor = new ChatActor(10L, 20L);

        assertDeleteErrorMapped(
                ChatStoreException.sessionNotFound(100L),
                actor,
                1_022_001_000
        );
        assertDeleteErrorMapped(
                ChatStoreException.sessionAccessDenied(100L),
                actor,
                1_022_001_000
        );
    }

    @Test
    void maps_running_session_to_retryable_busy_error() {
        assertDeleteErrorMapped(
                ChatStoreException.sessionBusy(100L, 500L),
                new ChatActor(10L, 20L),
                1_022_001_001
        );
    }

    private void assertDeleteErrorMapped(
            ChatStoreException storeException,
            ChatActor actor,
            int expectedCode
    ) {
        ChatHistoryStore store = mock(ChatHistoryStore.class);
        doThrow(storeException).when(store).deleteSession(100L, actor);
        DefaultChatHistoryApplication application =
                new DefaultChatHistoryApplication(store);

        assertThatThrownBy(() -> application.deleteSession(100L, actor))
                .isInstanceOf(ServiceException.class)
                .satisfies(error -> org.assertj.core.api.Assertions.assertThat(
                        ((ServiceException) error).getCode()
                ).isEqualTo(expectedCode));
    }
}
