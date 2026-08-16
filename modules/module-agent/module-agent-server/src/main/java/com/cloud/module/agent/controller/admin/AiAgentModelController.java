package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.DTO.*;
import com.cloud.module.agent.service.ModelManagementService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/modelConfig")
@RequiredArgsConstructor
public class AiAgentModelController {

    private final ModelManagementService managementService;

    @GetMapping("/metrics")
    public CommonResult<ModelMetricsResponse> metrics() {
        return handle(managementService::metrics);
    }

    @GetMapping("/analytics")
    public CommonResult<ModelAnalyticsResponse> analytics(
            @RequestParam(defaultValue = "7") int days
    ) {
        return handle(() -> managementService.analytics(days));
    }

    @GetMapping("/page")
    public CommonResult<IPage<ModelListItemResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String protocol,
            @RequestParam(required = false) Integer status
    ) {
        return handle(() -> managementService.page(
                current,
                size,
                keyword,
                provider,
                protocol,
                status
        ));
    }

    @GetMapping("/list")
    public CommonResult<List<ModelCandidateResponse>> list() {
        return handle(managementService::listCandidates);
    }

    @GetMapping("/{id}")
    public CommonResult<ModelDetailResponse> detail(
            @PathVariable Long id,
            HttpServletResponse response
    ) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        return handle(() -> managementService.detail(id));
    }

    @PostMapping("/create")
    public CommonResult<ModelListItemResponse> create(
            @RequestBody ModelSaveRequest request
    ) {
        return handle(() -> managementService.create(request));
    }

    @PostMapping("/update")
    public CommonResult<ModelListItemResponse> update(
            @RequestBody ModelSaveRequest request
    ) {
        return handle(() -> managementService.update(request));
    }

    @DeleteMapping("/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return handle(() -> managementService.delete(id));
    }

    @PostMapping("/test")
    public CommonResult<ModelTestResponse> test(@RequestBody ModelSaveRequest request) {
        return handle(() -> managementService.test(request));
    }

    @GetMapping("/logs/page")
    public CommonResult<IPage<ModelCallLogResponse>> logs(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long modelConfigId,
            @RequestParam(required = false) String callSource,
            @RequestParam(required = false) String status
    ) {
        return handle(() -> managementService.pageLogs(
                current,
                size,
                modelConfigId,
                callSource,
                status
        ));
    }

    private <T> CommonResult<T> handle(java.util.function.Supplier<T> supplier) {
        try {
            return success(supplier.get());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return CommonResult.error(500,exception.getMessage());
        }
    }
}
