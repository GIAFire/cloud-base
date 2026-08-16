package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiAgentPlanTaskEntity;
import com.cloud.module.agent.service.AiAgentPlanTaskService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * Agent计划任务表：保存todo_write生成的结构化任务清单和执行状态 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-08
 */
@RestController
@RequestMapping("/agent/agentPlanTask")
@AllArgsConstructor
public class AiAgentPlanTaskController {
    private final AiAgentPlanTaskService aiAgentPlanTaskService;

    @GetMapping("/list")
    public CommonResult<List<AiAgentPlanTaskEntity>> list() {
        return success(aiAgentPlanTaskService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiAgentPlanTaskEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiAgentPlanTaskService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiAgentPlanTaskEntity> getById(@PathVariable Long id) {
        return success(aiAgentPlanTaskService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiAgentPlanTaskEntity entity) {
        return success(aiAgentPlanTaskService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiAgentPlanTaskEntity entity) {
        return success(aiAgentPlanTaskService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiAgentPlanTaskService.removeById(id));
    }

}