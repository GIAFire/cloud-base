package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.module.agent.entity.AiToolRolePermissionEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Agent权限规则表：定义某个工具在不同调用模式下允许、拒绝或询问 服务类
 * </p>
 *
 * @author zhiRan
 * @since 2026-06-28
 */
public interface AiToolRolePermissionService extends IService<AiToolRolePermissionEntity> {

    List<Map<String, String>> getToolPermissionByUserId(Long userId);

    List<AiToolRolePermissionEntity> permissionListByRoleId(String toolName, Set<Long> roleIds, Long tenantId);
}
