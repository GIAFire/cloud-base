package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiAgentConfigEntity;
import com.cloud.module.agent.service.AiAgentConfigService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * Agent 配置表：保存每次可视化配置发布后的不可变快照 前端控制器
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
@RestController
@RequestMapping("/agentConfig")
@AllArgsConstructor
public class AiAgentConfigController {
    private final AiAgentConfigService aiAgentConfigService;

    @GetMapping("/list")
    public CommonResult<List<AiAgentConfigEntity>> list() {
        return success(aiAgentConfigService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiAgentConfigEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiAgentConfigService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiAgentConfigEntity> getById(@PathVariable Long id) {
        return success(aiAgentConfigService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiAgentConfigEntity entity) {
        return success(aiAgentConfigService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiAgentConfigEntity entity) {
        return success(aiAgentConfigService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiAgentConfigService.removeById(id));
    }

}
