package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiKnowledgeAgentBindingEntity;
import com.cloud.module.agent.service.AiKnowledgeAgentBindingService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * Agent知识库绑定表：控制Agent配置可访问的知识库及检索参数 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-11
 */
@RestController
@RequestMapping("/agent/knowledgeAgentBinding")
@AllArgsConstructor
public class AiKnowledgeAgentBindingController {
    private final AiKnowledgeAgentBindingService aiKnowledgeAgentBindingService;

    @GetMapping("/list")
    public CommonResult<List<AiKnowledgeAgentBindingEntity>> list() {
        return success(aiKnowledgeAgentBindingService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiKnowledgeAgentBindingEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiKnowledgeAgentBindingService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiKnowledgeAgentBindingEntity> getById(@PathVariable Long id) {
        return success(aiKnowledgeAgentBindingService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiKnowledgeAgentBindingEntity entity) {
        return success(aiKnowledgeAgentBindingService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiKnowledgeAgentBindingEntity entity) {
        return success(aiKnowledgeAgentBindingService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiKnowledgeAgentBindingService.removeById(id));
    }

}