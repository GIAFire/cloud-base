package com.cloud.module.agent.entity.DTO;

public record SubagentMetricsResponse(
        long total,
        long enabled,
        long todayDelegations,
        Double delegationChangePercent,
        Double successRate,
        long unsuccessfulTasks,
        Double averageDurationMs
) {
}
