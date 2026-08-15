package com.cloud.module.agent.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.DTO.SkillUseLogResponse;
import com.cloud.module.agent.service.SkillManagementService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.cloud.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/skillLog")
@RequiredArgsConstructor
public class AiSkillLogController {

    private final SkillManagementService managementService;

    @GetMapping("/page")
    public CommonResult<IPage<SkillUseLogResponse>> page(
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
}
