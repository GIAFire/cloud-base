package com.cloud.module.agent.dal.mysql.relation;

import com.cloud.module.agent.dal.dataobject.relation.AgentCategoryAgentRelationDO;
import com.cloud.module.agent.dal.dataobject.relation.AgentTagAgentRelationDO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AgentRelationMapperTest {

    @Test
    void groups_category_ids_by_agent_for_batch_assembly() {
        AgentCategoryAgentRelationMapper mapper = mock(AgentCategoryAgentRelationMapper.class, CALLS_REAL_METHODS);
        List<Long> agentIds = List.of(10L, 20L);
        doReturn(List.of(
                categoryRelation(10L, 101L),
                categoryRelation(10L, 102L),
                categoryRelation(20L, 103L)
        )).when(mapper).selectByAgentIds(agentIds);

        assertThat(mapper.selectCategoryIdsByAgentIds(agentIds))
                .containsEntry(10L, List.of(101L, 102L))
                .containsEntry(20L, List.of(103L));
    }

    @Test
    void groups_tag_ids_by_agent_for_batch_assembly() {
        AgentTagAgentRelationMapper mapper = mock(AgentTagAgentRelationMapper.class, CALLS_REAL_METHODS);
        List<Long> agentIds = List.of(10L, 20L);
        doReturn(List.of(
                tagRelation(10L, 201L),
                tagRelation(20L, 202L),
                tagRelation(20L, 203L)
        )).when(mapper).selectByAgentIds(agentIds);

        assertThat(mapper.selectTagIdsByAgentIds(agentIds))
                .containsEntry(10L, List.of(201L))
                .containsEntry(20L, List.of(202L, 203L));
    }

    @Test
    void skips_queries_for_empty_agent_ids() {
        AgentCategoryAgentRelationMapper categoryMapper =
                mock(AgentCategoryAgentRelationMapper.class, CALLS_REAL_METHODS);
        AgentTagAgentRelationMapper tagMapper = mock(AgentTagAgentRelationMapper.class, CALLS_REAL_METHODS);

        assertThat(categoryMapper.selectCategoryIdsByAgentIds(List.of())).isEmpty();
        assertThat(tagMapper.selectTagIdsByAgentIds(List.of())).isEmpty();
        verify(categoryMapper, never()).selectByAgentIds(List.of());
        verify(tagMapper, never()).selectByAgentIds(List.of());
    }

    private static AgentCategoryAgentRelationDO categoryRelation(Long agentId, Long categoryId) {
        return AgentCategoryAgentRelationDO.builder()
                .agentId(agentId)
                .categoryId(categoryId)
                .build();
    }

    private static AgentTagAgentRelationDO tagRelation(Long agentId, Long tagId) {
        return AgentTagAgentRelationDO.builder()
                .agentId(agentId)
                .tagId(tagId)
                .build();
    }

}
