package com.cloud.module.agent.enums;

import com.cloud.framework.common.exception.ErrorCode;

/**
 * Agent 模块错误码枚举类
 *
 * Agent 模块使用 1-022-000-000 段
 */
public interface ErrorCodeConstants {
    ErrorCode INFO_NOT_EXISTS = new ErrorCode(500, "Agent 定义表不存在");
    ErrorCode SYS_PROMPT_NOT_EXISTS = new ErrorCode(500, "系统提示词表不存在");
    ErrorCode MODEL_NOT_EXISTS = new ErrorCode(500, "模型配置表不存在");

    ErrorCode CHAT_SESSION_NOT_FOUND_OR_DENIED =
            new ErrorCode(1_022_001_000, "会话不存在");
    ErrorCode CHAT_SESSION_BUSY =
            new ErrorCode(1_022_001_001, "会话正在运行中，暂时无法删除");

}
