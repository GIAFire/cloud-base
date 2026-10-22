package com.cloud.module.agent.controller.admin.tag;

import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.util.object.BeanUtils;
import com.cloud.module.agent.controller.admin.tag.vo.AgentTagPageReqVO;
import com.cloud.module.agent.controller.admin.tag.vo.AgentTagRespVO;
import com.cloud.module.agent.controller.admin.tag.vo.AgentTagSaveReqVO;
import com.cloud.module.agent.controller.admin.tag.vo.AgentTagSimpleRespVO;
import com.cloud.module.agent.dal.dataobject.tag.AgentTagDO;
import com.cloud.module.agent.service.tag.AgentTagService;
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

@Tag(name = "管理后台 - Agent 标签")
@RestController
@RequestMapping("/agent/tag")
@Validated
public class AgentTagController {

    @Resource
    private AgentTagService tagService;

    @PostMapping("/create")
    @Operation(summary = "创建 Agent 标签")
    @PreAuthorize("@ss.hasPermission('agent:tag:create')")
    public CommonResult<Long> createTag(@Valid @RequestBody AgentTagSaveReqVO createReqVO) {
        return success(tagService.createTag(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 Agent 标签")
    @PreAuthorize("@ss.hasPermission('agent:tag:update')")
    public CommonResult<Boolean> updateTag(@Valid @RequestBody AgentTagSaveReqVO updateReqVO) {
        tagService.updateTag(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 Agent 标签")
    @Parameter(name = "id", description = "标签编号", required = true)
    @PreAuthorize("@ss.hasPermission('agent:tag:delete')")
    public CommonResult<Boolean> deleteTag(@RequestParam("id") Long id) {
        tagService.deleteTag(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 Agent 标签")
    @Parameter(name = "id", description = "标签编号", required = true)
    @PreAuthorize("@ss.hasPermission('agent:tag:query')")
    public CommonResult<AgentTagRespVO> getTag(@RequestParam("id") Long id) {
        AgentTagDO tag = tagService.getTag(id);
        return success(BeanUtils.toBean(tag, AgentTagRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用的 Agent 标签精简列表", description = "用于 Agent 配置的标签多选项")
    @PreAuthorize("@ss.hasAnyPermissions('agent:tag:query','agent:info:query',"
            + "'agent:info:create','agent:info:update')")
    public CommonResult<List<AgentTagSimpleRespVO>> getTagSimpleList() {
        return success(BeanUtils.toBean(tagService.getEnabledTagList(), AgentTagSimpleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 Agent 标签分页")
    @PreAuthorize("@ss.hasPermission('agent:tag:query')")
    public CommonResult<PageResult<AgentTagRespVO>> getTagPage(@Valid AgentTagPageReqVO pageReqVO) {
        PageResult<AgentTagDO> pageResult = tagService.getTagPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AgentTagRespVO.class));
    }

}
