package com.cloud.module.agent.chat.adapter.persistence;

import com.cloud.framework.tenant.core.util.TenantUtils;
import com.cloud.module.agent.chat.application.command.StartChatCommand;
import com.cloud.module.agent.chat.application.model.RunFinalization;
import com.cloud.module.agent.chat.application.model.StartedRun;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.chat.store.ChatStore;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 使用 MyBatis 持久化业务聊天状态的 ChatStore Adapter。
 *
 * <p>ChatStore 的外部接口是 Reactor 类型，而 MyBatis/JDBC 和 Spring 本地事务
 * 是同步阻塞模型。本类把同步事务调度到 boundedElastic，并在目标线程重新
 * 建立 TenantUtils 上下文；真正的事务和行锁逻辑位于
 * ChatStoreTransactions。</p>
 */
public final class MybatisChatStore implements ChatStore {

    /** 由 Spring 代理、提供同步 @Transactional 方法的内部实现。 */
    private final ChatStoreTransactions transactions;

    public MybatisChatStore(ChatStoreTransactions transactions) {
        this.transactions = Objects.requireNonNull(transactions,"transactions 不能为空");
    }

    /**
     * 在阻塞线程中恢复租户上下文，再原子创建 Session/Run/用户消息。
     */
    @Override
    public Mono<StartedRun> begin(StartChatCommand command) {
        Objects.requireNonNull(command,"command 不能为空");

        ChatActor actor = command.getActor();

        return Mono.fromCallable(() -> executeInTenant(
                actor.getTenantId(),
                () -> transactions.begin(command)))
                /*
                 * MyBatis/JDBC 是阻塞调用，不能占用模型流或 HTTP Client 线程。
                 */
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 在阻塞线程中恢复租户上下文，再提交成功、失败或取消终态。
     */
    @Override
    public Mono<Void> finish(RunFinalization finalization) {
        Objects.requireNonNull(finalization,"finalization 不能为空");

        ChatActor actor = finalization.getActor();

        return Mono.fromRunnable(() ->
                        TenantUtils.execute(
                                actor.getTenantId(),
                                () -> transactions.finish(finalization)
                        )
                ).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * 在 TenantUtils 的 Runnable 重载中取得返回值，避免 Callable 重载额外
     * 包装异常，同时确保返回值不能为 null。
     */
    private <T> T executeInTenant(Long tenantId, Supplier<T> operation) {
        AtomicReference<T> result = new AtomicReference<>();

        /*
         * 使用 Runnable 重载，避免 TenantUtils 的 Callable 重载
         * 再包装一次 RuntimeException。
         */
        TenantUtils.execute(tenantId, () -> result.set(operation.get()));

        return Objects.requireNonNull(result.get(), "Tenant operation returned null");
    }
}
