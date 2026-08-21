package com.cloud.module.AiFriend.enums;

import com.cloud.framework.common.exception.ErrorCode;

/**
 * Infra 错误码枚举类
 *
 * infra 系统，使用 1-001-000-000 段
 */
public interface ErrorCodeConstants {
    ErrorCode AGENT_INFO_NOT_EXISTS = new ErrorCode(500, "Agent定义表 不存在");
    ErrorCode SESSION_NOT_EXISTS = new ErrorCode(500, "Agent会话表 不存在");

}
