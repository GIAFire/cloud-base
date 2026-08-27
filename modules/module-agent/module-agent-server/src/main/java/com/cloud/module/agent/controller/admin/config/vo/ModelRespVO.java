package com.cloud.module.agent.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 模型配置表 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ModelRespVO {

    @Schema(description = "模型配置主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25462")
    @ExcelProperty("模型配置主键ID")
    private Long id;

    @Schema(description = "模型接口协议：openaiCompatible、dashscope、anthropic、ollama", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("模型接口协议：openaiCompatible、dashscope、anthropic、ollama")
    private String protocol;

    @Schema(description = "兼容 OpenAI 协议时的自定义 baseUrl", example = "https://www.iocoder.cn")
    @ExcelProperty("兼容 OpenAI 协议时的自定义 baseUrl")
    private String baseUrl;

    @Schema(description = "API Key；当前阶段明文存储，仅模型详情接口允许返回")
    @ExcelProperty("API Key；当前阶段明文存储，仅模型详情接口允许返回")
    private String apiKey;

    @Schema(description = "模型说明", example = "你猜")
    @ExcelProperty("模型说明")
    private String description;

    @Schema(description = "真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5", requiredMode = Schema.RequiredMode.REQUIRED, example = "ZR")
    @ExcelProperty("真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5")
    private String modelName;

    @Schema(description = "是否支持流式", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否支持流式")
    private Boolean streaming;

    @Schema(description = "是否支持思考")
    @ExcelProperty("是否支持思考")
    private Boolean thinking;

    @Schema(description = "采样温度，控制随机性")
    @ExcelProperty("采样温度，控制随机性")
    private BigDecimal temperature;

    @Schema(description = "核采样参数")
    @ExcelProperty("核采样参数")
    private BigDecimal topP;

    @Schema(description = "最大输出token")
    @ExcelProperty("最大输出token")
    private Integer maxTokens;

    @Schema(description = "最大思考token")
    @ExcelProperty("最大思考token")
    private Integer thinkingBudget;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}