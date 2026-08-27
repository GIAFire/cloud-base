package com.cloud.module.agent.chat.runtime.definition;

import com.cloud.module.agent.chat.domain.ChatActor;
import reactor.core.publisher.Mono;

/** 根据当前租户和 Agent ID 加载单次运行所需定义的 seam。 */
public interface AgentDefinitionLoader {

    Mono<AgentDefinition> load(Long agentId, ChatActor actor);
}
