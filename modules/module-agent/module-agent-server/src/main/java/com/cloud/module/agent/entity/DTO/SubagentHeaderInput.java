package com.cloud.module.agent.entity.DTO;

import lombok.Data;

@Data
public class SubagentHeaderInput {
    private Long id;
    private String headerName;
    private String headerValue;
    private Boolean remove;
}
