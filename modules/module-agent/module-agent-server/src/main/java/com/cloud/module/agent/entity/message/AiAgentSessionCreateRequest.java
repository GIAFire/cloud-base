package com.cloud.module.agent.entity.message;

import lombok.Data;

@Data
public class AiAgentSessionCreateRequest {
    private Long agentId;
    private String title;
}
