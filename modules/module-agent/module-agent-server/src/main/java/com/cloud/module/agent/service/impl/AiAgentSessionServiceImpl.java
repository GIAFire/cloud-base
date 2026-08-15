package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.entity.AiAgentSessionEntity;
import com.cloud.module.agent.mapper.AiAgentSessionMapper;
import com.cloud.module.agent.service.AiAgentSessionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * Agent 会话表：保存用户与 Agent 的一次连续对话 服务实现类
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
@Service
public class AiAgentSessionServiceImpl extends ServiceImpl<AiAgentSessionMapper, AiAgentSessionEntity> implements AiAgentSessionService {

    @Autowired
    private AiAgentSessionMapper agentSessionMapper;

    @Override
    public AiAgentSessionEntity createSession(LoginUser loginUser, Long agentId, Long agentConfigId, String title) {
        AiAgentSessionEntity sessionEntity = buildSession(loginUser, agentId, agentConfigId, title);
        agentSessionMapper.insert(sessionEntity);
        return sessionEntity;
    }

    @Override
    public AiAgentSessionEntity getOwnedSession(LoginUser loginUser, Long agentId, Long sessionId) {
        if (loginUser == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }
        if (agentId == null) {
            throw new IllegalArgumentException("agentId must not be null");
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId must not be null");
        }
        return agentSessionMapper.selectOne(new LambdaQueryWrapper<AiAgentSessionEntity>()
                .eq(AiAgentSessionEntity::getTenantId, loginUser.getTenantId())
                .eq(AiAgentSessionEntity::getUserId, loginUser.getId())
                .eq(AiAgentSessionEntity::getAgentId, agentId)
                .eq(AiAgentSessionEntity::getId, sessionId));
    }

    private AiAgentSessionEntity buildSession(LoginUser loginUser, Long agentId, Long agentConfigId, String title) {
        if (loginUser == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }
        if (agentId == null) {
            throw new IllegalArgumentException("agentId must not be null");
        }
        String normalizedTitle = normalizeTitle(title);
        AiAgentSessionEntity sessionEntity = new AiAgentSessionEntity();
        sessionEntity.setTenantId(loginUser.getTenantId());
        sessionEntity.setUserId(loginUser.getId());
        sessionEntity.setAgentId(agentId);
        sessionEntity.setAgentConfigId(agentConfigId);
        sessionEntity.setTitle(normalizedTitle);
        sessionEntity.setStatus(1);
        sessionEntity.setLastMessageAt(LocalDateTime.now());
        return sessionEntity;
    }

    private String normalizeTitle(String title) {
        String value = title == null || title.isBlank() ? "New chat" : title.trim();
        return value.length() > 15 ? value.substring(0, 15) + "..." : value;
    }
}
