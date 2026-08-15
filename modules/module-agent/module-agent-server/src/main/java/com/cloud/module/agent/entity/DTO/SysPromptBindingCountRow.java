package com.cloud.module.agent.entity.DTO;

import lombok.Data;

@Data
public class SysPromptBindingCountRow {
    private Long id;
    private String promptName;
    private Long bindingCount;
}
