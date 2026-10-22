package com.cloud.module.agent.controller.admin.agentInfo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - Agent 分类或标签摘要")
@Data
public class AgentTaxonomyItemRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "内容创作")
    private String name;

    @Schema(description = "状态：0 启用，1 禁用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "排序值", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer sort;

}
