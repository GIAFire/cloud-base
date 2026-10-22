package com.cloud.module.agent.dal.mysql.agentInfo;

import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloud.module.agent.controller.admin.agentInfo.vo.AgentInfoPageReqVO;
import com.cloud.module.agent.dal.dataobject.agentInfo.AgentInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Agent 定义表 Mapper
 *
 * @author ZR
 */
@Mapper
public interface AgentInfoMapper extends BaseMapperX<AgentInfoDO> {

    default PageResult<AgentInfoDO> selectPage(AgentInfoPageReqVO reqVO) {
        LambdaQueryWrapperX<AgentInfoDO> query = new LambdaQueryWrapperX<AgentInfoDO>()
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
                .eqIfPresent(AgentInfoDO::getPersonality, reqVO.getPersonality())
                .betweenIfPresent(AgentInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AgentInfoDO::getId);
        addRelationFilter(query, "agent_category_agent", "category_id", reqVO.getCategoryIds());
        addRelationFilter(query, "agent_tag_agent", "tag_id", reqVO.getTagIds());
        return selectPage(reqVO, query);
    }

    private static void addRelationFilter(LambdaQueryWrapperX<AgentInfoDO> query, String relationTable,
                                          String taxonomyColumn, Collection<Long> taxonomyIds) {
        if (taxonomyIds == null || taxonomyIds.isEmpty()) {
            return;
        }
        String placeholders = IntStream.range(0, taxonomyIds.size())
                .mapToObj(index -> "{" + index + "}")
                .collect(Collectors.joining(", "));
        String existsSql = "SELECT 1 FROM " + relationTable + " relation "
                + "WHERE relation.agent_id = agent_info.id "
                + "AND relation.tenant_id = agent_info.tenant_id "
                + "AND relation." + taxonomyColumn + " IN (" + placeholders + ")";
        query.exists(existsSql, taxonomyIds.toArray());
    }

}
