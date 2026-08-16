package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.framework.security.core.util.SecurityFrameworkUtils;
import com.cloud.module.agent.entity.AiSubagentEntity;
import com.cloud.module.agent.entity.DTO.*;
import com.cloud.module.agent.service.AiSubagentService;
import com.cloud.module.agent.service.SubagentManagementService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * 子Agent定义表：保存可复用专家Agent的能力描述、模型、工具、知识库和安全配置 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-11
 */
@RestController
@RequestMapping("/subagent")
@AllArgsConstructor
public class AiSubagentController {
    private final AiSubagentService aiSubagentService;
    private final SubagentManagementService managementService;

    @GetMapping("/list")
    public CommonResult<List<AiSubagentEntity>> list() {
        return success(aiSubagentService.list(new LambdaQueryWrapper<AiSubagentEntity>()
                .eq(AiSubagentEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
                .eq(AiSubagentEntity::getDeleted, 0)
                .orderByDesc(AiSubagentEntity::getUpdateTime)));
    }

    @GetMapping("/page")
    public CommonResult<IPage<SubagentListItemResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Byte sourceType,
            @RequestParam(required = false) Byte enabled,
            @RequestParam(required = false) Boolean sourceAvailable
    ) {
        return success(managementService.page(
                current,
                size,
                keyword,
                sourceType,
                enabled,
                sourceAvailable
        ));
    }

    @GetMapping("/metrics")
    public CommonResult<SubagentMetricsResponse> metrics() {
        return success(managementService.metrics());
    }

    @GetMapping("/local-agent-options")
    public CommonResult<List<SubagentAgentOptionResponse>> localAgentOptions() {
        return success(managementService.localAgentOptions());
    }

    @GetMapping("/recent-tasks")
    public CommonResult<List<SubagentTaskResponse>> recentTasks(
            @RequestParam(defaultValue = "6") int limit,
            @RequestParam(defaultValue = "false") boolean exceptionsOnly
    ) {
        return success(managementService.recentTasks(limit, exceptionsOnly));
    }

    @GetMapping("/{id}")
    public CommonResult<AiSubagentEntity> getById(@PathVariable Long id) {
        return success(aiSubagentService.getOne(new LambdaQueryWrapper<AiSubagentEntity>()
                .eq(AiSubagentEntity::getTenantId, SecurityFrameworkUtils.getLoginUser().getTenantId())
                .eq(AiSubagentEntity::getId, id)
                .eq(AiSubagentEntity::getDeleted, 0)));
    }

    @PostMapping("/create")
    public CommonResult<SubagentListItemResponse> create(@RequestBody SubagentSaveRequest request) {
        try {
            return success(managementService.create(request));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return CommonResult.error(500,exception.getMessage());
        }
    }

    @PostMapping("/update")
    public CommonResult<SubagentListItemResponse> update(@RequestBody SubagentSaveRequest request) {
        try {
            return success(managementService.update(request));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return CommonResult.error(500,exception.getMessage());
        }
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        try {
            return success(managementService.delete(id));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return CommonResult.error(500,exception.getMessage());
        }
    }

}
