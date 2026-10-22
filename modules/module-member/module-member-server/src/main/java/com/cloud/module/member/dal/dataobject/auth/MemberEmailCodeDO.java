package com.cloud.module.member.dal.dataobject.auth;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.framework.tenant.core.db.TenantBaseDO;
import lombok.*;

import java.time.LocalDateTime;

@TableName("member_email_code")
@KeySequence("member_email_code_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEmailCodeDO extends TenantBaseDO {

    private Long id;
    private String email;
    private String codeHash;
    private Integer scene;
    private String createIp;
    private LocalDateTime expireTime;
    private Integer attemptCount;
    private Boolean used;
    private LocalDateTime usedTime;
    private String usedIp;
}
