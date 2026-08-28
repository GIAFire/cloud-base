package com.cloud.module.agent.chat.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/** 基于 seq 游标的消息切片；消息按正序返回。 */
@Getter
@AllArgsConstructor
public final class ChatMessageSlice {

    private final List<StoredChatMessage> list;
    private final Long nextBeforeSeq;
    private final boolean hasMore;
}
