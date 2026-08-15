package com.cloud.module.agent.entity.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RunOverviewRecentInteractionRow {

    private Long runId;
    private Long sessionId;
    private Long agentId;
    private String agentCode;
    private String agentName;
    private String userMessage;
    private String assistantMessage;
    private String status;
    private LocalDateTime startedAt;
}
