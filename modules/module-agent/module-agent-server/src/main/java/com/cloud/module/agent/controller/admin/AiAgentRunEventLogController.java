package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiAgentRunEventLogEntity;
import com.cloud.module.agent.service.AiAgentRunEventLogService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * Agent 运行事件表：保存流式 token、工具调用、权限请求等事件 前端控制器
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
@RestController
@RequestMapping("/agentRunEvent")
@AllArgsConstructor
public class AiAgentRunEventLogController {
    private final AiAgentRunEventLogService aiAgentRunEventLogService;

    @GetMapping("/list")
    public CommonResult<List<AiAgentRunEventLogEntity>> list() {
        return success(aiAgentRunEventLogService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiAgentRunEventLogEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiAgentRunEventLogService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiAgentRunEventLogEntity> getById(@PathVariable Long id) {
        return success(aiAgentRunEventLogService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiAgentRunEventLogEntity entity) {
        return success(aiAgentRunEventLogService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiAgentRunEventLogEntity entity) {
        return success(aiAgentRunEventLogService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiAgentRunEventLogService.removeById(id));
    }

}
