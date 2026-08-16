package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.knowledge.dto.KnowledgeChunkResponse;
import com.cloud.module.agent.knowledge.service.KnowledgeManagementService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.cloud.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/knowledgeDocuments")
@RequiredArgsConstructor
public class AiKnowledgeChunkController {

    private final KnowledgeManagementService managementService;

    @GetMapping("/{documentId}/chunks")
    public CommonResult<IPage<KnowledgeChunkResponse>> page(
            @PathVariable Long documentId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return success(
                managementService.pageChunks(documentId, current, size)
        );
    }
}
