package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiSubagentAgentBindingEntity;
import com.cloud.module.agent.service.AiSubagentAgentBindingService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * 主Agent与子Agent绑定表：定义某个主Agent版本可以委派哪些子Agent及调用策略 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-11
 */
@RestController
@RequestMapping("/subagentAgentBinding")
@AllArgsConstructor
public class AiSubagentAgentBindingController {
    private final AiSubagentAgentBindingService aiSubagentAgentBindingService;

    @GetMapping("/list")
    public CommonResult<List<AiSubagentAgentBindingEntity>> list() {
        return success(aiSubagentAgentBindingService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiSubagentAgentBindingEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiSubagentAgentBindingService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiSubagentAgentBindingEntity> getById(@PathVariable Long id) {
        return success(aiSubagentAgentBindingService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiSubagentAgentBindingEntity entity) {
        return success(aiSubagentAgentBindingService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiSubagentAgentBindingEntity entity) {
        return success(aiSubagentAgentBindingService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiSubagentAgentBindingService.removeById(id));
    }

}