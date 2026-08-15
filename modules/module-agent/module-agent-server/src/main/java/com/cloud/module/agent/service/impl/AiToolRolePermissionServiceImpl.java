package com.cloud.module.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.module.agent.entity.AiToolRolePermissionEntity;
import com.cloud.module.agent.mapper.AiToolRolePermissionMapper;
import com.cloud.module.agent.service.AiToolRolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Agent权限规则表：定义某个工具在不同调用模式下允许、拒绝或询问 服务实现类
 * </p>
 *
 * @author zhiRan
 * @since 2026-06-28
 */
@RequiredArgsConstructor
@Service
public class AiToolRolePermissionServiceImpl extends ServiceImpl<AiToolRolePermissionMapper, AiToolRolePermissionEntity> implements AiToolRolePermissionService {

    private final AiToolRolePermissionMapper toolRolePermissionMapper;
    @Override
    public List<Map<String, String>> getToolPermissionByUserId(Long userId) {
        return toolRolePermissionMapper.getToolPermissionByUserId(userId);
    }

    @Override
    public List<AiToolRolePermissionEntity> permissionListByRoleId(String toolName, Set<Long> roleIds, Long tenantId) {
        return toolRolePermissionMapper.permissionListByRoleId(toolName, roleIds, tenantId);
    }
}
