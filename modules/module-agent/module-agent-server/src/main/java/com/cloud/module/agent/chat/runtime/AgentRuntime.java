package com.cloud.module.agent.chat.runtime;

import reactor.core.publisher.Flux;

/**
 * Agent 框架运行时 seam。
 *
 * <p>实现负责把第三方框架事件翻译为 RuntimeEvent，不得向应用层暴露
 * AgentScope 类型。一个合法事件流必须满足：</p>
 *
 * <ol>
 *     <li>恰好一个 STARTED；</li>
 *     <li>零个或多个 THINKING_DELTA/TEXT_DELTA；</li>
 *     <li>正常完成前恰好一个 COMPLETED。</li>
 * </ol>
 *
 * <p>模型异常通过 Reactor error 传播，不能伪装成 COMPLETED；取消必须继续
 * 向下游传播，以便停止模型生成。</p>
 */
public interface AgentRuntime {

    /**
     * 启动一次 Agent 调用并返回冷事件流。
     *
     * @param invocation 已校验的 Session、Run、身份和当前输入
     * @return 满足运行时事件协议的冷流
     */
    Flux<RuntimeEvent> stream(AgentInvocation invocation);
}
