package com.cloud.module.agent.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiAgentPlanEntity;
import com.cloud.module.agent.service.AiAgentPlanService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * Agent计划表：保存Plan Mode生成的计划元数据、内容快照、审批和执行状态 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-08
 */
@RestController
@RequestMapping("/agentPlan")
@AllArgsConstructor
public class AiAgentPlanController {
    private final AiAgentPlanService aiAgentPlanService;

    @GetMapping("/list")
    public CommonResult<List<AiAgentPlanEntity>> list() {
        return success(aiAgentPlanService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiAgentPlanEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiAgentPlanService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiAgentPlanEntity> getById(@PathVariable Long id) {
        return success(aiAgentPlanService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiAgentPlanEntity entity) {
        return success(aiAgentPlanService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiAgentPlanEntity entity) {
        return success(aiAgentPlanService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiAgentPlanService.removeById(id));
    }

}