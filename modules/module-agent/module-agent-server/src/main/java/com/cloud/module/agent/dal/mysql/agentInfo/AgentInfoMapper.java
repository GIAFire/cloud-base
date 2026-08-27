package com.cloud.module.agent.dal.mysql.agentInfo;

import java.util.*;

import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agent.dal.dataobject.agentInfo.AgentInfoDO;
import org.apache.ibatis.annotations.Mapper;
import com.cloud.module.agent.controller.admin.agentInfo.vo.*;

/**
 * Agent 定义表 Mapper
 *
 * @author ZR
 */
@Mapper
public interface AgentInfoMapper extends BaseMapperX<AgentInfoDO> {

    default PageResult<AgentInfoDO> selectPage(AgentInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AgentInfoDO>()
                .eqIfPresent(AgentInfoDO::getModelId, reqVO.getModelId())
                .eqIfPresent(AgentInfoDO::getSysPromptId, reqVO.getSysPromptId())
                .eqIfPresent(AgentInfoDO::getAgentCode, reqVO.getAgentCode())
                .likeIfPresent(AgentInfoDO::getAgentName, reqVO.getAgentName())
                .eqIfPresent(AgentInfoDO::getDescription, reqVO.getDescription())
                .eqIfPresent(AgentInfoDO::getMaxIters, reqVO.getMaxIters())
                .eqIfPresent(AgentInfoDO::getPermissionMode, reqVO.getPermissionMode())
                .eqIfPresent(AgentInfoDO::getCompactionEnabled, reqVO.getCompactionEnabled())
                .eqIfPresent(AgentInfoDO::getTriggerMessages, reqVO.getTriggerMessages())
                .eqIfPresent(AgentInfoDO::getKeepMessages, reqVO.getKeepMessages())
                .eqIfPresent(AgentInfoDO::getTriggerTokens, reqVO.getTriggerTokens())
                .eqIfPresent(AgentInfoDO::getKeepTokens, reqVO.getKeepTokens())
                .eqIfPresent(AgentInfoDO::getToolResultEvictionEnabled, reqVO.getToolResultEvictionEnabled())
                .eqIfPresent(AgentInfoDO::getMemoryEnable, reqVO.getMemoryEnable())
                .eqIfPresent(AgentInfoDO::getPlanModeEnabled, reqVO.getPlanModeEnabled())
                .eqIfPresent(AgentInfoDO::getAllowShellInPlanMode, reqVO.getAllowShellInPlanMode())
                .betweenIfPresent(AgentInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AgentInfoDO::getId));
    }

}