package com.cloud.module.agent.chat.runtime;

import reactor.core.publisher.Flux;

public interface AgentRuntime {

    Flux<RuntimeEvent> stream(AgentInvocation invocation);
}