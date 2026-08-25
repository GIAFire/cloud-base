package com.cloud.module.agentruntime.chat.application;

import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.domain.ChatEvent;
import reactor.core.publisher.Flux;

public interface ChatApplication {

    Flux<ChatEvent> stream(StartChatCommand command);
}