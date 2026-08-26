package com.cloud.module.agentruntime.chat.adapter.persistence;

import com.cloud.framework.tenant.core.util.TenantUtils;
import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.application.model.RunFinalization;
import com.cloud.module.agentruntime.chat.application.model.StartedRun;
import com.cloud.module.agentruntime.chat.domain.ChatActor;
import com.cloud.module.agentruntime.chat.store.ChatStore;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class MybatisChatStore
        implements ChatStore {

    private final ChatStoreTransactions transactions;

    public MybatisChatStore(
            ChatStoreTransactions transactions
    ) {
        this.transactions = Objects.requireNonNull(
                transactions,
                "transactions must not be null"
        );
    }

    @Override
    public Mono<StartedRun> begin(
            StartChatCommand command
    ) {
        Objects.requireNonNull(
                command,
                "command must not be null"
        );

        ChatActor actor = command.getActor();

        return Mono.fromCallable(() ->
                        executeInTenant(
                                actor.getTenantId(),
                                () -> transactions.begin(command)
                        )
                )
                /*
                 * MyBatis/JDBC 是阻塞调用，不能占用模型流或 HTTP Client 线程。
                 */
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> finish(
            RunFinalization finalization
    ) {
        Objects.requireNonNull(
                finalization,
                "finalization must not be null"
        );

        ChatActor actor = finalization.getActor();

        return Mono.fromRunnable(() ->
                        TenantUtils.execute(
                                actor.getTenantId(),
                                () -> transactions.finish(finalization)
                        )
                )
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

    private <T> T executeInTenant(
            Long tenantId,
            Supplier<T> operation
    ) {
        AtomicReference<T> result =
                new AtomicReference<>();

        /*
         * 使用 Runnable 重载，避免 TenantUtils 的 Callable 重载
         * 再包装一次 RuntimeException。
         */
        TenantUtils.execute(
                tenantId,
                () -> result.set(operation.get())
        );

        return Objects.requireNonNull(
                result.get(),
                "Tenant operation returned null"
        );
    }
}