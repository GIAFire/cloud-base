package com.cloud.module.agent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.framework.security.core.util.SecurityFrameworkUtils;
import com.cloud.module.agent.entity.AiToolRolePermissionEntity;
import com.cloud.module.agent.service.AiToolRolePermissionService;
import com.cloud.module.agent.service.ToolManagementService;



import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * Agent权限规则表：定义某个工具在不同调用模式下允许、拒绝或询问 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-06-28
 */
@RestController
@RequestMapping("/toolRolePermission")
@AllArgsConstructor
public class AiToolRolePermissionController {
    private final AiToolRolePermissionService aiToolRolePermissionService;
    private final ToolManagementService toolManagementService;

    @GetMapping("/list")
    public CommonResult<List<AiToolRolePermissionEntity>> list() {
        return success(aiToolRolePermissionService.list(
                new LambdaQueryWrapper<AiToolRolePermissionEntity>()
                        .eq(AiToolRolePermissionEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
                        .orderByDesc(AiToolRolePermissionEntity::getUpdateTime)));
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiToolRolePermissionEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiToolRolePermissionService.page(new Page<>(current, size),
                new LambdaQueryWrapper<AiToolRolePermissionEntity>()
                        .eq(AiToolRolePermissionEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
                        .orderByDesc(AiToolRolePermissionEntity::getUpdateTime)));
    }

    @GetMapping("/management/page")
    public CommonResult<IPage<AiToolRolePermissionEntity>> managementPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long toolId
    ) {
        return success(toolManagementService.pagePermissions(current, size, toolId));
    }

    @PostMapping("/management/save")
    public CommonResult<AiToolRolePermissionEntity> managementSave(
            @RequestBody AiToolRolePermissionEntity entity
    ) {
        try {
            return success(toolManagementService.savePermission(entity));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return CommonResult.error(500,exception.getMessage());
        }
    }

    @PostMapping("/management/{id}/disable")
    public CommonResult<Boolean> managementDisable(@PathVariable Long id) {
        return success(toolManagementService.disablePermission(id));
    }

    @GetMapping("/{id}")
    public CommonResult<AiToolRolePermissionEntity> getById(@PathVariable Long id) {
        return success(aiToolRolePermissionService.getOne(
                new LambdaQueryWrapper<AiToolRolePermissionEntity>()
                        .eq(AiToolRolePermissionEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
                        .eq(AiToolRolePermissionEntity::getId, id)));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiToolRolePermissionEntity entity) {
        return success(aiToolRolePermissionService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiToolRolePermissionEntity entity) {
        return success(aiToolRolePermissionService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiToolRolePermissionService.removeById(id));
    }

}
