package com.cloud.module.agentruntime.chat.store;

import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.application.model.PreparedRun;
import com.cloud.module.agentruntime.chat.application.model.RunFinalization;
import reactor.core.publisher.Mono;

public interface ChatStore {

    Mono<PreparedRun> begin(StartChatCommand command);

    Mono<Void> finish(RunFinalization finalization);
}