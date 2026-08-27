package com.cloud.module.agent.framework.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import static org.assertj.core.api.Assertions.assertThat;

@WebAppConfiguration
@SpringJUnitConfig(classes = {
        AgentWebMvcConfiguration.class,
        AgentWebMvcConfigurationTest.WebConfiguration.class
})
class AgentWebMvcConfigurationTest {

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    @Autowired
    @Qualifier(AgentWebMvcConfiguration.SSE_TASK_EXECUTOR)
    private AsyncTaskExecutor taskExecutor;

    @Test
    void configures_bounded_executor_for_mvc_async_streaming() {
        Object configuredExecutor = ReflectionTestUtils.getField(handlerAdapter, "taskExecutor");
        Object configuredTimeout = ReflectionTestUtils.getField(handlerAdapter, "asyncRequestTimeout");

        assertThat(configuredExecutor).isSameAs(taskExecutor);
        assertThat(configuredTimeout).isEqualTo(600_000L);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    static class WebConfiguration {
    }
}
