package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiAgentWorkspaceFileEntity;
import com.cloud.module.agent.service.AiAgentWorkspaceFileService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * Agent工作区文件表 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-05
 */
@RestController
@RequestMapping("/agent/workspaceFile")
@AllArgsConstructor
public class AiAgentWorkspaceFileController {
    private final AiAgentWorkspaceFileService aiAgentWorkspaceFileService;

    @GetMapping("/list")
    public CommonResult<List<AiAgentWorkspaceFileEntity>> list() {
        return success(aiAgentWorkspaceFileService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiAgentWorkspaceFileEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiAgentWorkspaceFileService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiAgentWorkspaceFileEntity> getById(@PathVariable Long id) {
        return success(aiAgentWorkspaceFileService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiAgentWorkspaceFileEntity entity) {
        return success(aiAgentWorkspaceFileService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiAgentWorkspaceFileEntity entity) {
        return success(aiAgentWorkspaceFileService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiAgentWorkspaceFileService.removeById(id));
    }

}