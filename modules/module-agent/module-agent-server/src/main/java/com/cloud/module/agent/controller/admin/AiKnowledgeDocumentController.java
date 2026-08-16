package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.knowledge.dto.KnowledgeDocumentResponse;
import com.cloud.module.agent.knowledge.dto.KnowledgeIndexRequest;
import com.cloud.module.agent.knowledge.service.KnowledgeManagementService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.cloud.framework.common.pojo.CommonResult.success;

@RestController
@RequiredArgsConstructor
public class AiKnowledgeDocumentController {

    private final KnowledgeManagementService managementService;

    @GetMapping("/knowledgeBases/{knowledgeBaseId}/documents")
    public CommonResult<IPage<KnowledgeDocumentResponse>> page(
            @PathVariable Long knowledgeBaseId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String parseStatus
    ) {
        return success(
                managementService.pageDocuments(
                        knowledgeBaseId,
                        current,
                        size,
                        keyword,
                        parseStatus
                )
        );
    }

    @PostMapping(
            value = "/knowledgeBases/{knowledgeBaseId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CommonResult<KnowledgeDocumentResponse> upload(
            @PathVariable Long knowledgeBaseId,
            @RequestPart("file") MultipartFile file
    ) {
        return success(
                managementService.uploadDocument(knowledgeBaseId, file)
        );
    }

    @DeleteMapping("/knowledgeDocuments/{documentId}")
    public CommonResult<KnowledgeDocumentResponse> delete(
            @PathVariable Long documentId
    ) {
        return success(managementService.deleteDocument(documentId));
    }

    @PostMapping("/knowledgeDocuments/{documentId}/index")
    public CommonResult<KnowledgeDocumentResponse> submitIndex(
            @PathVariable Long documentId,
            @RequestBody KnowledgeIndexRequest request
    ) {
        return success(
                managementService.submitIndex(documentId, request)
        );
    }
}
