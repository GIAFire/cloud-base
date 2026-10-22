package com.cloud.module.agent.service.agentInfo;

import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.util.object.BeanUtils;
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
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloud.module.agent.enums.ErrorCodeConstants.INFO_NOT_EXISTS;

/**
 * Agent 定义表 Service 实现类。
 *
 * <p>Agent 与分类、标签的绑定由本服务作为聚合事务的一部分维护。请求中的编号集合代表
 * 绑定的完整目标状态，而不是增量命令。</p>
 */
@Service
@Validated
public class AgentInfoServiceImpl implements AgentInfoService {

    private static final Comparator<AgentTaxonomyItemRespVO> TAXONOMY_ORDER =
            Comparator.comparing(AgentTaxonomyItemRespVO::getSort, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(AgentTaxonomyItemRespVO::getId);

    @Resource
    private AgentInfoMapper infoMapper;
    @Resource
    private AgentCategoryService categoryService;
    @Resource
    private AgentTagService tagService;
    @Resource
    private AgentCategoryAgentRelationMapper categoryRelationMapper;
    @Resource
    private AgentTagAgentRelationMapper tagRelationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInfo(AgentInfoSaveReqVO createReqVO) {
        Set<Long> categoryIds = normalizeIds(createReqVO.getCategoryIds());
        Set<Long> tagIds = normalizeIds(createReqVO.getTagIds());
        categoryService.validateCategoriesEnabled(categoryIds);
        tagService.validateTagsEnabled(tagIds);

        AgentInfoDO info = BeanUtils.toBean(createReqVO, AgentInfoDO.class);
        infoMapper.insert(info);
        insertCategoryRelations(info.getId(), categoryIds);
        insertTagRelations(info.getId(), tagIds);
        return info.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(AgentInfoSaveReqVO updateReqVO) {
        validateInfoExists(updateReqVO.getId());
        syncCategoryRelations(updateReqVO.getId(), normalizeIds(updateReqVO.getCategoryIds()));
        syncTagRelations(updateReqVO.getId(), normalizeIds(updateReqVO.getTagIds()));
        infoMapper.updateById(BeanUtils.toBean(updateReqVO, AgentInfoDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInfo(Long id) {
        validateInfoExists(id);
        categoryRelationMapper.deleteByAgentId(id);
        tagRelationMapper.deleteByAgentId(id);
        infoMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInfoListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        categoryRelationMapper.deleteByAgentIds(ids);
        tagRelationMapper.deleteByAgentIds(ids);
        infoMapper.deleteByIds(ids);
    }

    private AgentInfoDO validateInfoExists(Long id) {
        AgentInfoDO info = infoMapper.selectById(id);
        if (info == null) {
            throw exception(INFO_NOT_EXISTS);
        }
        return info;
    }

    @Override
    public AgentInfoDO getInfo(Long id) {
        return infoMapper.selectById(id);
    }

    @Override
    public AgentInfoRespVO getInfoDetail(Long id) {
        AgentInfoDO info = validateInfoExists(id);
        return buildInfoResponses(Collections.singletonList(info)).get(0);
    }

    @Override
    public PageResult<AgentInfoRespVO> getInfoPage(AgentInfoPageReqVO pageReqVO) {
        PageResult<AgentInfoDO> page = infoMapper.selectPage(pageReqVO);
        if (page.getList().isEmpty()) {
            return PageResult.empty(page.getTotal());
        }
        return new PageResult<>(buildInfoResponses(page.getList()), page.getTotal());
    }

    private void syncCategoryRelations(Long agentId, Set<Long> requestedIds) {
        Set<Long> existingIds = new LinkedHashSet<>(categoryRelationMapper.selectCategoryIdsByAgentId(agentId));
        Set<Long> createIds = difference(requestedIds, existingIds);
        Set<Long> deleteIds = difference(existingIds, requestedIds);
        // 已有的禁用分类可以继续保留；只有新增绑定必须处于启用状态。
        categoryService.validateCategoriesEnabled(createIds);
        insertCategoryRelations(agentId, createIds);
        categoryRelationMapper.deleteByAgentIdAndCategoryIds(agentId, deleteIds);
    }

    private void syncTagRelations(Long agentId, Set<Long> requestedIds) {
        Set<Long> existingIds = new LinkedHashSet<>(tagRelationMapper.selectTagIdsByAgentId(agentId));
        Set<Long> createIds = difference(requestedIds, existingIds);
        Set<Long> deleteIds = difference(existingIds, requestedIds);
        // 已有的禁用标签可以继续保留；只有新增绑定必须处于启用状态。
        tagService.validateTagsEnabled(createIds);
        insertTagRelations(agentId, createIds);
        tagRelationMapper.deleteByAgentIdAndTagIds(agentId, deleteIds);
    }

    private void insertCategoryRelations(Long agentId, Collection<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            return;
        }
        categoryRelationMapper.insertBatch(categoryIds.stream()
                .map(categoryId -> AgentCategoryAgentRelationDO.builder()
                        .agentId(agentId)
                        .categoryId(categoryId)
                        .build())
                .toList());
    }

    private void insertTagRelations(Long agentId, Collection<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return;
        }
        tagRelationMapper.insertBatch(tagIds.stream()
                .map(tagId -> AgentTagAgentRelationDO.builder()
                        .agentId(agentId)
                        .tagId(tagId)
                        .build())
                .toList());
    }

    private List<AgentInfoRespVO> buildInfoResponses(List<AgentInfoDO> infos) {
        List<Long> agentIds = infos.stream().map(AgentInfoDO::getId).toList();
        Map<Long, List<Long>> categoryIdsByAgent = categoryRelationMapper
                .selectCategoryIdsMapByAgentIds(agentIds);
        Map<Long, List<Long>> tagIdsByAgent = tagRelationMapper.selectTagIdsMapByAgentIds(agentIds);

        Set<Long> allCategoryIds = flattenIds(categoryIdsByAgent.values());
        Set<Long> allTagIds = flattenIds(tagIdsByAgent.values());
        Map<Long, AgentCategoryDO> categoryMap = categoryService.getCategoryList(allCategoryIds).stream()
                .collect(Collectors.toMap(AgentCategoryDO::getId, Function.identity()));
        Map<Long, AgentTagDO> tagMap = tagService.getTagList(allTagIds).stream()
                .collect(Collectors.toMap(AgentTagDO::getId, Function.identity()));

        List<AgentInfoRespVO> responses = new ArrayList<>(infos.size());
        for (AgentInfoDO info : infos) {
            AgentInfoRespVO response = BeanUtils.toBean(info, AgentInfoRespVO.class);
            List<AgentTaxonomyItemRespVO> categories = categoryIdsByAgent
                    .getOrDefault(info.getId(), Collections.emptyList()).stream()
                    .map(categoryMap::get)
                    .filter(Objects::nonNull)
                    .map(AgentInfoServiceImpl::toCategoryItem)
                    .sorted(TAXONOMY_ORDER)
                    .toList();
            List<AgentTaxonomyItemRespVO> tags = tagIdsByAgent
                    .getOrDefault(info.getId(), Collections.emptyList()).stream()
                    .map(tagMap::get)
                    .filter(Objects::nonNull)
                    .map(AgentInfoServiceImpl::toTagItem)
                    .sorted(TAXONOMY_ORDER)
                    .toList();
            response.setCategories(categories);
            response.setCategoryIds(categories.stream().map(AgentTaxonomyItemRespVO::getId).toList());
            response.setTags(tags);
            response.setTagIds(tags.stream().map(AgentTaxonomyItemRespVO::getId).toList());
            responses.add(response);
        }
        return responses;
    }

    private static AgentTaxonomyItemRespVO toCategoryItem(AgentCategoryDO category) {
        AgentTaxonomyItemRespVO item = new AgentTaxonomyItemRespVO();
        item.setId(category.getId());
        item.setName(category.getName());
        item.setStatus(category.getStatus());
        item.setSort(category.getSort());
        return item;
    }

    private static AgentTaxonomyItemRespVO toTagItem(AgentTagDO tag) {
        AgentTaxonomyItemRespVO item = new AgentTaxonomyItemRespVO();
        item.setId(tag.getId());
        item.setName(tag.getName());
        item.setStatus(tag.getStatus());
        item.setSort(tag.getSort());
        return item;
    }

    private static Set<Long> normalizeIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Set<Long> difference(Set<Long> left, Set<Long> right) {
        Set<Long> result = new LinkedHashSet<>(left);
        result.removeAll(right);
        return result;
    }

    private static Set<Long> flattenIds(Collection<List<Long>> values) {
        return values.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
