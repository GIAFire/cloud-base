package com.cloud.module.member.service.auth;

public interface MemberEmailCodeService {

    void sendCode(String email, Integer scene, String createIp);

    void useCode(String email, String code, Integer scene, String usedIp);
}
