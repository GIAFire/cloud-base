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

/**
 * Agent 聊天的 HTTP/SSE 入站 Adapter。
 *
 * <p>Controller 只负责把认证身份和请求 DTO 转换为 StartChatCommand，再把
 * ChatEvent 转换为标准 SSE。运行编排、Agent 框架调用和事务规则全部留在
 * 下层模块。</p>
 */
@Tag(name = "智能体运行 - 聊天")
@RequiredArgsConstructor
@RestController
@RequestMapping("/agent/chat")
public class ChatController {

    /** 聊天应用层接口。 */
    private final ChatApplication chatApplication;

    /**
     * 发起一次流式聊天。
     *
     * <p>第一次对话可不传 sessionId；响应使用 {@code text/event-stream}，
     * 每个 ChatEvent 对应一个具名 SSE。客户端断开连接会沿 Flux 传播取消信号。</p>
     */
    @Operation(summary = "发起流式聊天")
    @PostMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatStreamEventResponse>> stream(@Valid @RequestBody ChatStreamRequest request) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        if (loginUser == null) {
            throw new IllegalStateException("用户未登录");
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

    /**
     * 将业务事件转换为传输 DTO，并以 {@code runId:sequence} 生成稳定 SSE id。
     */
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
