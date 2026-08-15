package com.cloud.module.agent.entity.DTO;

public record SkillMetricsResponse(
        long total,
        long enabled,
        long todayUses,
        Double useChangePercent,
        Double successRate,
        long failedUses,
        Double averageDurationMs,
        Double averageDurationChangeMs
) {
}
