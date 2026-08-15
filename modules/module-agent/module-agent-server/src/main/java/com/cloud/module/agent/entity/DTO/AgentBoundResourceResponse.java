package com.cloud.module.agent.entity.DTO;

public record AgentBoundResourceResponse(
        Long id,
        String name,
        boolean available
) {
}
