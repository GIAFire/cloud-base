package com.cloud.module.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailCodeSceneEnum {

    REGISTER(1, "member-email-register-code"),
    RESET_PASSWORD(2, "member-email-reset-password-code");

    private final Integer scene;
    private final String templateCode;

    public static EmailCodeSceneEnum valueOfScene(Integer scene) {
        for (EmailCodeSceneEnum value : values()) {
            if (value.scene.equals(scene)) {
                return value;
            }
        }
        return null;
    }
}
