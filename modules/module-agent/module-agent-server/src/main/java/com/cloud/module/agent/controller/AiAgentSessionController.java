package com.cloud.module.agent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.framework.security.core.util.SecurityFrameworkUtils;
import com.cloud.module.agent.entity.AiAgentSessionEntity;
import com.cloud.module.agent.entity.DTO.AgentConfigDTO;
import com.cloud.module.agent.entity.message.AiAgentSessionCreateRequest;
import com.cloud.module.agent.service.AiAgentService;
import com.cloud.module.agent.service.AiAgentSessionService;
import static com.cloud.framework.common.pojo.CommonResult.success;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * Agent 会话表：保存用户与 Agent 的一次连续对话 前端控制器
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
@RestController
@RequestMapping("/agentSession")
@AllArgsConstructor
public class AiAgentSessionController {
    private final AiAgentSessionService aiAgentSessionService;
    private final AiAgentService aiAgentService;

    @GetMapping("/list")
    public CommonResult<List<AiAgentSessionEntity>> list() {
        return success(aiAgentSessionService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiAgentSessionEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) String keyword
    ) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        LambdaQueryWrapper<AiAgentSessionEntity> query = new LambdaQueryWrapper<>();
        query.eq(AiAgentSessionEntity::getTenantId, loginUser.getTenantId())
                .eq(AiAgentSessionEntity::getUserId, loginUser.getId());
        if (agentId != null) {
            query.eq(AiAgentSessionEntity::getAgentId, agentId);
        }
        if (sessionId != null) {
            query.eq(AiAgentSessionEntity::getId, sessionId);
        }
        if (StringUtils.hasText(keyword)) {
            query.like(AiAgentSessionEntity::getTitle, keyword.trim());
        }
        query.orderByDesc(AiAgentSessionEntity::getLastMessageAt)
                .orderByDesc(AiAgentSessionEntity::getUpdateTime)
                .orderByDesc(AiAgentSessionEntity::getCreateTime);
        return success(aiAgentSessionService.page(new Page<>(current, size), query));
    }

    @GetMapping("/{id}")
    public CommonResult<AiAgentSessionEntity> getById(@PathVariable Long id) {
        return success(aiAgentSessionService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<AiAgentSessionEntity> create(@RequestBody AiAgentSessionCreateRequest request) {
        if (request == null || request.getAgentId() == null) {
            return CommonResult.error(500,"agentId must not be null");
        }
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        AgentConfigDTO agentConfig = aiAgentService.getAgentConfigById(request.getAgentId(),loginUser);
        return success(aiAgentSessionService.createSession(
                loginUser,
                request.getAgentId(),
                agentConfig.getAgentConfigId(),
                request.getTitle()
        ));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiAgentSessionEntity entity) {
        return success(aiAgentSessionService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiAgentSessionService.removeById(id));
    }

}
