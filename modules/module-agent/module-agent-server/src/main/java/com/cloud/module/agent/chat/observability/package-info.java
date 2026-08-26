/**
 * Agent 聊天链路的轻量可观测性支持。
 *
 * <p>当前只提供结构化耗时日志，不引入指标注册中心或链路追踪依赖。后续接入
 * Micrometer/OpenTelemetry 时，可以复用同一组 metric 名称和起止点。</p>
 */
package com.cloud.module.agent.chat.observability;
