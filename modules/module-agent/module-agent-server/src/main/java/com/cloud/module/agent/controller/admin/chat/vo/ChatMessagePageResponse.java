package com.cloud.module.agent.controller.admin.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/** 管理后台 - 基于 seq 游标的消息页。 */
@Schema(description = "管理后台 - 基于 seq 游标的消息页")
@Getter
@AllArgsConstructor
public final class ChatMessagePageResponse {

    private final List<ChatMessageResponse> list;
    private final String nextBeforeSeq;
    private final boolean hasMore;
}
