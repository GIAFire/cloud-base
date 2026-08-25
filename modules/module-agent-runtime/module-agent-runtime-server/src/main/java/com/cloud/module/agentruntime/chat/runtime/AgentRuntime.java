package com.cloud.module.agentruntime.chat.runtime;

import reactor.core.publisher.Flux;

public interface AgentRuntime {

    Flux<RuntimeEvent> stream(AgentInvocation invocation);
}