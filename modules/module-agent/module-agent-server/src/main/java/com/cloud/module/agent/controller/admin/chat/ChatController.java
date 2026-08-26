package com.cloud.module.agent.controller.admin.chat;

import com.cloud.framework.security.core.LoginUser;
import com.cloud.framework.security.core.util.SecurityFrameworkUtils;
import com.cloud.module.agent.chat.application.ChatApplication;
import com.cloud.module.agent.chat.application.command.StartChatCommand;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.domain.ChatEvent;
import com.cloud.module.agent.controller.admin.chat.vo.ChatStreamEventResponse;
import com.cloud.module.agent.controller.admin.chat.vo.ChatStreamRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "智能体运行 - 聊天")
@RequiredArgsConstructor
@RestController
@RequestMapping("/agent/chat")
public class ChatController {

    private final ChatApplication chatApplication;

    @Operation(summary = "发起流式聊天")
    @PostMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatStreamEventResponse>> stream(@Valid @RequestBody ChatStreamRequest request) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        if (loginUser == null) {
            throw new IllegalStateException("Authenticated user is required");
        }

        ChatActor actor = new ChatActor(loginUser.getTenantId(),loginUser.getId());

        Long sessionId = request.getSessionId() == null ? null : Long.valueOf(request.getSessionId());

        StartChatCommand command = new StartChatCommand(
                        sessionId,
                        request.getRequestId(),
                        request.getContent(),
                        actor
                );

        return chatApplication.stream(command).map(this::toServerSentEvent);
    }

    private ServerSentEvent<ChatStreamEventResponse> toServerSentEvent(ChatEvent event) {

        ChatStreamEventResponse response =
                new ChatStreamEventResponse(
                    event.getSchemaVersion(),
                    String.valueOf(event.getSessionId()),
                    String.valueOf(event.getRunId()),
                    event.getSequence(),
                    event.getType().getValue(),
                    event.getDelta(),
                    event.getErrorCode(),
                    event.getErrorMessage()
                );

        return ServerSentEvent
                .<ChatStreamEventResponse>builder()
                .id(event.getRunId() + ":" + event.getSequence())
                .event(event.getType().getValue())
                .data(response)
                .build();
    }
}