package com.cloud.module.agent.service.category;

import com.cloud.framework.common.enums.CommonStatusEnum;
import com.cloud.framework.common.exception.ServiceException;
import com.cloud.module.agent.controller.admin.category.vo.AgentCategorySaveReqVO;
import com.cloud.module.agent.dal.dataobject.category.AgentCategoryDO;
import com.cloud.module.agent.dal.mysql.category.AgentCategoryMapper;
import com.cloud.module.agent.dal.mysql.relation.AgentCategoryAgentRelationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_CATEGORY_DISABLED;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_CATEGORY_IN_USE;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_CATEGORY_NAME_EXISTS;
import static com.cloud.module.agent.enums.ErrorCodeConstants.AGENT_CATEGORY_NOT_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentCategoryServiceImplTest {

    @Mock
    private AgentCategoryMapper categoryMapper;

    @Mock
    private AgentCategoryAgentRelationMapper categoryAgentRelationMapper;

    @InjectMocks
    private AgentCategoryServiceImpl categoryService;

    @Test
    void creates_category_with_trimmed_name() {
        AgentCategorySaveReqVO request = request(null, "  内容创作  ", 10, CommonStatusEnum.ENABLE.getStatus());
        when(categoryMapper.insert(any(AgentCategoryDO.class))).thenAnswer(invocation -> {
            AgentCategoryDO category = invocation.getArgument(0);
            category.setId(100L);
            return 1;
        });

        Long id = categoryService.createCategory(request);

        assertThat(id).isEqualTo(100L);
        ArgumentCaptor<AgentCategoryDO> captor = ArgumentCaptor.forClass(AgentCategoryDO.class);
        verify(categoryMapper).insert(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("内容创作");
        assertThat(captor.getValue().getSort()).isEqualTo(10);
    }

    @Test
    void rejects_duplicate_category_name() {
        when(categoryMapper.selectByName("内容创作"))
                .thenReturn(category(100L, "内容创作", CommonStatusEnum.ENABLE.getStatus()));

        assertServiceException(
                () -> categoryService.createCategory(request(null, "内容创作", 0, 0)),
                AGENT_CATEGORY_NAME_EXISTS.getCode());

        verify(categoryMapper, never()).insert(any(AgentCategoryDO.class));
    }

    @Test
    void rejects_updating_missing_category() {
        when(categoryMapper.selectById(100L)).thenReturn(null);

        assertServiceException(
                () -> categoryService.updateCategory(request(100L, "办公效率", 0, 0)),
                AGENT_CATEGORY_NOT_EXISTS.getCode());

        verify(categoryMapper, never()).updateById(any(AgentCategoryDO.class));
    }

    @Test
    void rejects_getting_missing_category() {
        when(categoryMapper.selectById(100L)).thenReturn(null);

        assertServiceException(() -> categoryService.getCategory(100L), AGENT_CATEGORY_NOT_EXISTS.getCode());
    }

    @Test
    void rejects_deleting_category_in_use() {
        when(categoryMapper.selectById(100L))
                .thenReturn(category(100L, "内容创作", CommonStatusEnum.ENABLE.getStatus()));
        when(categoryAgentRelationMapper.countByCategoryId(100L)).thenReturn(1L);

        assertServiceException(() -> categoryService.deleteCategory(100L), AGENT_CATEGORY_IN_USE.getCode());

        verify(categoryMapper, never()).deleteById(100L);
    }

    @Test
    void deletes_unused_category() {
        when(categoryMapper.selectById(100L))
                .thenReturn(category(100L, "内容创作", CommonStatusEnum.ENABLE.getStatus()));
        when(categoryAgentRelationMapper.countByCategoryId(100L)).thenReturn(0L);

        categoryService.deleteCategory(100L);

        verify(categoryMapper).deleteById(100L);
    }

    @Test
    void accepts_empty_category_selection_without_query() {
        categoryService.validateCategoriesEnabled(List.of());

        verifyNoInteractions(categoryMapper, categoryAgentRelationMapper);
    }

    @Test
    void rejects_missing_or_cross_tenant_category() {
        when(categoryMapper.selectByIds(anyCollection()))
                .thenReturn(List.of(category(100L, "内容创作", CommonStatusEnum.ENABLE.getStatus())));

        assertServiceException(
                () -> categoryService.validateCategoriesEnabled(List.of(100L, 200L)),
                AGENT_CATEGORY_NOT_EXISTS.getCode());
    }

    @Test
    void rejects_disabled_category() {
        when(categoryMapper.selectByIds(anyCollection()))
                .thenReturn(List.of(category(100L, "内容创作", CommonStatusEnum.DISABLE.getStatus())));

        assertServiceException(
                () -> categoryService.validateCategoriesEnabled(List.of(100L)),
                AGENT_CATEGORY_DISABLED.getCode());
    }

    @Test
    void accepts_enabled_categories() {
        when(categoryMapper.selectByIds(anyCollection())).thenReturn(List.of(
                category(100L, "内容创作", CommonStatusEnum.ENABLE.getStatus()),
                category(200L, "办公效率", CommonStatusEnum.ENABLE.getStatus())));

        categoryService.validateCategoriesEnabled(List.of(100L, 200L, 100L));

        verify(categoryMapper).selectByIds(anyCollection());
    }

    private static AgentCategorySaveReqVO request(Long id, String name, Integer sort, Integer status) {
        AgentCategorySaveReqVO request = new AgentCategorySaveReqVO();
        request.setId(id);
        request.setName(name);
        request.setSort(sort);
        request.setStatus(status);
        return request;
    }

    private static AgentCategoryDO category(Long id, String name, Integer status) {
        AgentCategoryDO category = new AgentCategoryDO();
        category.setId(id);
        category.setName(name);
        category.setStatus(status);
        return category;
    }

    private static void assertServiceException(Runnable action, int expectedCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(ServiceException.class)
                .satisfies(error -> assertThat(((ServiceException) error).getCode()).isEqualTo(expectedCode));
    }

}
