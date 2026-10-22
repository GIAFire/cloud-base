package com.cloud.module.agent.controller.admin.category;

import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.util.object.BeanUtils;
import com.cloud.module.agent.controller.admin.category.vo.AgentCategoryPageReqVO;
import com.cloud.module.agent.controller.admin.category.vo.AgentCategoryRespVO;
import com.cloud.module.agent.controller.admin.category.vo.AgentCategorySaveReqVO;
import com.cloud.module.agent.controller.admin.category.vo.AgentCategorySimpleRespVO;
import com.cloud.module.agent.dal.dataobject.category.AgentCategoryDO;
import com.cloud.module.agent.service.category.AgentCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - Agent 分类")
@RestController
@RequestMapping("/agent/category")
@Validated
public class AgentCategoryController {

    @Resource
    private AgentCategoryService categoryService;

    @PostMapping("/create")
    @Operation(summary = "创建 Agent 分类")
    @PreAuthorize("@ss.hasPermission('agent:category:create')")
    public CommonResult<Long> createCategory(@Valid @RequestBody AgentCategorySaveReqVO createReqVO) {
        return success(categoryService.createCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 Agent 分类")
    @PreAuthorize("@ss.hasPermission('agent:category:update')")
    public CommonResult<Boolean> updateCategory(@Valid @RequestBody AgentCategorySaveReqVO updateReqVO) {
        categoryService.updateCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 Agent 分类")
    @Parameter(name = "id", description = "分类编号", required = true)
    @PreAuthorize("@ss.hasPermission('agent:category:delete')")
    public CommonResult<Boolean> deleteCategory(@RequestParam("id") Long id) {
        categoryService.deleteCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 Agent 分类")
    @Parameter(name = "id", description = "分类编号", required = true)
    @PreAuthorize("@ss.hasPermission('agent:category:query')")
    public CommonResult<AgentCategoryRespVO> getCategory(@RequestParam("id") Long id) {
        AgentCategoryDO category = categoryService.getCategory(id);
        return success(BeanUtils.toBean(category, AgentCategoryRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用的 Agent 分类精简列表", description = "用于 Agent 配置的分类多选项")
    @PreAuthorize("@ss.hasAnyPermissions('agent:category:query','agent:info:query',"
            + "'agent:info:create','agent:info:update')")
    public CommonResult<List<AgentCategorySimpleRespVO>> getCategorySimpleList() {
        return success(BeanUtils.toBean(categoryService.getEnabledCategoryList(), AgentCategorySimpleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 Agent 分类分页")
    @PreAuthorize("@ss.hasPermission('agent:category:query')")
    public CommonResult<PageResult<AgentCategoryRespVO>> getCategoryPage(
            @Valid AgentCategoryPageReqVO pageReqVO) {
        PageResult<AgentCategoryDO> pageResult = categoryService.getCategoryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AgentCategoryRespVO.class));
    }

}
