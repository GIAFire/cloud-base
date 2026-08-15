package com.cloud.module.agent.entity.DTO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubagentSaveRequest {
    private Long id;
    private String subagentCode;
    private String subagentName;
    private String description;
    private Byte sourceType;
    private Long localAgentId;
    private String remoteUrl;
    private Byte protocolType;
    private Byte enabled;
    private String remark;
    private List<SubagentHeaderInput> headers = new ArrayList<>();
}
