package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiAgentStateLogEntity;
import com.cloud.module.agent.service.AiAgentStateLogService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * AgentState状态引用表：记录AgentScope运行时状态在外部存储中的位置和元数据 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-04
 */
@RestController
@RequestMapping("/agentStateLog")
@AllArgsConstructor
public class AiAgentStateLogController {
    private final AiAgentStateLogService aiAgentStateLogService;

    @GetMapping("/list")
    public CommonResult<List<AiAgentStateLogEntity>> list() {
        return success(aiAgentStateLogService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiAgentStateLogEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiAgentStateLogService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiAgentStateLogEntity> getById(@PathVariable Long id) {
        return success(aiAgentStateLogService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiAgentStateLogEntity entity) {
        return success(aiAgentStateLogService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiAgentStateLogEntity entity) {
        return success(aiAgentStateLogService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiAgentStateLogService.removeById(id));
    }

}