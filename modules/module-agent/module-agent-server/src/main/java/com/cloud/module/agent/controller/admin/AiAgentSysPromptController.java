package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.DTO.*;
import com.cloud.module.agent.service.SysPromptManagementService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Supplier;

import static com.cloud.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/sysPrompt")
@RequiredArgsConstructor
public class AiAgentSysPromptController {

    private final SysPromptManagementService managementService;

    @GetMapping("/metrics")
    public CommonResult<SysPromptMetricsResponse> metrics() {
        return handle(managementService::metrics);
    }

    @GetMapping("/analytics")
    public CommonResult<SysPromptAnalyticsResponse> analytics(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return handle(() -> managementService.analytics(limit));
    }

    @GetMapping("/page")
    public CommonResult<IPage<SysPromptListItemResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "8") long size,
            @RequestParam(required = false) String keyword
    ) {
        return handle(() -> managementService.page(current, size, keyword));
    }

    @GetMapping("/list")
    public CommonResult<List<SysPromptOptionResponse>> list() {
        return handle(managementService::listOptions);
    }

    @GetMapping("/{id}")
    public CommonResult<SysPromptDetailResponse> detail(
            @PathVariable Long id
    ) {
        return success(managementService.detail(id));
    }

    @PostMapping("/create")
    public CommonResult<SysPromptDetailResponse> create(
            @RequestBody SysPromptSaveRequest request
    ) {
        return handle(() -> managementService.create(request));
    }

    @PostMapping("/update")
    public CommonResult<SysPromptDetailResponse> update(
            @RequestBody SysPromptSaveRequest request
    ) {
        return handle(() -> managementService.update(request));
    }

    @DeleteMapping("/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return handle(() -> managementService.delete(id));
    }

    private <T> CommonResult<T> handle(Supplier<T> supplier) {
        try {
            return success(supplier.get());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return CommonResult.error(500,exception.getMessage());
        }
    }
}
