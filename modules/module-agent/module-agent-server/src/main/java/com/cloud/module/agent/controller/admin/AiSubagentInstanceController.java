package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiSubagentInstanceEntity;
import com.cloud.module.agent.service.AiSubagentInstanceService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * 子Agent运行实例表：记录父Agent创建的子Agent实例、会话、工作区和暴露状态 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-11
 */
@RestController
@RequestMapping("/subagentInstance")
@AllArgsConstructor
public class AiSubagentInstanceController {
    private final AiSubagentInstanceService aiSubagentInstanceService;

    @GetMapping("/list")
    public CommonResult<List<AiSubagentInstanceEntity>> list() {
        return success(aiSubagentInstanceService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiSubagentInstanceEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiSubagentInstanceService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiSubagentInstanceEntity> getById(@PathVariable Long id) {
        return success(aiSubagentInstanceService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiSubagentInstanceEntity entity) {
        return success(aiSubagentInstanceService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiSubagentInstanceEntity entity) {
        return success(aiSubagentInstanceService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiSubagentInstanceService.removeById(id));
    }

}