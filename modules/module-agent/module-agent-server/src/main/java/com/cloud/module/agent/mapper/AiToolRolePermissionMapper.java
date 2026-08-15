package com.cloud.module.agent.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.module.agent.entity.AiToolRolePermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Agent权限规则表：定义某个工具在不同调用模式下允许、拒绝或询问 Mapper 接口
 * </p>
 *
 * @author zhiRan
 * @since 2026-06-28
 */
@Mapper
public interface AiToolRolePermissionMapper extends BaseMapper<AiToolRolePermissionEntity> {

    List<Map<String, String>> getToolPermissionByUserId(Long userId);

    @InterceptorIgnore(tenantLine = "true")
    List<AiToolRolePermissionEntity> permissionListByRoleId(@Param("toolName") String toolName,
                                                            @Param("roleIds") Set<Long> roleIds,
                                                            @Param("tenantId") Long tenantId);
}
