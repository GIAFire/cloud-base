package com.cloud.module.agent.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiToolInfoConfigEntity;
import com.cloud.module.agent.entity.DTO.ToolMetricsResponse;
import com.cloud.module.agent.service.AiToolInfoConfigService;
import com.cloud.module.agent.service.ToolManagementService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * 全局工具配置表 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-06-27
 */
@RestController
@RequestMapping("/toolInfoConfig")
@AllArgsConstructor
public class AiToolInfoConfigController {

    private final AiToolInfoConfigService aiToolInfoConfigService;
    private final ToolManagementService toolManagementService;

    /**
     * 查询所有工具配置列表
     */
    @GetMapping("/list")
    public CommonResult<List<AiToolInfoConfigEntity>> list() {
        return success(toolManagementService.listTools());
    }

    @GetMapping("/metrics")
    public CommonResult<ToolMetricsResponse> metrics() {
        return success(toolManagementService.metrics());
    }

    /**
     * 分页查询工具配置
     */
    @GetMapping("/page")
    public CommonResult<IPage<AiToolInfoConfigEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiToolInfoConfigService.page(new Page<>(current, size)));
    }

    /**
     * 根据ID查询工具配置
     */
    @GetMapping("/{id}")
    public CommonResult<AiToolInfoConfigEntity> getById(@PathVariable Long id) {
        return success(aiToolInfoConfigService.getById(id));
    }

    /**
     * 新增工具配置
     */
    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiToolInfoConfigEntity entity) {
        return success(aiToolInfoConfigService.save(entity));
    }

    /**
     * 更新工具配置
     */
    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiToolInfoConfigEntity entity) {
        return success(aiToolInfoConfigService.updateById(entity));
    }

    /**
     * 根据ID删除工具配置
     */
    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiToolInfoConfigService.removeById(id));
    }

}
