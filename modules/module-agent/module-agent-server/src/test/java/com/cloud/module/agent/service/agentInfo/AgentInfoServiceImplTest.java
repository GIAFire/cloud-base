package com.cloud.module.agent.service.agentInfo;

import com.cloud.framework.common.enums.CommonStatusEnum;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.module.agent.controller.admin.agentInfo.vo.AgentInfoPageReqVO;
import com.cloud.module.agent.controller.admin.agentInfo.vo.AgentInfoRespVO;
import com.cloud.module.agent.controller.admin.agentInfo.vo.AgentInfoSaveReqVO;
import com.cloud.module.agent.controller.admin.agentInfo.vo.AgentTaxonomyItemRespVO;
import com.cloud.module.agent.dal.dataobject.agentInfo.AgentInfoDO;
import com.cloud.module.agent.dal.dataobject.category.AgentCategoryDO;
import com.cloud.module.agent.dal.dataobject.relation.AgentCategoryAgentRelationDO;
import com.cloud.module.agent.dal.dataobject.relation.AgentTagAgentRelationDO;
import com.cloud.module.agent.dal.dataobject.tag.AgentTagDO;
import com.cloud.module.agent.dal.mysql.agentInfo.AgentInfoMapper;
import com.cloud.module.agent.dal.mysql.relation.AgentCategoryAgentRelationMapper;
import com.cloud.module.agent.dal.mysql.relation.AgentTagAgentRelationMapper;
import com.cloud.module.agent.service.category.AgentCategoryService;
import com.cloud.module.agent.service.tag.AgentTagService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentInfoServiceImplTest {

    @Mock
    private AgentInfoMapper infoMapper;

    @Mock
    private AgentCategoryService categoryService;

    @Mock
    private AgentTagService tagService;

    @Mock
    private AgentCategoryAgentRelationMapper categoryRelationMapper;

    @Mock
    private AgentTagAgentRelationMapper tagRelationMapper;

    @InjectMocks
    private AgentInfoServiceImpl infoService;

    @Test
    void creates_agent_after_validating_and_inserting_both_relation_sets() {
        AgentInfoSaveReqVO request = request(null,
                List.of(101L, 102L, 101L),
                List.of(201L, 202L, 201L));
        when(infoMapper.insert(any(AgentInfoDO.class))).thenAnswer(invocation -> {
            AgentInfoDO info = invocation.getArgument(0);
            info.setId(1L);
            return 1;
        });

        Long id = infoService.createInfo(request);

        assertThat(id).isEqualTo(1L);
        verify(categoryService).validateCategoriesEnabled(Set.of(101L, 102L));
        verify(tagService).validateTagsEnabled(Set.of(201L, 202L));
        verify(categoryRelationMapper).insertBatch(argThat(relations ->
                hasCategoryBindings(relations, 1L, List.of(101L, 102L))));
        verify(tagRelationMapper).insertBatch(argThat(relations ->
                hasTagBindings(relations, 1L, List.of(201L, 202L))));
        verify(categoryRelationMapper, never()).deleteByAgentIdAndCategoryIds(any(), anyCollection());
        verify(tagRelationMapper, never()).deleteByAgentIdAndTagIds(any(), anyCollection());
    }

    @Test
    void updates_only_relation_differences_and_does_not_revalidate_retained_bindings() {
        AgentInfoSaveReqVO request = request(1L, List.of(101L, 103L), List.of(202L, 203L));
        when(infoMapper.selectById(1L)).thenReturn(info(1L, "writer"));
        // 101/202 可以是已禁用项；只要继续保留，就不应再校验其启用状态。
        when(categoryRelationMapper.selectCategoryIdsByAgentId(1L)).thenReturn(List.of(101L, 102L));
        when(tagRelationMapper.selectTagIdsByAgentId(1L)).thenReturn(List.of(201L, 202L));

        infoService.updateInfo(request);

        verify(categoryService).validateCategoriesEnabled(Set.of(103L));
        verify(tagService).validateTagsEnabled(Set.of(203L));
        verifyNoMoreInteractions(categoryService, tagService);
        verify(categoryRelationMapper).insertBatch(argThat(relations ->
                hasCategoryBindings(relations, 1L, List.of(103L))));
        verify(tagRelationMapper).insertBatch(argThat(relations ->
                hasTagBindings(relations, 1L, List.of(203L))));
        verify(categoryRelationMapper).deleteByAgentIdAndCategoryIds(eq(1L),
                argThat(ids -> ids.equals(Set.of(102L))));
        verify(tagRelationMapper).deleteByAgentIdAndTagIds(eq(1L),
                argThat(ids -> ids.equals(Set.of(201L))));
        verify(infoMapper).updateById(argThat(info -> info.getId().equals(1L)));
    }

    @Test
    void deletes_relations_before_deleting_agent() {
        when(infoMapper.selectById(1L)).thenReturn(info(1L, "writer"));

        infoService.deleteInfo(1L);

        InOrder operations = inOrder(infoMapper, categoryRelationMapper, tagRelationMapper);
        operations.verify(infoMapper).selectById(1L);
        operations.verify(categoryRelationMapper).deleteByAgentId(1L);
        operations.verify(tagRelationMapper).deleteByAgentId(1L);
        operations.verify(infoMapper).deleteById(1L);
    }

    @Test
    void assembles_detail_with_category_and_tag_summaries() {
        AgentInfoDO info = info(1L, "writer");
        when(infoMapper.selectById(1L)).thenReturn(info);
        when(categoryRelationMapper.selectCategoryIdsMapByAgentIds(List.of(1L)))
                .thenReturn(Map.of(1L, List.of(101L)));
        when(tagRelationMapper.selectTagIdsMapByAgentIds(List.of(1L)))
                .thenReturn(Map.of(1L, List.of(201L)));
        when(categoryService.getCategoryList(Set.of(101L))).thenReturn(List.of(
                category(101L, "内容创作", 10, CommonStatusEnum.DISABLE.getStatus())));
        when(tagService.getTagList(Set.of(201L))).thenReturn(List.of(
                tag(201L, "中文", 20, CommonStatusEnum.ENABLE.getStatus())));

        AgentInfoRespVO response = infoService.getInfoDetail(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCategoryIds()).containsExactly(101L);
        assertThat(response.getCategories()).singleElement().satisfies(item -> {
            assertThat(item.getName()).isEqualTo("内容创作");
            assertThat(item.getStatus()).isEqualTo(CommonStatusEnum.DISABLE.getStatus());
        });
        assertThat(response.getTagIds()).containsExactly(201L);
        assertThat(response.getTags()).extracting(AgentTaxonomyItemRespVO::getName)
                .containsExactly("中文");
    }

    @Test
    void assembles_page_taxonomy_with_one_batch_query_per_relation_and_catalog() {
        AgentInfoPageReqVO request = new AgentInfoPageReqVO();
        List<AgentInfoDO> infos = List.of(info(1L, "writer"), info(2L, "analyst"));
        when(infoMapper.selectPage(request)).thenReturn(new PageResult<>(infos, 2L));
        when(categoryRelationMapper.selectCategoryIdsMapByAgentIds(List.of(1L, 2L))).thenReturn(Map.of(
                1L, List.of(101L, 102L),
                2L, List.of(103L)));
        when(tagRelationMapper.selectTagIdsMapByAgentIds(List.of(1L, 2L))).thenReturn(Map.of(
                1L, List.of(201L),
                2L, List.of(202L, 203L)));
        when(categoryService.getCategoryList(Set.of(101L, 102L, 103L))).thenReturn(List.of(
                category(101L, "开发工具", 20, CommonStatusEnum.ENABLE.getStatus()),
                category(102L, "内容创作", 10, CommonStatusEnum.ENABLE.getStatus()),
                category(103L, "办公效率", 5, CommonStatusEnum.ENABLE.getStatus())));
        when(tagService.getTagList(Set.of(201L, 202L, 203L))).thenReturn(List.of(
                tag(201L, "中文", 20, CommonStatusEnum.ENABLE.getStatus()),
                tag(202L, "长文本", 30, CommonStatusEnum.DISABLE.getStatus()),
                tag(203L, "数据分析", 10, CommonStatusEnum.ENABLE.getStatus())));

        PageResult<AgentInfoRespVO> page = infoService.getInfoPage(request);

        assertThat(page.getTotal()).isEqualTo(2L);
        assertThat(page.getList()).hasSize(2);
        assertThat(page.getList().get(0).getCategoryIds()).containsExactly(102L, 101L);
        assertThat(page.getList().get(0).getTagIds()).containsExactly(201L);
        assertThat(page.getList().get(1).getCategoryIds()).containsExactly(103L);
        assertThat(page.getList().get(1).getTagIds()).containsExactly(203L, 202L);

        verify(categoryRelationMapper).selectCategoryIdsMapByAgentIds(List.of(1L, 2L));
        verify(tagRelationMapper).selectTagIdsMapByAgentIds(List.of(1L, 2L));
        verify(categoryService).getCategoryList(anyCollection());
        verify(tagService).getTagList(anyCollection());
        verifyNoMoreInteractions(categoryRelationMapper, tagRelationMapper, categoryService, tagService);
    }

    private static AgentInfoSaveReqVO request(Long id, List<Long> categoryIds, List<Long> tagIds) {
        AgentInfoSaveReqVO request = new AgentInfoSaveReqVO();
        request.setId(id);
        request.setAgentCode("writer");
        request.setAgentName("写作助手");
        request.setCategoryIds(categoryIds);
        request.setTagIds(tagIds);
        return request;
    }

    private static AgentInfoDO info(Long id, String code) {
        AgentInfoDO info = new AgentInfoDO();
        info.setId(id);
        info.setAgentCode(code);
        info.setAgentName(code);
        return info;
    }

    private static AgentCategoryDO category(Long id, String name, Integer sort, Integer status) {
        AgentCategoryDO category = new AgentCategoryDO();
        category.setId(id);
        category.setName(name);
        category.setSort(sort);
        category.setStatus(status);
        return category;
    }

    private static AgentTagDO tag(Long id, String name, Integer sort, Integer status) {
        AgentTagDO tag = new AgentTagDO();
        tag.setId(id);
        tag.setName(name);
        tag.setSort(sort);
        tag.setStatus(status);
        return tag;
    }

    private static boolean hasCategoryBindings(Collection<AgentCategoryAgentRelationDO> relations,
                                               Long agentId, List<Long> categoryIds) {
        return relations.stream().allMatch(relation -> agentId.equals(relation.getAgentId()))
                && relations.stream().map(AgentCategoryAgentRelationDO::getCategoryId).toList().equals(categoryIds);
    }

    private static boolean hasTagBindings(Collection<AgentTagAgentRelationDO> relations,
                                          Long agentId, List<Long> tagIds) {
        return relations.stream().allMatch(relation -> agentId.equals(relation.getAgentId()))
                && relations.stream().map(AgentTagAgentRelationDO::getTagId).toList().equals(tagIds);
    }

}
