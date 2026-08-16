package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.framework.security.core.util.SecurityFrameworkUtils;
import com.cloud.module.agent.entity.AiSkillAgentBindingEntity;
import com.cloud.module.agent.service.AiSkillAgentBindingService;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/skillAgentBinding")
@RequiredArgsConstructor
public class AiSkillAgentBindingController {

    private final AiSkillAgentBindingService bindingService;

    @GetMapping("/list")
    public CommonResult<List<AiSkillAgentBindingEntity>> list(
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) Long agentConfigId
    ) {
        Long tenantId = SecurityFrameworkUtils.getLoginUser().getTenantId();
        LambdaQueryWrapper<AiSkillAgentBindingEntity> query =
                new LambdaQueryWrapper<AiSkillAgentBindingEntity>()
                        .eq(AiSkillAgentBindingEntity::getTenantId, tenantId);
        if (agentId != null) {
            query.eq(AiSkillAgentBindingEntity::getAgentId, agentId);
        }
        if (agentConfigId != null) {
            query.eq(AiSkillAgentBindingEntity::getAgentConfigId, agentConfigId);
        }
        return success(bindingService.list(query));
    }
}
