package com.cloud.module.agent.enums.config.enums;

import com.cloud.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ModelProtocolEnum implements ArrayValuable<String> {

    OPENAI("openAI"),
    DASHSCOPE("dashscope"),
    ANTHROPIC("anthropic"),
    OLLAMA("ollama");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(ModelProtocolEnum::getValue)
            .toArray(String[]::new);

    /**
     * 对应 system_dict_data.value
     */
    private final String value;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static ModelProtocolEnum valueOfCode(String value) {
        return Arrays.stream(values())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}