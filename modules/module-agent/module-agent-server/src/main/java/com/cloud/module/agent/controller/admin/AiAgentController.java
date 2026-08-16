package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.DTO.*;
import com.cloud.module.agent.service.AgentManagementService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static com.cloud.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/agent/agent")
@RequiredArgsConstructor
public class AiAgentController {

    private final AgentManagementService managementService;

    @GetMapping("/metrics")
    public CommonResult<AgentMetricsResponse> metrics() {
        return handle(managementService::metrics);
    }

    @GetMapping("/recent-runs")
    public CommonResult<AgentRecentRunsResponse> recentRuns(
            @RequestParam(defaultValue = "5") int size
    ) {
        return handle(() -> managementService.recentRuns(size));
    }

    @GetMapping("/page")
    public CommonResult<IPage<AgentListItemResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword
    ) {
        return handle(() -> managementService.page(current, size, keyword));
    }

    @GetMapping("/{id}")
    public CommonResult<AgentDetailResponse> detail(@PathVariable Long id) {
        return handle(() -> managementService.detail(id));
    }

    @PostMapping
    public CommonResult<AgentDetailResponse> create(@RequestBody AgentSaveRequest request) {
        return handle(() -> managementService.create(request));
    }

    @PutMapping("/{id}")
    public CommonResult<AgentDetailResponse> update(
            @PathVariable Long id,
            @RequestBody AgentSaveRequest request
    ) {
        return handle(() -> managementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(managementService.delete(id));
    }

    @GetMapping("/{id}/runs")
    public CommonResult<IPage<AgentRunLogResponse>> runs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return handle(() -> managementService.runs(
                id, current, size, status, start, end));
    }

    private <T> CommonResult<T> handle(Supplier<T> supplier) {
        try {
            return success(supplier.get());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return CommonResult.error(500,exception.getMessage());
        }
    }
}
