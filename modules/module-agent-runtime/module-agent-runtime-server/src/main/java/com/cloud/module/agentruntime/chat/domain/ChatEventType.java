package com.cloud.module.agentruntime.chat.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatEventType {

    RUN_STARTED("run.started"),
    MESSAGE_DELTA("message.delta"),
    RUN_COMPLETED("run.completed"),
    RUN_FAILED("run.failed");

    private final String value;
}
