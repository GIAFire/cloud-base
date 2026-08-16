package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.SysPermissionEntity;
import com.cloud.module.agent.service.SysPermissionService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * 权限表（菜单/接口） 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-06-28
 */
@RestController
@RequestMapping("/agent/sysPermission")
@AllArgsConstructor
public class SysPermissionController {
    private final SysPermissionService sysPermissionService;

    @GetMapping("/list")
    public CommonResult<List<SysPermissionEntity>> list() {
        return success(sysPermissionService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<SysPermissionEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(sysPermissionService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<SysPermissionEntity> getById(@PathVariable Long id) {
        return success(sysPermissionService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody SysPermissionEntity entity) {
        return success(sysPermissionService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody SysPermissionEntity entity) {
        return success(sysPermissionService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(sysPermissionService.removeById(id));
    }

}