package com.cloud.module.member.service.auth;

import cn.hutool.core.map.MapUtil;
import com.cloud.module.member.dal.dataobject.auth.MemberEmailCodeDO;
import com.cloud.module.member.dal.mysql.auth.MemberEmailCodeMapper;
import com.cloud.module.member.enums.EmailCodeSceneEnum;
import com.cloud.module.system.api.mail.MailSendApi;
import com.cloud.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static com.cloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloud.module.member.enums.ErrorCodeConstants.*;

@Service
public class MemberEmailCodeServiceImpl implements MemberEmailCodeService {

    private static final Duration EXPIRE_TIME = Duration.ofMinutes(10);
    private static final Duration SEND_FREQUENCY = Duration.ofSeconds(60);
    private static final int HOURLY_LIMIT = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Resource
    private MemberEmailCodeMapper emailCodeMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private MailSendApi mailSendApi;
    @Resource
    private PlatformTransactionManager transactionManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendCode(String email, Integer scene, String createIp) {
        email = email.trim().toLowerCase(java.util.Locale.ROOT);
        EmailCodeSceneEnum sceneEnum = EmailCodeSceneEnum.valueOfScene(scene);
        if (sceneEnum == null) {
            throw exception(AUTH_EMAIL_CODE_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        MemberEmailCodeDO last = emailCodeMapper.selectLastByEmail(email);
        if (last != null && last.getCreateTime().plus(SEND_FREQUENCY).isAfter(now)) {
            throw exception(AUTH_EMAIL_CODE_SEND_TOO_FAST);
        }
        if (emailCodeMapper.selectCountSince(email, now.minusHours(1)) >= HOURLY_LIMIT) {
            throw exception(AUTH_EMAIL_CODE_SEND_LIMIT);
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        MemberEmailCodeDO emailCode = MemberEmailCodeDO.builder()
                .email(email).codeHash(passwordEncoder.encode(code)).scene(scene)
                .createIp(createIp).expireTime(now.plus(EXPIRE_TIME))
                .attemptCount(0).used(false).build();
        emailCode.setCreator(email);
        emailCodeMapper.insert(emailCode);

        MailSendSingleToUserReqDTO mailReq = new MailSendSingleToUserReqDTO();
        mailReq.setToMails(List.of(email));
        mailReq.setTemplateCode(sceneEnum.getTemplateCode());
        mailReq.setTemplateParams(MapUtil.of("code", code));
        // TODO 生产环境需要开启
//        mailSendApi.sendSingleMailToMember(mailReq).checkError();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCode(String email, String code, Integer scene, String usedIp) {
        email = email.trim().toLowerCase(java.util.Locale.ROOT);
        MemberEmailCodeDO emailCode = emailCodeMapper.selectLast(email, scene);
        if (emailCode == null || Boolean.TRUE.equals(emailCode.getUsed())
                || !emailCode.getExpireTime().isAfter(LocalDateTime.now())) {
            throw exception(AUTH_EMAIL_CODE_INVALID);
        }
        if (emailCode.getAttemptCount() >= MAX_ATTEMPTS) {
            throw exception(AUTH_EMAIL_CODE_ATTEMPTS_EXCEEDED);
        }
        if (!passwordEncoder.matches(code, emailCode.getCodeHash())) {
            int attempts = emailCode.getAttemptCount() + 1;
            persistFailedAttempt(emailCode.getId(), attempts);
            if (attempts >= MAX_ATTEMPTS) {
                throw exception(AUTH_EMAIL_CODE_ATTEMPTS_EXCEEDED);
            }
            throw exception(AUTH_EMAIL_CODE_INVALID);
        }
        emailCodeMapper.updateById(MemberEmailCodeDO.builder().id(emailCode.getId())
                .used(true).usedTime(LocalDateTime.now()).usedIp(usedIp).build());
    }

    private void persistFailedAttempt(Long id, int attempts) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.executeWithoutResult(status -> emailCodeMapper.updateById(MemberEmailCodeDO.builder().id(id)
                .attemptCount(attempts).used(attempts >= MAX_ATTEMPTS).build()));
    }
}
