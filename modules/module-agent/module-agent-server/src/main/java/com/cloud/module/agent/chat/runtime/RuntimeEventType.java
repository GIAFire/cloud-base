package com.cloud.module.agent.chat.runtime;

/** AgentRuntime 向应用层公开的最小事件集合。 */
public enum RuntimeEventType {
    /** Agent 已接受本次调用。 */
    STARTED,
    /** 推理过程文本增量，不进入最终助手正文。 */
    THINKING_DELTA,
    /** 最终助手正文增量。 */
    TEXT_DELTA,
    /** Agent 框架确认正常结束。 */
    COMPLETED
}
