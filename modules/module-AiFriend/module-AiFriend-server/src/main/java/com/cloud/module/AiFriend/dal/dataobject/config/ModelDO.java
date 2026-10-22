package com.cloud.module.AiFriend.dal.dataobject.config;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.cloud.framework.mybatis.core.dataobject.BaseDO;

/**
 * 模型配置表 DO
 *
 * @author ZR
 */
@TableName("agent_model")
@KeySequence("agent_model_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelDO extends BaseDO {

    /**
     * 模型配置主键ID
     */
    @TableId
    private Long id;
    /**
     * 模型接口协议：openaiCompatible、dashscope、anthropic、ollama
     *
     */
    private String protocol;
    /**
     * 兼容 OpenAI 协议时的自定义 baseUrl
     */
    private String baseUrl;
    /**
     * API Key；当前阶段明文存储，仅模型详情接口允许返回
     */
    private String apiKey;
    /**
     * 模型说明
     */
    private String description;
    /**
     * 真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5
     */
    private String modelName;
    /**
     * 是否支持流式
     *
     */
    private Boolean streaming;
    /**
     * 是否支持思考
     *
     */
    private Boolean thinking;
    /**
     * 采样温度，控制随机性
     */
    private BigDecimal temperature;
    /**
     * 核采样参数
     */
    private BigDecimal topP;
    /**
     * 最大输出token
     */
    private Integer maxTokens;
    /**
     * 最大思考token
     */
    private Integer thinkingBudget;

}
