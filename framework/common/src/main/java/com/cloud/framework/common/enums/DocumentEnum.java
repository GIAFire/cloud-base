package com.cloud.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档地址
 *
 * @author Cloud
 */
@Getter
@AllArgsConstructor
public enum DocumentEnum {

    REDIS_INSTALL("https://www.baidu.com", "Redis 安装文档");

    private final String url;
    private final String memo;

}
