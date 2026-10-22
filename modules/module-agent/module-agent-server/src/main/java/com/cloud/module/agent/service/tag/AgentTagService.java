package com.cloud.module.agent.service.tag;

import com.cloud.framework.common.pojo.PageResult;
import com.cloud.module.agent.controller.admin.tag.vo.AgentTagPageReqVO;
import com.cloud.module.agent.controller.admin.tag.vo.AgentTagSaveReqVO;
import com.cloud.module.agent.dal.dataobject.tag.AgentTagDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * Agent 标签 Service 接口。
 */
public interface AgentTagService {

    Long createTag(@Valid AgentTagSaveReqVO createReqVO);

    void updateTag(@Valid AgentTagSaveReqVO updateReqVO);

    void deleteTag(Long id);

    AgentTagDO getTag(Long id);

    List<AgentTagDO> getTagList(Collection<Long> ids);

    List<AgentTagDO> getEnabledTagList();

    PageResult<AgentTagDO> getTagPage(AgentTagPageReqVO pageReqVO);

    /**
     * 校验标签均存在且处于启用状态。租户插件确保只能读取当前租户的数据。
     */
    void validateTagsEnabled(Collection<Long> ids);

}
