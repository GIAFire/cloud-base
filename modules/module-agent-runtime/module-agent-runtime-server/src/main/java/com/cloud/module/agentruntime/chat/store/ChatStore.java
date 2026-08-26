package com.cloud.module.agentruntime.chat.store;

import com.cloud.module.agentruntime.chat.application.command.StartChatCommand;
import com.cloud.module.agentruntime.chat.application.model.RunFinalization;
import com.cloud.module.agentruntime.chat.application.model.StartedRun;
import reactor.core.publisher.Mono;

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
