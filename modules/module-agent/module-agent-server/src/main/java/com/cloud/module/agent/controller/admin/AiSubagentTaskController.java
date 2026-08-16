package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.framework.security.core.util.SecurityFrameworkUtils;
import com.cloud.module.agent.entity.AiSubagentTaskEntity;
import com.cloud.module.agent.service.AiSubagentTaskService;



import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * 子Agent任务表：记录agent_spawn/agent_send产生的同步或后台委派任务 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-11
 */
@RestController
@RequestMapping("/agent/subagentTask")
@AllArgsConstructor
public class AiSubagentTaskController {
    private final AiSubagentTaskService aiSubagentTaskService;

    @GetMapping("/list")
    public CommonResult<List<AiSubagentTaskEntity>> list() {
        return success(aiSubagentTaskService.list(new LambdaQueryWrapper<AiSubagentTaskEntity>()
                .eq(AiSubagentTaskEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())));
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiSubagentTaskEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiSubagentTaskService.page(
                new Page<>(current, size),
                new LambdaQueryWrapper<AiSubagentTaskEntity>()
                        .eq(AiSubagentTaskEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
        ));
    }

    @GetMapping("/{id}")
    public CommonResult<AiSubagentTaskEntity> getById(@PathVariable Long id) {
        return success(aiSubagentTaskService.getOne(new LambdaQueryWrapper<AiSubagentTaskEntity>()
                .eq(AiSubagentTaskEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
                .eq(AiSubagentTaskEntity::getId, id)));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiSubagentTaskEntity entity) {
        entity.setId(null);
        entity.setTenantId(SecurityFrameworkUtils.getLoginUser().getTenantId());
        return success(aiSubagentTaskService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiSubagentTaskEntity entity) {
        if (entity.getId() == null) {
            return success(false);
        }
        Long tenantId = SecurityFrameworkUtils.getLoginUser().getTenantId();
        entity.setTenantId(tenantId);
        return success(aiSubagentTaskService.update(
                entity,
                new LambdaQueryWrapper<AiSubagentTaskEntity>()
                        .eq(AiSubagentTaskEntity::getTenantId, tenantId)
                        .eq(AiSubagentTaskEntity::getId, entity.getId())
        ));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiSubagentTaskService.remove(new LambdaQueryWrapper<AiSubagentTaskEntity>()
                .eq(AiSubagentTaskEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
                .eq(AiSubagentTaskEntity::getId, id)));
    }

}
