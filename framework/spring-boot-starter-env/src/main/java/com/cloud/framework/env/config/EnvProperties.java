package com.cloud.framework.env.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 环境配置
 *
 * @author Cloud
 */
@ConfigurationProperties(prefix = "com.cloud.env")
@Data
public class EnvProperties {

    public static final String TAG_KEY = "com.cloud.env.tag";

    /**
     * 环境标签
     */
    private String tag;

}
