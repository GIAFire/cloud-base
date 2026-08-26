package com.cloud.module.agent.chat.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 对前端公开的稳定 SSE 事件类型。
 *
 * <p>枚举名供 Java 内部使用，value 是线上协议字段；修改 value 属于协议
 * 变更，需要同步提升 ChatEvent schemaVersion。</p>
 */
@Getter
@RequiredArgsConstructor
public enum ChatEventType {

    /** Run 已创建并且运行时已开始。 */
    RUN_STARTED("run.started"),

    /** 推理过程增量；只展示，不计入最终助手正文。 */
    MESSAGE_THINKING_DELTA("message.thinking.delta"),

    /** 最终助手正文增量。 */
    MESSAGE_DELTA("message.delta"),

    /** 成功结果已经可靠写入 ChatStore。 */
    RUN_COMPLETED("run.completed"),

    /** 运行失败且 FAILED 终态已经可靠写入 ChatStore。 */
    RUN_FAILED("run.failed");

    private final String value;
}
