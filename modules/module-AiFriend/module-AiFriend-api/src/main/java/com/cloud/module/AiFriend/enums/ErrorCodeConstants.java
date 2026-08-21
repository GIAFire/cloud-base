package com.cloud.module.AiFriend.enums;

import com.cloud.framework.common.exception.ErrorCode;

/**
 * Infra 错误码枚举类
 *
 * infra 系统，使用 1-001-000-000 段
 */
public interface ErrorCodeConstants {
    ErrorCode SESSION_NOT_EXISTS = new ErrorCode(500, "Agent会话表：保存用户和Agent之间的一段连续对话不存在");

}
