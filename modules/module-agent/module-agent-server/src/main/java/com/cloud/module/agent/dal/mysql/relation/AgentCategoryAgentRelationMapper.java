package com.cloud.module.agent.dal.mysql.relation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agent.dal.dataobject.relation.AgentCategoryAgentRelationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agent 与分类关联 Mapper。
 */
@Mapper
public interface AgentCategoryAgentRelationMapper extends BaseMapperX<AgentCategoryAgentRelationDO> {

    default List<AgentCategoryAgentRelationDO> selectByAgentId(Long agentId) {
        return selectList(AgentCategoryAgentRelationDO::getAgentId, agentId);
    }

    default List<AgentCategoryAgentRelationDO> selectByAgentIds(Collection<Long> agentIds) {
        return selectList(AgentCategoryAgentRelationDO::getAgentId, agentIds);
    }

    default List<Long> selectCategoryIdsByAgentId(Long agentId) {
        return selectByAgentId(agentId).stream()
                .map(AgentCategoryAgentRelationDO::getCategoryId)
                .toList();
    }

    default Map<Long, List<Long>> selectCategoryIdsByAgentIds(Collection<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return Map.of();
        }
        return selectByAgentIds(agentIds).stream()
                .collect(Collectors.groupingBy(AgentCategoryAgentRelationDO::getAgentId,
                        LinkedHashMap::new,
                        Collectors.mapping(AgentCategoryAgentRelationDO::getCategoryId, Collectors.toList())));
    }

    /**
     * 显式的 Map 命名别名，方便批量装配 Agent 响应。
     */
    default Map<Long, List<Long>> selectCategoryIdsMapByAgentIds(Collection<Long> agentIds) {
        return selectCategoryIdsByAgentIds(agentIds);
    }

    default Long countByCategoryId(Long categoryId) {
        return selectCount(AgentCategoryAgentRelationDO::getCategoryId, categoryId);
    }

    default int deleteByAgentId(Long agentId) {
        return delete(AgentCategoryAgentRelationDO::getAgentId, agentId);
    }

    default int deleteByAgentIds(Collection<Long> agentIds) {
        return deleteBatch(AgentCategoryAgentRelationDO::getAgentId, agentIds);
    }

    default int deleteByCategoryId(Long categoryId) {
        return delete(AgentCategoryAgentRelationDO::getCategoryId, categoryId);
    }

    default int deleteByCategoryIds(Collection<Long> categoryIds) {
        return deleteBatch(AgentCategoryAgentRelationDO::getCategoryId, categoryIds);
    }

    default int deleteByAgentIdAndCategoryIds(Long agentId, Collection<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return 0;
        }
        return delete(new LambdaQueryWrapper<AgentCategoryAgentRelationDO>()
                .eq(AgentCategoryAgentRelationDO::getAgentId, agentId)
                .in(AgentCategoryAgentRelationDO::getCategoryId, categoryIds));
    }

}
