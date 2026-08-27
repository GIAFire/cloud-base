package com.cloud.module.agent.controller.admin.config.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.cloud.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.cloud.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 模型配置表分页 Request VO")
@Data
public class ModelPageReqVO extends PageParam {

    @Schema(description = "模型接口协议：openaiCompatible、dashscope、anthropic、ollama")
    private String protocol;

    @Schema(description = "兼容 OpenAI 协议时的自定义 baseUrl", example = "https://www.iocoder.cn")
    private String baseUrl;

    @Schema(description = "API Key；当前阶段明文存储，仅模型详情接口允许返回")
    private String apiKey;

    @Schema(description = "模型说明", example = "你猜")
    private String description;

    @Schema(description = "真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5", example = "ZR")
    private String modelName;

    @Schema(description = "是否支持流式")
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

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}