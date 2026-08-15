package com.cloud.module.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.module.agent.entity.AiAgentWorkspaceFileEntity;
import io.agentscope.core.agent.RuntimeContext;

/**
 * <p>
 * Agent工作区文件表 服务类
 * </p>
 *
 * @author zhiRan
 * @since 2026-07-05
 */
public interface AiAgentWorkspaceFileService extends IService<AiAgentWorkspaceFileEntity> {

    AiAgentWorkspaceFileEntity saveGeneratedFile(RuntimeContext runtimeContext, String toolCallId, String fileName, String content);

}
