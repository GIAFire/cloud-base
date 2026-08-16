package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiSkillInfoEntity;
import com.cloud.module.agent.entity.DTO.*;
import com.cloud.module.agent.service.SkillManagementService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/agent/skillInfo")
@RequiredArgsConstructor
public class AiSkillInfoController {

    private final SkillManagementService managementService;

    @GetMapping("/metrics")
    public CommonResult<SkillMetricsResponse> metrics() {
        return success(managementService.metrics());
    }

    @GetMapping("/list")
    public CommonResult<List<AiSkillInfoEntity>> listCandidates() {
        return success(managementService.listCandidates());
    }

    @GetMapping("/page")
    public CommonResult<IPage<SkillListItemResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) String riskLevel
    ) {
        return success(managementService.pageSkills(
                current, size, keyword, category, status, riskLevel
        ));
    }

    @GetMapping("/{id}")
    public CommonResult<SkillDetailResponse> getById(@PathVariable Long id) {
        return success(managementService.detail(id));
    }

    @PostMapping("/create")
    public CommonResult<SkillDetailResponse> create(@RequestBody AiSkillInfoSaveRequest request) {
        return success(managementService.create(request));
    }

    @PostMapping("/update")
    public CommonResult<SkillDetailResponse> update(@RequestBody AiSkillInfoSaveRequest request) {
        return success(managementService.update(request));
    }

    @DeleteMapping("/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(managementService.delete(id));
    }

    @GetMapping("/logs/page")
    public CommonResult<IPage<SkillUseLogResponse>> pageLogs(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long skillId,
            @RequestParam(required = false) Byte success,
            @RequestParam(required = false) String operation
    ) {
        return success(managementService.pageLogs(
                current, size, skillId, success, operation
        ));
    }

    @GetMapping("/logs/recent")
    public CommonResult<List<SkillUseLogResponse>> recentLogs(
            @RequestParam(defaultValue = "6") int limit,
            @RequestParam(required = false) Byte success
    ) {
        return success(managementService.recentLogs(limit, success));
    }
}
