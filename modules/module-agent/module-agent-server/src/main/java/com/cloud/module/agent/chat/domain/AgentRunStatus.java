package com.cloud.module.agent.chat.domain;

/**
 * 一次 Agent Run 的持久化状态机。
 *
 * <p>只允许 {@code RUNNING -> SUCCEEDED|FAILED|CANCELLED}。终态不可互相转换，
 * 相同最终结果允许幂等重复提交。</p>
 */
public enum AgentRunStatus {
    /** begin 已完成，Agent 正在执行或等待收尾。 */
    RUNNING,
    /** Agent 正常完成且助手正文已可靠保存。 */
    SUCCEEDED,
    /** Agent 执行失败且安全错误信息已可靠保存。 */
    FAILED,
    /** 客户端取消订阅，运行被主动中止。 */
    CANCELLED
}
