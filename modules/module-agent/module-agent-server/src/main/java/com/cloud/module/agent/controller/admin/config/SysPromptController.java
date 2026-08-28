package com.cloud.module.agent.controller.admin.config;

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

import com.cloud.module.agent.controller.admin.config.vo.*;
import com.cloud.module.agent.dal.dataobject.config.SysPromptDO;
import com.cloud.module.agent.service.config.SysPromptService;

@Tag(name = "管理后台 - 系统提示词表")
@RestController
@RequestMapping("/agent/sys-prompt")
@Validated
public class SysPromptController {

    @Resource
    private SysPromptService sysPromptService;

    @PostMapping("/create")
    @Operation(summary = "创建系统提示词表")
    @PreAuthorize("@ss.hasPermission('agent:sys-prompt:create')")
    public CommonResult<Long> createSysPrompt(@Valid @RequestBody SysPromptSaveReqVO createReqVO) {
        return success(sysPromptService.createSysPrompt(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新系统提示词表")
    @PreAuthorize("@ss.hasPermission('agent:sys-prompt:update')")
    public CommonResult<Boolean> updateSysPrompt(@Valid @RequestBody SysPromptSaveReqVO updateReqVO) {
        sysPromptService.updateSysPrompt(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除系统提示词表")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('agent:sys-prompt:delete')")
    public CommonResult<Boolean> deleteSysPrompt(@RequestParam("id") Long id) {
        sysPromptService.deleteSysPrompt(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除系统提示词表")
                @PreAuthorize("@ss.hasPermission('agent:sys-prompt:delete')")
    public CommonResult<Boolean> deleteSysPromptList(@RequestParam("ids") List<Long> ids) {
        sysPromptService.deleteSysPromptListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得系统提示词表")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('agent:sys-prompt:query')")
    public CommonResult<SysPromptRespVO> getSysPrompt(@RequestParam("id") Long id) {
        SysPromptDO sysPrompt = sysPromptService.getSysPrompt(id);
        return success(BeanUtils.toBean(sysPrompt, SysPromptRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得系统提示词精简列表", description = "用于 Agent 配置的提示词下拉选项")
    public CommonResult<List<SysPromptSimpleRespVO>> getSysPromptSimpleList() {
        return success(BeanUtils.toBean(sysPromptService.getSysPromptList(), SysPromptSimpleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得系统提示词表分页")
    @PreAuthorize("@ss.hasPermission('agent:sys-prompt:query')")
    public CommonResult<PageResult<SysPromptRespVO>> getSysPromptPage(@Valid SysPromptPageReqVO pageReqVO) {
        PageResult<SysPromptDO> pageResult = sysPromptService.getSysPromptPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SysPromptRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出系统提示词表 Excel")
    @PreAuthorize("@ss.hasPermission('agent:sys-prompt:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSysPromptExcel(@Valid SysPromptPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SysPromptDO> list = sysPromptService.getSysPromptPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "系统提示词表.xls", "数据", SysPromptRespVO.class,
                        BeanUtils.toBean(list, SysPromptRespVO.class));
    }

}
