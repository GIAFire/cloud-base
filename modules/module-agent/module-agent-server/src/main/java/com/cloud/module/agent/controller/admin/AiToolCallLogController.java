package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.framework.security.core.util.SecurityFrameworkUtils;
import com.cloud.module.agent.entity.AiToolCallLogEntity;
import com.cloud.module.agent.entity.DTO.ToolCallLogResponse;
import com.cloud.module.agent.service.AiToolCallLogService;
import com.cloud.module.agent.service.ToolManagementService;



import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * 工具调用审计表：记录Agent每一次工具调用的权限结果、参数、结果和耗时 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-06-28
 */
@RestController
@RequestMapping("/toolCallLog")
@AllArgsConstructor
public class AiToolCallLogController {
    private final AiToolCallLogService aiToolCallLogService;
    private final ToolManagementService toolManagementService;

    @GetMapping("/list")
    public CommonResult<List<AiToolCallLogEntity>> list() {
        return success(aiToolCallLogService.list(new LambdaQueryWrapper<AiToolCallLogEntity>()
                .eq(AiToolCallLogEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
                .orderByDesc(AiToolCallLogEntity::getStartedAt)));
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiToolCallLogEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiToolCallLogService.page(new Page<>(current, size),
                new LambdaQueryWrapper<AiToolCallLogEntity>()
                        .eq(AiToolCallLogEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
                        .orderByDesc(AiToolCallLogEntity::getStartedAt)));
    }

    @GetMapping("/management/page")
    public CommonResult<IPage<ToolCallLogResponse>> managementPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long toolId,
            @RequestParam(required = false) String successStatus
    ) {
        return success(toolManagementService.pageCallLogs(current, size, toolId, successStatus));
    }

    @GetMapping("/{id}")
    public CommonResult<AiToolCallLogEntity> getById(@PathVariable Long id) {
        return success(aiToolCallLogService.getOne(new LambdaQueryWrapper<AiToolCallLogEntity>()
                .eq(AiToolCallLogEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
                .eq(AiToolCallLogEntity::getId, id)));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiToolCallLogEntity entity) {
        return success(aiToolCallLogService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiToolCallLogEntity entity) {
        return success(aiToolCallLogService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiToolCallLogService.removeById(id));
    }

}
