package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiAgentRunLogEntity;
import com.cloud.module.agent.service.AiAgentRunLogService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * Agent 运行表：一次用户请求对应一次 AgentScope call 或 streamEvents 执行 前端控制器
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
@RestController
@RequestMapping("/agent/agentRun")
@AllArgsConstructor
public class AiAgentRunLogController {
    private final AiAgentRunLogService aiAgentRunLogService;

    @GetMapping("/list")
    public CommonResult<List<AiAgentRunLogEntity>> list() {
        return success(aiAgentRunLogService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiAgentRunLogEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiAgentRunLogService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiAgentRunLogEntity> getById(@PathVariable Long id) {
        return success(aiAgentRunLogService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiAgentRunLogEntity entity) {
        return success(aiAgentRunLogService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiAgentRunLogEntity entity) {
        return success(aiAgentRunLogService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiAgentRunLogService.removeById(id));
    }

}
