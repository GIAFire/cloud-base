package com.cloud.module.agent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.module.agent.config.UserContext;
import com.cloud.module.agent.entity.AiAgentMessageEntity;
import com.cloud.module.agent.service.AiAgentMessageService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/**
 * <p>
 * Agent 消息表：保存用户输入、Agent 回复、工具消息等完整上下文 前端控制器
 * </p>
 *
 * @author 
 * @since 2026-06-20
 */
@RestController
@RequestMapping("/agentMessage")
@AllArgsConstructor
public class AiAgentMessageController {
    private final AiAgentMessageService aiAgentMessageService;

    @GetMapping("/list")
    public CommonResult<List<AiAgentMessageEntity>> list() {
        return success(aiAgentMessageService.list());
    }

    @GetMapping("/page")
    public CommonResult<IPage<AiAgentMessageEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long sessionId
    ) {
        LambdaQueryWrapper<AiAgentMessageEntity> query = new LambdaQueryWrapper<>();
        if (sessionId != null) {
            query.eq(AiAgentMessageEntity::getSessionId, sessionId);
        }
        query.and(wrapper -> wrapper.eq(AiAgentMessageEntity::getDeleted, 0)
                        .or()
                        .isNull(AiAgentMessageEntity::getDeleted))
                .orderByDesc(AiAgentMessageEntity::getSeq)
                .orderByDesc(AiAgentMessageEntity::getCreateTime);
        return success(aiAgentMessageService.page(new Page<>(current, size), query));
    }

    @GetMapping("/{id}")
    public CommonResult<AiAgentMessageEntity> getById(@PathVariable Long id) {
        return success(aiAgentMessageService.getById(id));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody AiAgentMessageEntity entity) {
        return success(aiAgentMessageService.save(entity));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody AiAgentMessageEntity entity) {
        return success(aiAgentMessageService.updateById(entity));
    }

    @GetMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(aiAgentMessageService.removeById(id));
    }

}
