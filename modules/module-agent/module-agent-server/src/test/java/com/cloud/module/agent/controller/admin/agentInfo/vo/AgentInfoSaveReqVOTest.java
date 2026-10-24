package com.cloud.module.agent.controller.admin.agentInfo.vo;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class AgentInfoSaveReqVOTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"CHAT", "CREATE_COSPLAYER"})
    void accepts_supported_or_omitted_kind(String kind) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            AgentInfoSaveReqVO request = new AgentInfoSaveReqVO();
            request.setAgentKind(kind);
            assertThat(factory.getValidator().validateProperty(request, "agentKind")).isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "UNKNOWN", "chat"})
    void rejects_unknown_kind(String kind) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            AgentInfoSaveReqVO request = new AgentInfoSaveReqVO();
            request.setAgentKind(kind);
            assertThat(factory.getValidator().validateProperty(request, "agentKind")).hasSize(1);
        }
    }
}
