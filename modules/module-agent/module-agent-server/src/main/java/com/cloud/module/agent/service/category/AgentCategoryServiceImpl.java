package com.cloud.module.agent.service.category;

import com.cloud.framework.common.enums.CommonStatusEnum;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.util.object.BeanUtils;
import com.cloud.module.agent.controller.admin.category.vo.AgentCategoryPageReqVO;
import com.cloud.module.agent.controller.admin.category.vo.AgentCategorySaveReqVO;
import com.cloud.module.agent.dal.dataobject.category.AgentCategoryDO;
import com.cloud.module.agent.dal.mysql.category.AgentCategoryMapper;
import com.cloud.module.agent.dal.mysql.relation.AgentCategoryAgentRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_CATEGORY_DISABLED;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_CATEGORY_IN_USE;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_CATEGORY_NAME_EXISTS;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_CATEGORY_NOT_EXISTS;

/**
 * Agent 分类 Service 实现类。
 */
@Service
@Validated
public class AgentCategoryServiceImpl implements AgentCategoryService {

    @Resource
    private AgentCategoryMapper categoryMapper;

    @Resource
    private AgentCategoryAgentRelationMapper categoryAgentRelationMapper;

    @Override
    public Long createCategory(AgentCategorySaveReqVO createReqVO) {
        String name = normalizeName(createReqVO.getName());
        validateCategoryNameUnique(null, name);
        AgentCategoryDO category = BeanUtils.toBean(createReqVO, AgentCategoryDO.class);
        category.setName(name);
        try {
            categoryMapper.insert(category);
        } catch (DuplicateKeyException ex) {
            throw exception(AGENT_CATEGORY_NAME_EXISTS, name);
        }
        return category.getId();
    }

    @Override
    public void updateCategory(AgentCategorySaveReqVO updateReqVO) {
        validateCategoryExists(updateReqVO.getId());
        String name = normalizeName(updateReqVO.getName());
        validateCategoryNameUnique(updateReqVO.getId(), name);
        AgentCategoryDO updateObj = BeanUtils.toBean(updateReqVO, AgentCategoryDO.class);
        updateObj.setName(name);
        try {
            categoryMapper.updateById(updateObj);
        } catch (DuplicateKeyException ex) {
            throw exception(AGENT_CATEGORY_NAME_EXISTS, name);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        validateCategoryExists(id);
        if (categoryAgentRelationMapper.countByCategoryId(id) > 0) {
            throw exception(AGENT_CATEGORY_IN_USE);
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public AgentCategoryDO getCategory(Long id) {
        return validateCategoryExists(id);
    }

    @Override
    public List<AgentCategoryDO> getCategoryList(Collection<Long> ids) {
        Set<Long> normalizedIds = normalizeIds(ids);
        return normalizedIds.isEmpty() ? Collections.emptyList() : categoryMapper.selectByIds(normalizedIds);
    }

    @Override
    public List<AgentCategoryDO> getEnabledCategoryList() {
        return categoryMapper.selectEnabledList();
    }

    @Override
    public PageResult<AgentCategoryDO> getCategoryPage(AgentCategoryPageReqVO pageReqVO) {
        return categoryMapper.selectPage(pageReqVO);
    }

    @Override
    public void validateCategoriesEnabled(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Map<Long, AgentCategoryDO> categoryMap = getCategoryList(ids).stream()
                .collect(Collectors.toMap(AgentCategoryDO::getId, Function.identity()));
        for (Long id : ids) {
            AgentCategoryDO category = categoryMap.get(id);
            if (category == null) {
                throw exception(AGENT_CATEGORY_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(category.getStatus())) {
                throw exception(AGENT_CATEGORY_DISABLED, category.getName());
            }
        }
    }

    private AgentCategoryDO validateCategoryExists(Long id) {
        AgentCategoryDO category = categoryMapper.selectById(id);
        if (category == null) {
            throw exception(AGENT_CATEGORY_NOT_EXISTS);
        }
        return category;
    }

    private void validateCategoryNameUnique(Long id, String name) {
        AgentCategoryDO category = categoryMapper.selectByName(name);
        if (category != null && !category.getId().equals(id)) {
            throw exception(AGENT_CATEGORY_NAME_EXISTS, name);
        }
    }

    private static String normalizeName(String name) {
        return name.trim();
    }

    private static Set<Long> normalizeIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
