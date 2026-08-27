package com.cloud.module.agent.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 模型配置表新增/修改 Request VO")
@Data
public class ModelSaveReqVO {

    @Schema(description = "模型配置主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25462")
    private Long id;

    @Schema(description = "模型接口协议：openaiCompatible、dashscope、anthropic、ollama", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "模型接口协议：openaiCompatible、dashscope、anthropic、ollama不能为空")
    private String protocol;

    @Schema(description = "兼容 OpenAI 协议时的自定义 baseUrl", example = "https://www.iocoder.cn")
    private String baseUrl;

    @Schema(description = "API Key；当前阶段明文存储，仅模型详情接口允许返回")
    private String apiKey;

    @Schema(description = "模型说明", example = "你猜")
    private String description;

    @Schema(description = "真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5", requiredMode = Schema.RequiredMode.REQUIRED, example = "ZR")
    @NotEmpty(message = "真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5不能为空")
    private String modelName;

    @Schema(description = "是否支持流式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否支持流式不能为空")
    private Boolean streaming;

    @Schema(description = "是否支持思考")
    private Boolean thinking;

    @Schema(description = "采样温度，控制随机性")
    private BigDecimal temperature;

    @Schema(description = "核采样参数")
    private BigDecimal topP;

    @Schema(description = "最大输出token")
    private Integer maxTokens;

    @Schema(description = "最大思考token")
    private Integer thinkingBudget;

}