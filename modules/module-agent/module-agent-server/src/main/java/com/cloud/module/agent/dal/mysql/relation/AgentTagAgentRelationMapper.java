package com.cloud.module.agent.dal.mysql.relation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agent.dal.dataobject.relation.AgentTagAgentRelationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agent 与标签关联 Mapper。
 */
@Mapper
public interface AgentTagAgentRelationMapper extends BaseMapperX<AgentTagAgentRelationDO> {

    default List<AgentTagAgentRelationDO> selectByAgentId(Long agentId) {
        return selectList(AgentTagAgentRelationDO::getAgentId, agentId);
    }

    default List<AgentTagAgentRelationDO> selectByAgentIds(Collection<Long> agentIds) {
        return selectList(AgentTagAgentRelationDO::getAgentId, agentIds);
    }

    default List<Long> selectTagIdsByAgentId(Long agentId) {
        return selectByAgentId(agentId).stream()
                .map(AgentTagAgentRelationDO::getTagId)
                .toList();
    }

    default Map<Long, List<Long>> selectTagIdsByAgentIds(Collection<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return Map.of();
        }
        return selectByAgentIds(agentIds).stream()
                .collect(Collectors.groupingBy(AgentTagAgentRelationDO::getAgentId,
                        LinkedHashMap::new,
                        Collectors.mapping(AgentTagAgentRelationDO::getTagId, Collectors.toList())));
    }

    /**
     * 显式的 Map 命名别名，方便批量装配 Agent 响应。
     */
    default Map<Long, List<Long>> selectTagIdsMapByAgentIds(Collection<Long> agentIds) {
        return selectTagIdsByAgentIds(agentIds);
    }

    default Long countByTagId(Long tagId) {
        return selectCount(AgentTagAgentRelationDO::getTagId, tagId);
    }

    default int deleteByAgentId(Long agentId) {
        return delete(AgentTagAgentRelationDO::getAgentId, agentId);
    }

    default int deleteByAgentIds(Collection<Long> agentIds) {
        return deleteBatch(AgentTagAgentRelationDO::getAgentId, agentIds);
    }

    default int deleteByTagId(Long tagId) {
        return delete(AgentTagAgentRelationDO::getTagId, tagId);
    }

    default int deleteByTagIds(Collection<Long> tagIds) {
        return deleteBatch(AgentTagAgentRelationDO::getTagId, tagIds);
    }

    default int deleteByAgentIdAndTagIds(Long agentId, Collection<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return 0;
        }
        return delete(new LambdaQueryWrapper<AgentTagAgentRelationDO>()
                .eq(AgentTagAgentRelationDO::getAgentId, agentId)
                .in(AgentTagAgentRelationDO::getTagId, tagIds));
    }

}
