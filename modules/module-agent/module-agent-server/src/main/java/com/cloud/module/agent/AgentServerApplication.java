package com.cloud.module.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Agent Server 进程入口。
 *
 * <p>扫描 {@code com.cloud.module.agent} 下的 Web Adapter、聊天应用层、
 * AgentScope Adapter、ChatStore Adapter 以及配置属性。具体运行时和存储实现
 * 由 {@code agent.runtime.type}、{@code agent.runtime.store.type} 条件装配。</p>
 *
 * @author Cloud
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AgentServerApplication {

    public static void main(String[] args) {
        // 由 Spring 管理 HarnessAgent、数据库事务和 SSE 线程池的完整生命周期。
        SpringApplication.run(AgentServerApplication.class, args);
    }

}
