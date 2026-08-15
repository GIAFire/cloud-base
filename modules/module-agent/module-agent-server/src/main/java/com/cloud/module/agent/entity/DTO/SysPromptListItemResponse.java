package com.cloud.module.agent.entity.DTO;

import java.time.LocalDateTime;

public record SysPromptListItemResponse(
        Long id,
        String promptName,
        String description,
        String contentPreview,
        long contentLength,
        long bindingCount,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
