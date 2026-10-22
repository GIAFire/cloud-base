package com.cloud.module.agent.service.tag;

import com.cloud.framework.common.enums.CommonStatusEnum;
import com.cloud.framework.common.exception.ServiceException;
import com.cloud.module.agent.controller.admin.tag.vo.AgentTagSaveReqVO;
import com.cloud.module.agent.dal.dataobject.tag.AgentTagDO;
import com.cloud.module.agent.dal.mysql.relation.AgentTagAgentRelationMapper;
import com.cloud.module.agent.dal.mysql.tag.AgentTagMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_TAG_DISABLED;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_TAG_IN_USE;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_TAG_NAME_EXISTS;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_TAG_NOT_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentTagServiceImplTest {

    @Mock
    private AgentTagMapper tagMapper;

    @Mock
    private AgentTagAgentRelationMapper tagAgentRelationMapper;

    @InjectMocks
    private AgentTagServiceImpl tagService;

    @Test
    void creates_tag_with_trimmed_name() {
        AgentTagSaveReqVO request = request(null, "  数据分析  ", 10, CommonStatusEnum.ENABLE.getStatus());
        when(tagMapper.insert(any(AgentTagDO.class))).thenAnswer(invocation -> {
            AgentTagDO tag = invocation.getArgument(0);
            tag.setId(100L);
            return 1;
        });

        Long id = tagService.createTag(request);

        assertThat(id).isEqualTo(100L);
        ArgumentCaptor<AgentTagDO> captor = ArgumentCaptor.forClass(AgentTagDO.class);
        verify(tagMapper).insert(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("数据分析");
        assertThat(captor.getValue().getSort()).isEqualTo(10);
    }

    @Test
    void rejects_duplicate_tag_name() {
        when(tagMapper.selectByName("数据分析"))
                .thenReturn(tag(100L, "数据分析", CommonStatusEnum.ENABLE.getStatus()));

        assertServiceException(
                () -> tagService.createTag(request(null, "数据分析", 0, 0)),
                AGENT_TAG_NAME_EXISTS.getCode());

        verify(tagMapper, never()).insert(any(AgentTagDO.class));
    }

    @Test
    void rejects_updating_missing_tag() {
        when(tagMapper.selectById(100L)).thenReturn(null);

        assertServiceException(
                () -> tagService.updateTag(request(100L, "中文", 0, 0)),
                AGENT_TAG_NOT_EXISTS.getCode());

        verify(tagMapper, never()).updateById(any(AgentTagDO.class));
    }

    @Test
    void rejects_getting_missing_tag() {
        when(tagMapper.selectById(100L)).thenReturn(null);

        assertServiceException(() -> tagService.getTag(100L), AGENT_TAG_NOT_EXISTS.getCode());
    }

    @Test
    void rejects_deleting_tag_in_use() {
        when(tagMapper.selectById(100L))
                .thenReturn(tag(100L, "数据分析", CommonStatusEnum.ENABLE.getStatus()));
        when(tagAgentRelationMapper.countByTagId(100L)).thenReturn(1L);

        assertServiceException(() -> tagService.deleteTag(100L), AGENT_TAG_IN_USE.getCode());

        verify(tagMapper, never()).deleteById(100L);
    }

    @Test
    void deletes_unused_tag() {
        when(tagMapper.selectById(100L))
                .thenReturn(tag(100L, "数据分析", CommonStatusEnum.ENABLE.getStatus()));
        when(tagAgentRelationMapper.countByTagId(100L)).thenReturn(0L);

        tagService.deleteTag(100L);

        verify(tagMapper).deleteById(100L);
    }

    @Test
    void accepts_empty_tag_selection_without_query() {
        tagService.validateTagsEnabled(List.of());

        verifyNoInteractions(tagMapper, tagAgentRelationMapper);
    }

    @Test
    void rejects_missing_or_cross_tenant_tag() {
        when(tagMapper.selectByIds(anyCollection()))
                .thenReturn(List.of(tag(100L, "数据分析", CommonStatusEnum.ENABLE.getStatus())));

        assertServiceException(
                () -> tagService.validateTagsEnabled(List.of(100L, 200L)),
                AGENT_TAG_NOT_EXISTS.getCode());
    }

    @Test
    void rejects_disabled_tag() {
        when(tagMapper.selectByIds(anyCollection()))
                .thenReturn(List.of(tag(100L, "数据分析", CommonStatusEnum.DISABLE.getStatus())));

        assertServiceException(
                () -> tagService.validateTagsEnabled(List.of(100L)),
                AGENT_TAG_DISABLED.getCode());
    }

    @Test
    void accepts_enabled_tags() {
        when(tagMapper.selectByIds(anyCollection())).thenReturn(List.of(
                tag(100L, "数据分析", CommonStatusEnum.ENABLE.getStatus()),
                tag(200L, "中文", CommonStatusEnum.ENABLE.getStatus())));

        tagService.validateTagsEnabled(List.of(100L, 200L, 100L));

        verify(tagMapper).selectByIds(anyCollection());
    }

    private static AgentTagSaveReqVO request(Long id, String name, Integer sort, Integer status) {
        AgentTagSaveReqVO request = new AgentTagSaveReqVO();
        request.setId(id);
        request.setName(name);
        request.setSort(sort);
        request.setStatus(status);
        return request;
    }

    private static AgentTagDO tag(Long id, String name, Integer status) {
        AgentTagDO tag = new AgentTagDO();
        tag.setId(id);
        tag.setName(name);
        tag.setStatus(status);
        return tag;
    }

    private static void assertServiceException(Runnable action, int expectedCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(ServiceException.class)
                .satisfies(error -> assertThat(((ServiceException) error).getCode()).isEqualTo(expectedCode));
    }

}
