package com.cloud.module.agent.chat.application;

import com.cloud.module.agent.chat.application.command.StartChatCommand;
import com.cloud.module.agent.chat.domain.ChatEvent;
import reactor.core.publisher.Flux;

public interface ChatApplication {

    Flux<ChatEvent> stream(StartChatCommand command);
}