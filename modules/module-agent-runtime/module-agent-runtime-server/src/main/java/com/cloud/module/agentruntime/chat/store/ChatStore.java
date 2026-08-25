package com.cloud.module.agentruntime.chat.store;

import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.application.model.RunFinalization;
import com.cloud.module.agentruntime.chat.application.model.StartedRun;
import reactor.core.publisher.Mono;

public interface ChatStore {

    Mono<StartedRun> begin(StartChatCommand command);

    Mono<Void> finish(RunFinalization finalization);
}
