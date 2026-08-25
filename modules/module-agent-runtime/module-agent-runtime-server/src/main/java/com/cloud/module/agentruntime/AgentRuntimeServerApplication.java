package com.cloud.module.agentruntime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 项目的启动类
 * @author Cloud
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AgentRuntimeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentRuntimeServerApplication.class, args);
    }

}
