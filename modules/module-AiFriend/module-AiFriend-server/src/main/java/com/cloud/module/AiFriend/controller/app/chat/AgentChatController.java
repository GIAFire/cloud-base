package com.cloud.module.AiFriend.controller.app.chat;

import com.cloud.framework.security.core.LoginUser;
import com.cloud.framework.security.core.util.SecurityFrameworkUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "智能体 - 聊天")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/agent/agent/chat")
public class AgentChatController {

//    @Operation(summary = "发起聊天")
//    @PostMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ServerSentEvent<AgentStreamResponse>> chatStream(@RequestBody AgentChatRequest request) {
//
//        return agentChatService.chatStream(agentConfig,loginUser, session.getId(), request.getContent(),run.getId());
//    }
}
