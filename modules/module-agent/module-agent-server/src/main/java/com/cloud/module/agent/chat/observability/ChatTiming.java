package com.cloud.module.agent.chat.observability;

/**
 * 聊天链路耗时计算工具。
 *
 * <p>所有区间都使用 {@link System#nanoTime()} 的单调时钟，避免服务器校时导致
 * 耗时出现负数或跳变。日志统一输出保留三位小数的毫秒值，既能直观看出
 * 秒级模型耗时，也能分辨亚毫秒的内存和状态存储操作。</p>
 */
public final class ChatTiming {

    private ChatTiming() {
    }

    /**
     * 计算从指定单调时钟起点到现在的毫秒数，保留三位小数。
     */
    public static double elapsedMillis(long startedNanos) {
        long elapsedNanos = System.nanoTime() - startedNanos;
        return Math.round(elapsedNanos / 1_000.0d) / 1_000.0d;
    }
}
