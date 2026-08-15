package com.cloud.module.agent.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiAgentPlanOpLogEntity;
import com.cloud.module.agent.service.AiAgentPlanOpLogService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * Agent计划操作日志表：记录plan_enter、plan_write、plan_exit、todo_write等计划相关事件 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-08
 */
@RestController
@RequestMapping("/agentPlanOpLog")
@AllArgsConstructor
public class AiAgentPlanOpLogController {
    private final AiAgentPlanOpLogService aiAgentPlanOpLogService;

    @GetMapping("/list")
    public CommonResult<List<AiAgentPlanOpLogEntity>> list() {
        return success(aiAgentPlanOpLogService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiAgentPlanOpLogEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiAgentPlanOpLogService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiAgentPlanOpLogEntity> getById(@PathVariable Long id) {
        return success(aiAgentPlanOpLogService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiAgentPlanOpLogEntity entity) {
        return success(aiAgentPlanOpLogService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiAgentPlanOpLogEntity entity) {
        return success(aiAgentPlanOpLogService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiAgentPlanOpLogService.removeById(id));
    }

}