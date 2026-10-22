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

    ErrorCode AGENT_CATEGORY_NOT_EXISTS =
            new ErrorCode(1_022_002_000, "Agent 分类不存在");
    ErrorCode AGENT_CATEGORY_NAME_EXISTS =
            new ErrorCode(1_022_002_001, "Agent 分类名称【{}】已存在");
    ErrorCode AGENT_CATEGORY_DISABLED =
            new ErrorCode(1_022_002_002, "Agent 分类【{}】已禁用");
    ErrorCode AGENT_CATEGORY_IN_USE =
            new ErrorCode(1_022_002_003, "Agent 分类已被 Agent 使用，无法删除");

    ErrorCode AGENT_TAG_NOT_EXISTS =
            new ErrorCode(1_022_003_000, "Agent 标签不存在");
    ErrorCode AGENT_TAG_NAME_EXISTS =
            new ErrorCode(1_022_003_001, "Agent 标签名称【{}】已存在");
    ErrorCode AGENT_TAG_DISABLED =
            new ErrorCode(1_022_003_002, "Agent 标签【{}】已禁用");
    ErrorCode AGENT_TAG_IN_USE =
            new ErrorCode(1_022_003_003, "Agent 标签已被 Agent 使用，无法删除");

}
