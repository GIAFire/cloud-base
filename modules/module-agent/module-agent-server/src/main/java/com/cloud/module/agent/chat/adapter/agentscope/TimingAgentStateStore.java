package com.cloud.module.agent.chat.adapter.agentscope;

import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.state.State;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.cloud.module.agent.chat.observability.ChatTiming.elapsedMillis;

/**
 * 为 AgentScope StateStore 增加耗时日志的透明装饰器。
 *
 * <p>HarnessAgent 一次运行可能读取或保存多个 state key，因此这里按真实存储
 * 操作逐条记录，而不是把整个 HarnessAgent 时间误算成 StateStore 时间。日志
 * 不包含状态正文，只输出命名空间、key、操作类型、结果和耗时。</p>
 */
@Slf4j
final class TimingAgentStateStore implements AgentStateStore {

    /** 保持原有存储行为的被装饰实现。 */
    private final AgentStateStore delegate;

    TimingAgentStateStore(AgentStateStore delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    public void save(String userId, String sessionId, String key, State value) {
        measureSave(
                "save",
                userId,
                sessionId,
                key,
                1,
                () -> delegate.save(userId, sessionId, key, value)
        );
    }

    @Override
    public void save(
            String userId,
            String sessionId,
            String key,
            List<? extends State> values
    ) {
        int itemCount = values == null ? 0 : values.size();
        measureSave(
                "saveList",
                userId,
                sessionId,
                key,
                itemCount,
                () -> delegate.save(userId, sessionId, key, values)
        );
    }

    @Override
    public <T extends State> Optional<T> get(
            String userId,
            String sessionId,
            String key,
            Class<T> type
    ) {
        return measureLoad(
                "get",
                userId,
                sessionId,
                key,
                () -> delegate.get(userId, sessionId, key, type)
        );
    }

    @Override
    public <T extends State> List<T> getList(
            String userId,
            String sessionId,
            String key,
            Class<T> itemType
    ) {
        return measureLoad(
                "getList",
                userId,
                sessionId,
                key,
                () -> delegate.getList(userId, sessionId, key, itemType)
        );
    }

    @Override
    public boolean exists(String userId, String sessionId) {
        return measureLoad(
                "exists",
                userId,
                sessionId,
                null,
                () -> delegate.exists(userId, sessionId)
        );
    }

    @Override
    public void delete(String userId, String sessionId) {
        measureDelete(
                "deleteSession",
                userId,
                sessionId,
                null,
                () -> delegate.delete(userId, sessionId)
        );
    }

    @Override
    public void delete(String userId, String sessionId, String key) {
        measureDelete(
                "deleteKey",
                userId,
                sessionId,
                key,
                () -> delegate.delete(userId, sessionId, key)
        );
    }

    @Override
    public Set<String> listSessionIds(String userId) {
        return measureLoad(
                "listSessionIds",
                userId,
                null,
                null,
                () -> delegate.listSessionIds(userId)
        );
    }

    @Override
    public void close() {
        delegate.close();
    }

    /** 记录一次有返回值的实际 StateStore 读取。 */
    private <T> T measureLoad(
            String operation,
            String userId,
            String sessionId,
            String key,
            Supplier<T> action
    ) {
        long startedNanos = System.nanoTime();
        try {
            T result = action.get();
            logStateTiming(
                    "agent.state.load.duration",
                    operation,
                    userId,
                    sessionId,
                    key,
                    null,
                    "SUCCESS",
                    startedNanos
            );
            return result;
        } catch (RuntimeException | Error error) {
            logStateTiming(
                    "agent.state.load.duration",
                    operation,
                    userId,
                    sessionId,
                    key,
                    null,
                    error.getClass().getSimpleName(),
                    startedNanos
            );
            throw error;
        }
    }

    /** 记录一次实际 StateStore 保存。 */
    private void measureSave(
            String operation,
            String userId,
            String sessionId,
            String key,
            Integer itemCount,
            Runnable action
    ) {
        measureWrite(
                "agent.state.save.duration",
                operation,
                userId,
                sessionId,
                key,
                itemCount,
                action
        );
    }

    /** 记录删除操作，便于排查会话清理造成的存储延迟。 */
    private void measureDelete(
            String operation,
            String userId,
            String sessionId,
            String key,
            Runnable action
    ) {
        measureWrite(
                "agent.state.delete.duration",
                operation,
                userId,
                sessionId,
                key,
                null,
                action
        );
    }

    /** 执行无返回值存储操作，并在成功或异常时保证只打印一次耗时。 */
    private void measureWrite(
            String metric,
            String operation,
            String userId,
            String sessionId,
            String key,
            Integer itemCount,
            Runnable action
    ) {
        long startedNanos = System.nanoTime();
        try {
            action.run();
            logStateTiming(
                    metric,
                    operation,
                    userId,
                    sessionId,
                    key,
                    itemCount,
                    "SUCCESS",
                    startedNanos
            );
        } catch (RuntimeException | Error error) {
            logStateTiming(
                    metric,
                    operation,
                    userId,
                    sessionId,
                    key,
                    itemCount,
                    error.getClass().getSimpleName(),
                    startedNanos
            );
            throw error;
        }
    }

    /** 输出统一格式的 StateStore 结构化耗时日志。 */
    private void logStateTiming(
            String metric,
            String operation,
            String userId,
            String sessionId,
            String key,
            Integer itemCount,
            String outcome,
            long startedNanos
    ) {
        log.info(
                "chat-timing metric={} durationMs={} outcome={} operation={} storeType={} "
                        + "userId={} sessionId={} key={} itemCount={}",
                metric,
                elapsedMillis(startedNanos),
                outcome,
                operation,
                delegate.getClass().getSimpleName(),
                userId,
                sessionId,
                key,
                itemCount
        );
    }
}
