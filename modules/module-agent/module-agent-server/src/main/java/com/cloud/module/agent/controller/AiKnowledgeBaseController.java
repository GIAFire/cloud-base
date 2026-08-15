package com.cloud.module.agent.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.knowledge.dto.*;
import com.cloud.module.agent.knowledge.service.KnowledgeManagementService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/knowledgeBases")
@RequiredArgsConstructor
public class AiKnowledgeBaseController {

    private final KnowledgeManagementService managementService;

    @GetMapping("/metrics")
    public CommonResult<KnowledgeMetricsResponse> metrics() {
        return success(managementService.knowledgeMetrics());
    }

    @GetMapping
    public CommonResult<IPage<KnowledgeBaseResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Byte status
    ) {
        return success(
                managementService.pageKnowledgeBases(
                        current,
                        size,
                        keyword,
                        status
                )
        );
    }

    @PostMapping
    public CommonResult<KnowledgeBaseResponse> create(
            @RequestBody KnowledgeBaseCreateRequest request
    ) {
        return success(managementService.createKnowledgeBase(request));
    }

    @GetMapping("/options")
    public CommonResult<List<KnowledgeBaseOptionResponse>> options() {
        return success(managementService.listKnowledgeBaseOptions());
    }

    @GetMapping("/{knowledgeBaseId}")
    public CommonResult<KnowledgeBaseResponse> get(
            @PathVariable Long knowledgeBaseId
    ) {
        return success(
                managementService.getKnowledgeBase(knowledgeBaseId)
        );
    }

    @PutMapping("/{knowledgeBaseId}")
    public CommonResult<KnowledgeBaseResponse> update(
            @PathVariable Long knowledgeBaseId,
            @RequestBody KnowledgeBaseUpdateRequest request
    ) {
        return success(
                managementService.updateKnowledgeBase(
                        knowledgeBaseId,
                        request
                )
        );
    }

    @DeleteMapping("/{knowledgeBaseId}")
    public CommonResult<Boolean> delete(
            @PathVariable Long knowledgeBaseId
    ) {
        return success(
                managementService.deleteKnowledgeBase(knowledgeBaseId)
        );
    }
}
