package com.cloud.module.agent.service;

import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;
import com.cloud.module.agent.entity.message.AgentInterventionRequest;
import com.cloud.module.agent.event.AgentStreamResponse;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * <p>
 * Agent 定义表：保存一个可视化 Agent 的基础身份信息 服务类
 * </p>
 *
 * @author
 * @since 2026-06-20
 */
public interface AgentChatService {

    Flux<ServerSentEvent<AgentStreamResponse>> chatStream(AgentConfigDTO config,
                                                          LoginUser loginUser,
                                                          Long sessionId,
                                                          String text,
                                                          Long runId);

    Flux<ServerSentEvent<AgentStreamResponse>> userConfirmStream(AgentConfigDTO config,
                                                                 LoginUser loginUser,
                                                                 Long sessionId,
                                                                 AgentInterventionRequest request);

    Flux<ServerSentEvent<AgentStreamResponse>> externalExecutionStream(AgentConfigDTO config,
                                                                       LoginUser loginUser,
                                                                       Long sessionId,
                                                                       AgentInterventionRequest request);

}
