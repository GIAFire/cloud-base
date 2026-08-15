package com.cloud.module.agent.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiKnowledgeRetrievalHitEntity;
import com.cloud.module.agent.service.AiKnowledgeRetrievalHitService;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * 知识库检索命中明细表：记录每次检索命中的文档切片、分数和引用信息 前端控制器
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-06
 */
@RestController
@RequestMapping("/knowledgeRetrievalHit")
@AllArgsConstructor
public class AiKnowledgeRetrievalHitController {
    private final AiKnowledgeRetrievalHitService aiKnowledgeRetrievalHitService;

    @GetMapping("/list")
    public CommonResult<List<AiKnowledgeRetrievalHitEntity>> list() {
        return success(aiKnowledgeRetrievalHitService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiKnowledgeRetrievalHitEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(aiKnowledgeRetrievalHitService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public CommonResult<AiKnowledgeRetrievalHitEntity> getById(@PathVariable Long id) {
        return success(aiKnowledgeRetrievalHitService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiKnowledgeRetrievalHitEntity entity) {
        return success(aiKnowledgeRetrievalHitService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiKnowledgeRetrievalHitEntity entity) {
        return success(aiKnowledgeRetrievalHitService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiKnowledgeRetrievalHitService.removeById(id));
    }

}