package com.cloud.module.agent.enums;

import com.cloud.framework.common.exception.ErrorCode;

/**
 * Infra 错误码枚举类
 *
 * infra 系统，使用 1-001-000-000 段
 */
public interface ErrorCodeConstants {
    ErrorCode INFO_NOT_EXISTS = new ErrorCode(500, "Agent 定义表不存在");
    ErrorCode SYS_PROMPT_NOT_EXISTS = new ErrorCode(500, "系统提示词表不存在");
    ErrorCode MODEL_NOT_EXISTS = new ErrorCode(500, "模型配置表不存在");

}
