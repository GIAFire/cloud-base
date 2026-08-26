package com.cloud.module.agent.support;

import com.cloud.module.agent.chat.runtime.AgentInvocation;
import com.cloud.module.agent.chat.runtime.AgentRuntime;
import com.cloud.module.agent.chat.runtime.RuntimeEvent;
import reactor.core.publisher.Flux;

import java.util.Objects;

public final class ScriptedAgentRuntime implements AgentRuntime {

    private final Flux<RuntimeEvent> script;

    public ScriptedAgentRuntime(Flux<RuntimeEvent> script) {
        this.script = Objects.requireNonNull(script,"script must not be null");
    }

    @Override
    public Flux<RuntimeEvent> stream(
            AgentInvocation invocation
    ) {
        return script;
    }
}