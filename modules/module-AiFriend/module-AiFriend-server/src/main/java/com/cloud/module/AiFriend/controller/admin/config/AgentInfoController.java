package com.cloud.module.AiFriend.controller.admin.config;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.framework.common.util.object.BeanUtils;
import static com.cloud.framework.common.pojo.CommonResult.success;

import com.cloud.framework.excel.core.util.ExcelUtils;

import com.cloud.framework.apilog.core.annotation.ApiAccessLog;
import static com.cloud.framework.apilog.core.enums.OperateTypeEnum.*;

import com.cloud.module.AiFriend.controller.admin.config.vo.*;
import com.cloud.module.AiFriend.dal.dataobject.config.AgentInfoDO;
import com.cloud.module.AiFriend.dal.dataobject.config.ModelDO;
import com.cloud.module.AiFriend.dal.dataobject.config.SysPromptDO;
import com.cloud.module.AiFriend.service.config.AgentInfoService;

@Tag(name = "管理后台 - Agent 定义表")
@RestController
@RequestMapping("/AiFriend/agent-info")
@Validated
public class AgentInfoController {

    @Resource
    private AgentInfoService agentInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建Agent 定义表")
    @PreAuthorize("@ss.hasPermission('AiFriend:agent-info:create')")
    public CommonResult<Long> createAgentInfo(@Valid @RequestBody AgentInfoSaveReqVO createReqVO) {
        return success(agentInfoService.createAgentInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新Agent 定义表")
    @PreAuthorize("@ss.hasPermission('AiFriend:agent-info:update')")
    public CommonResult<Boolean> updateAgentInfo(@Valid @RequestBody AgentInfoSaveReqVO updateReqVO) {
        agentInfoService.updateAgentInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除Agent 定义表")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('AiFriend:agent-info:delete')")
    public CommonResult<Boolean> deleteAgentInfo(@RequestParam("id") Long id) {
        agentInfoService.deleteAgentInfo(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除Agent 定义表")
                @PreAuthorize("@ss.hasPermission('AiFriend:agent-info:delete')")
    public CommonResult<Boolean> deleteAgentInfoList(@RequestParam("ids") List<Long> ids) {
        agentInfoService.deleteAgentInfoListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得Agent 定义表")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('AiFriend:agent-info:query')")
    public CommonResult<AgentInfoRespVO> getAgentInfo(@RequestParam("id") Long id) {
        AgentInfoDO agentInfo = agentInfoService.getAgentInfo(id);
        return success(BeanUtils.toBean(agentInfo, AgentInfoRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得Agent 定义表分页")
    @PreAuthorize("@ss.hasPermission('AiFriend:agent-info:query')")
    public CommonResult<PageResult<AgentInfoRespVO>> getAgentInfoPage(@Valid AgentInfoPageReqVO pageReqVO) {
        PageResult<AgentInfoDO> pageResult = agentInfoService.getAgentInfoPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AgentInfoRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出Agent 定义表 Excel")
    @PreAuthorize("@ss.hasPermission('AiFriend:agent-info:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportAgentInfoExcel(@Valid AgentInfoPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AgentInfoDO> list = agentInfoService.getAgentInfoPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "Agent 定义表.xls", "数据", AgentInfoRespVO.class,
                        BeanUtils.toBean(list, AgentInfoRespVO.class));
    }

    // ==================== 子表（模型配置表） ====================

    @GetMapping("/model/get-by-id")
    @Operation(summary = "获得模型配置表")
    @Parameter(name = "id", description = "模型配置主键ID")
    @PreAuthorize("@ss.hasPermission('AiFriend:agent-info:query')")
    public CommonResult<ModelDO> getModelById(@RequestParam("id") Long id) {
        return success(agentInfoService.getModelById(id));
    }

    // ==================== 子表（系统提示词表） ====================

    @GetMapping("/sys-prompt/get-by-id")
    @Operation(summary = "获得系统提示词表")
    @Parameter(name = "id", description = "提示词模板主键ID")
    @PreAuthorize("@ss.hasPermission('AiFriend:agent-info:query')")
    public CommonResult<SysPromptDO> getSysPromptById(@RequestParam("id") Long id) {
        return success(agentInfoService.getSysPromptById(id));
    }

}