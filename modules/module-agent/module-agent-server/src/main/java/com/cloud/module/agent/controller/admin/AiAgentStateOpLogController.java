package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiAgentStateOpLogEntity;
import com.cloud.module.agent.service.AiAgentStateOpLogService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * AgentState操作日志表：记录状态加载、保存、压缩、清理等操作 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-04
 */
@RestController
@RequestMapping("/agentStateOpLog")
@AllArgsConstructor
public class AiAgentStateOpLogController {
    private final AiAgentStateOpLogService aiAgentStateOpLogService;

    @GetMapping("/list")
    public CommonResult<List<AiAgentStateOpLogEntity>> list() {
        return success(aiAgentStateOpLogService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiAgentStateOpLogEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiAgentStateOpLogService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiAgentStateOpLogEntity> getById(@PathVariable Long id) {
        return success(aiAgentStateOpLogService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiAgentStateOpLogEntity entity) {
        return success(aiAgentStateOpLogService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiAgentStateOpLogEntity entity) {
        return success(aiAgentStateOpLogService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiAgentStateOpLogService.removeById(id));
    }

}