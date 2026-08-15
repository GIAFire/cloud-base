package com.cloud.module.agent.entity.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record SkillListItemResponse(
        Long id,
        String skillCode,
        String skillName,
        String description,
        String category,
        List<String> tags,
        String riskLevel,
        Byte status,
        List<String> roleIds,
        long todayUses,
        long boundAgents,
        Double successRate,
        boolean hasScripts,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updateTime
) {
}
