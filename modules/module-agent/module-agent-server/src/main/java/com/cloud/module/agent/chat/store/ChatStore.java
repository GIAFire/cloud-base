package com.cloud.module.agent.chat.store;

import com.cloud.module.agent.chat.application.command.StartChatCommand;
import com.cloud.module.agent.chat.application.model.RunFinalization;
import com.cloud.module.agent.chat.application.model.StartedRun;
import reactor.core.publisher.Mono;

/**
 * 业务聊天状态存储 seam。
 *
 * <p>该接口只管理 Session、Run 和最终消息，不管理 HarnessAgent 的模型上下文。
 * begin/finish 都是事务语义操作，Adapter 必须保证幂等、数据归属校验和状态
 * 转换约束；应用层不感知其背后是进程内存还是 MySQL。</p>
 *
 * <p>虽然接口使用 Reactor 类型，但这不要求底层存储必须是非阻塞实现。
 * MyBatis Adapter 会把阻塞事务调度到 boundedElastic，并在目标线程恢复租户
 * 上下文。</p>
 */
public interface ChatStore {

    /**
     * 原子地开始一次聊天运行。
     *
     * 同一租户、用户和 requestId 只能创建一个 Run；同一 Session 同时只能
     * 存在一个 RUNNING Run。实现必须在返回前保存本轮用户消息。
     */
    Mono<StartedRun> begin(StartChatCommand command);

    /**
     * 原子地结束一次聊天运行。
     *
     * 只有 Session 和 Run 的所属用户可以收尾。相同结果可重复提交；已经以
     * 不同结果结束的 Run 必须拒绝。只有 SUCCEEDED 会保存 assistant 消息。
     */
    Mono<Void> finish(RunFinalization finalization);
}
