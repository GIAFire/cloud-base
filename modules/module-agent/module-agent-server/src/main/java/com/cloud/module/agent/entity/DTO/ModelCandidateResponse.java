package com.cloud.module.agent.entity.DTO;

public record ModelCandidateResponse(
        Long id,
        String configName,
        String protocol,
        String modelName,
        Integer maxTokens,
        Integer status
) {
}
