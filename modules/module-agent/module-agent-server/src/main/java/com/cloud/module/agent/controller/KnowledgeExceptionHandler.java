package com.cloud.module.agent.controller;

import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.knowledge.KnowledgeOperationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice(assignableTypes = {
        AiKnowledgeBaseController.class,
        AiKnowledgeDocumentController.class,
        AiKnowledgeChunkController.class,
        AiKnowledgeAgentBindingController.class
})
public class KnowledgeExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(KnowledgeExceptionHandler.class);

    @ExceptionHandler(KnowledgeOperationException.class)
    public CommonResult<Void> handleKnowledgeOperation(
            KnowledgeOperationException error
    ) {
        return CommonResult.error(500,error.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult<Void> handleBadRequest(IllegalArgumentException error) {
        return CommonResult.error(500,error.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public CommonResult<Void> handleUploadTooLarge(
            MaxUploadSizeExceededException ignored
    ) {
        return CommonResult.error(500,"文件大小不能超过 50 MB");
    }

    @ExceptionHandler(Exception.class)
    public CommonResult<Void> handleUnknown(Exception error) {
        log.error(
                "Knowledge management request failed, errorType={}",
                error.getClass().getSimpleName()
        );
        return CommonResult.error(500,"知识管理操作失败，请查看服务日志");
    }
}
