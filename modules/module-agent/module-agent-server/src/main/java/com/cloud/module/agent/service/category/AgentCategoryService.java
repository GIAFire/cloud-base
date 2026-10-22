package com.cloud.module.agent.service.category;

import com.cloud.framework.common.pojo.PageResult;
import com.cloud.module.agent.controller.admin.category.vo.AgentCategoryPageReqVO;
import com.cloud.module.agent.controller.admin.category.vo.AgentCategorySaveReqVO;
import com.cloud.module.agent.dal.dataobject.category.AgentCategoryDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * Agent 分类 Service 接口。
 */
public interface AgentCategoryService {

    Long createCategory(@Valid AgentCategorySaveReqVO createReqVO);

    void updateCategory(@Valid AgentCategorySaveReqVO updateReqVO);

    void deleteCategory(Long id);

    AgentCategoryDO getCategory(Long id);

    List<AgentCategoryDO> getCategoryList(Collection<Long> ids);

    List<AgentCategoryDO> getEnabledCategoryList();

    PageResult<AgentCategoryDO> getCategoryPage(AgentCategoryPageReqVO pageReqVO);

    /**
     * 校验分类均存在且处于启用状态。租户插件确保只能读取当前租户的数据。
     */
    void validateCategoriesEnabled(Collection<Long> ids);

}
