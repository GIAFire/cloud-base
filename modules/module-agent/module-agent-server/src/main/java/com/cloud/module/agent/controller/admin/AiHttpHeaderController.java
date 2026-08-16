package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.framework.security.core.util.SecurityFrameworkUtils;
import com.cloud.module.agent.config.UserContext;
import com.cloud.module.agent.entity.AiHttpHeaderEntity;
import com.cloud.module.agent.service.AiHttpHeaderService;



import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * HTTP请求头配置表 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-26
 */
@AllArgsConstructor
@RestController
@RequestMapping("/agent/httpHeader")
public class AiHttpHeaderController {

    private final AiHttpHeaderService httpHeaderService;

    @GetMapping("/list")
    public CommonResult<List<AiHttpHeaderEntity>> list() {
        return success(httpHeaderService.list(new LambdaQueryWrapper<AiHttpHeaderEntity>()
                .eq(AiHttpHeaderEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())));
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiHttpHeaderEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(httpHeaderService.page(new Page<>(current, size),
                new LambdaQueryWrapper<AiHttpHeaderEntity>()
                        .eq(AiHttpHeaderEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())));
    }

    @GetMapping("/{id}")
    public CommonResult<AiHttpHeaderEntity> getById(@PathVariable Long id) {
        return success(httpHeaderService.getOne(new LambdaQueryWrapper<AiHttpHeaderEntity>()
                .eq(AiHttpHeaderEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
                .eq(AiHttpHeaderEntity::getId, id)));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiHttpHeaderEntity entity) {
        entity.setId(null);
        entity.setTenantId(SecurityFrameworkUtils.getLoginUser().getTenantId());
        return success(httpHeaderService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiHttpHeaderEntity entity) {
        if (entity.getId() == null) {
            return success(false);
        }
        Long tenantId = SecurityFrameworkUtils.getLoginUser().getTenantId();
        entity.setTenantId(tenantId);
        return success(httpHeaderService.update(
                entity,
                new LambdaQueryWrapper<AiHttpHeaderEntity>()
                        .eq(AiHttpHeaderEntity::getTenantId, tenantId)
                        .eq(AiHttpHeaderEntity::getId, entity.getId())
        ));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(httpHeaderService.remove(new LambdaQueryWrapper<AiHttpHeaderEntity>()
                .eq(AiHttpHeaderEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
                .eq(AiHttpHeaderEntity::getId, id)));
    }


}
