package com.cloud.module.agent.service.tag;

import com.cloud.framework.common.enums.CommonStatusEnum;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.util.object.BeanUtils;
import com.cloud.module.agent.controller.admin.tag.vo.AgentTagPageReqVO;
import com.cloud.module.agent.controller.admin.tag.vo.AgentTagSaveReqVO;
import com.cloud.module.agent.dal.dataobject.tag.AgentTagDO;
import com.cloud.module.agent.dal.mysql.relation.AgentTagAgentRelationMapper;
import com.cloud.module.agent.dal.mysql.tag.AgentTagMapper;
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
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_TAG_DISABLED;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_TAG_IN_USE;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_TAG_NAME_EXISTS;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_TAG_NOT_EXISTS;

/**
 * Agent 标签 Service 实现类。
 */
@Service
@Validated
public class AgentTagServiceImpl implements AgentTagService {

    @Resource
    private AgentTagMapper tagMapper;

    @Resource
    private AgentTagAgentRelationMapper tagAgentRelationMapper;

    @Override
    public Long createTag(AgentTagSaveReqVO createReqVO) {
        String name = normalizeName(createReqVO.getName());
        validateTagNameUnique(null, name);
        AgentTagDO tag = BeanUtils.toBean(createReqVO, AgentTagDO.class);
        tag.setName(name);
        try {
            tagMapper.insert(tag);
        } catch (DuplicateKeyException ex) {
            throw exception(AGENT_TAG_NAME_EXISTS, name);
        }
        return tag.getId();
    }

    @Override
    public void updateTag(AgentTagSaveReqVO updateReqVO) {
        validateTagExists(updateReqVO.getId());
        String name = normalizeName(updateReqVO.getName());
        validateTagNameUnique(updateReqVO.getId(), name);
        AgentTagDO updateObj = BeanUtils.toBean(updateReqVO, AgentTagDO.class);
        updateObj.setName(name);
        try {
            tagMapper.updateById(updateObj);
        } catch (DuplicateKeyException ex) {
            throw exception(AGENT_TAG_NAME_EXISTS, name);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        validateTagExists(id);
        if (tagAgentRelationMapper.countByTagId(id) > 0) {
            throw exception(AGENT_TAG_IN_USE);
        }
        tagMapper.deleteById(id);
    }

    @Override
    public AgentTagDO getTag(Long id) {
        return validateTagExists(id);
    }

    @Override
    public List<AgentTagDO> getTagList(Collection<Long> ids) {
        Set<Long> normalizedIds = normalizeIds(ids);
        return normalizedIds.isEmpty() ? Collections.emptyList() : tagMapper.selectByIds(normalizedIds);
    }

    @Override
    public List<AgentTagDO> getEnabledTagList() {
        return tagMapper.selectEnabledList();
    }

    @Override
    public PageResult<AgentTagDO> getTagPage(AgentTagPageReqVO pageReqVO) {
        return tagMapper.selectPage(pageReqVO);
    }

    @Override
    public void validateTagsEnabled(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Map<Long, AgentTagDO> tagMap = getTagList(ids).stream()
                .collect(Collectors.toMap(AgentTagDO::getId, Function.identity()));
        for (Long id : ids) {
            AgentTagDO tag = tagMap.get(id);
            if (tag == null) {
                throw exception(AGENT_TAG_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(tag.getStatus())) {
                throw exception(AGENT_TAG_DISABLED, tag.getName());
            }
        }
    }

    private AgentTagDO validateTagExists(Long id) {
        AgentTagDO tag = tagMapper.selectById(id);
        if (tag == null) {
            throw exception(AGENT_TAG_NOT_EXISTS);
        }
        return tag;
    }

    private void validateTagNameUnique(Long id, String name) {
        AgentTagDO tag = tagMapper.selectByName(name);
        if (tag != null && !tag.getId().equals(id)) {
            throw exception(AGENT_TAG_NAME_EXISTS, name);
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
