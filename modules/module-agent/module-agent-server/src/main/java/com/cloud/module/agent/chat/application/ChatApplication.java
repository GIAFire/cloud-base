package com.cloud.module.agent.chat.application;

import com.cloud.module.agent.chat.application.command.StartChatCommand;
import com.cloud.module.agent.chat.domain.ChatEvent;
import reactor.core.publisher.Flux;

/**
 * 发起流式聊天的应用层接口。
 *
 * <p>调用方只需要提交一次完整的用户命令；Session/Run 创建、Agent 调用、
 * 事件校验以及最终收尾均由实现隐藏。返回值是冷流，每次订阅代表执行一次
 * 完整聊天链路，调用方不得对同一命令重复订阅。</p>
 */
public interface ChatApplication {

    /**
     * 执行一次聊天并按严格顺序发布业务事件。
     *
     * <p>正常顺序为 {@code run.started -> message.*.delta* -> run.completed}；
     * Agent 失败且失败状态可靠落库后，以 {@code run.failed} 结束。客户端取消
     * 订阅会触发 CANCELLED 收尾。</p>
     *
     * @param command 已完成格式校验并携带租户用户身份的聊天命令
     * @return 面向 Web Adapter 的业务事件冷流
     */
    Flux<ChatEvent> stream(StartChatCommand command);
}
