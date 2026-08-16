package com.cloud.module.agent.controller.admin;

import com.cloud.framework.security.core.LoginUser;
import com.cloud.framework.security.core.util.SecurityFrameworkUtils;
import com.cloud.module.agent.config.UserContext;
import com.cloud.module.agent.entity.AiAgentMessageEntity;
import com.cloud.module.agent.entity.AiAgentRunLogEntity;
import com.cloud.module.agent.entity.AiAgentSessionEntity;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;
import com.cloud.module.agent.entity.message.AgentChatRequest;
import com.cloud.module.agent.entity.message.AgentInterventionRequest;
import com.cloud.module.agent.event.AgentStreamResponse;
import com.cloud.module.agent.service.*;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/agent/chat")
public class AgentChatController {

    private final AiAgentService agentService;
    private final AiAgentSessionService agentSessionService;
    private final AiAgentMessageService agentMessageService;
    private final AiAgentRunLogService agentRunService;
    private final AgentChatService agentChatService;


    @PostMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AgentStreamResponse>> chatStream(@RequestBody AgentChatRequest request) {
        Long requestStartNs = System.nanoTime();
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        AiAgentSessionEntity session = requireOwnedSession(loginUser, request.getAgentId(), request.getSessionId());
        AgentConfigDTO agentConfig = agentService.getAgentConfigById(
                request.getAgentId(),
                session.getAgentConfigId(),
                loginUser
        );

        AiAgentMessageEntity userMessage = agentMessageService.saveUserMessage(
                loginUser,
                session.getId(),
                request.getContent()
        );

        AiAgentRunLogEntity run = agentRunService.createRunningRun(
                loginUser,
                request.getAgentId(),
                agentConfig.getAgentConfigId(),
                session.getId(),
                userMessage.getId()
        );
        agentMessageService.bindRunId(userMessage.getId(), run.getId());

        log.warn("Controller 里同步 DB 初始化耗时, runId={}, initCostMs={}",
                run.getId(),
                (System.nanoTime() - requestStartNs) / 1_000_000
        );

        return agentChatService.chatStream(agentConfig,loginUser, session.getId(), request.getContent(),run.getId());
    }

    @PostMapping(value = "/userConfirm", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AgentStreamResponse>> userConfirm(@RequestBody AgentInterventionRequest request) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        AiAgentSessionEntity session = requireOwnedSession(
                loginUser,
                request.getAgentId(),
                request.getSessionId()
        );
        AgentConfigDTO config = agentService.getAgentConfigById(
                request.getAgentId(),
                session.getAgentConfigId(),
                loginUser
        );
        return agentChatService.userConfirmStream(config, loginUser, request.getSessionId(), request);
    }

    @PostMapping(value = "/externalExecution", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AgentStreamResponse>> externalExecution(@RequestBody AgentInterventionRequest request) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        AiAgentSessionEntity session = requireOwnedSession(
                loginUser,
                request.getAgentId(),
                request.getSessionId()
        );
        AgentConfigDTO config = agentService.getAgentConfigById(
                request.getAgentId(),
                session.getAgentConfigId(),
                loginUser
        );
        return agentChatService.externalExecutionStream(config, loginUser, request.getSessionId(), request);
    }

    private AiAgentSessionEntity requireOwnedSession(LoginUser loginUser, Long agentId, Long sessionId) {
        AiAgentSessionEntity session = agentSessionService.getOwnedSession(loginUser, agentId, sessionId);
        if (session == null) {
            throw new IllegalArgumentException("当前会话不存在,请重新建立一次对话");
        }
        return session;
    }
}
