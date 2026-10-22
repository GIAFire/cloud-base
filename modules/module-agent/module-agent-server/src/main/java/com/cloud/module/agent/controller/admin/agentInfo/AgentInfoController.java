package com.cloud.module.agent.controller.admin.agentInfo;

import com.cloud.framework.security.core.LoginUser;
import com.cloud.framework.security.core.util.SecurityFrameworkUtils;
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

import java.nio.file.*;
import java.nio.file.Path;
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

import com.cloud.module.agent.controller.admin.agentInfo.vo.*;
import com.cloud.module.agent.dal.dataobject.agentInfo.AgentInfoDO;
import com.cloud.module.agent.service.agentInfo.AgentInfoService;

@Tag(name = "管理后台 - Agent 定义表")
@RestController
@RequestMapping("/agent/info")
@Validated
public class AgentInfoController {

    @Resource
    private AgentInfoService infoService;

    @GetMapping("/getPersonality")
    @Operation(summary = "创建Agent 人格")
    public CommonResult<Long> getPersonality(@RequestParam("id") Long id) throws IOException {

        AgentInfoDO info = infoService.getInfo(id);

        Long agentId = info.getId();
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        String personality = info.getPersonality();

        Path filePath = Paths.get(
                ".agentOS",
                "workspace",
                "agents",
                String.valueOf(agentId),
                "users",
                String.valueOf(loginUser.getId()),
                "AGENTS.md"
        );

        Files.createDirectories(filePath.getParent());

        try {
            Files.writeString(
                    filePath,
                    personality,
                    StandardOpenOption.CREATE_NEW
            );
        } catch (FileAlreadyExistsException ignored) {
            // AGENTS.md 已经存在，不覆盖
        }

        return success(agentId);
    }

    @PostMapping("/create")
    @Operation(summary = "创建Agent 定义表")
    @PreAuthorize("@ss.hasPermission('agent:info:create')")
    public CommonResult<Long> createInfo(@Valid @RequestBody AgentInfoSaveReqVO createReqVO) {
        return success(infoService.createInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新Agent 定义表")
    @PreAuthorize("@ss.hasPermission('agent:info:update')")
    public CommonResult<Boolean> updateInfo(@Valid @RequestBody AgentInfoSaveReqVO updateReqVO) {
        infoService.updateInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除Agent 定义表")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('agent:info:delete')")
    public CommonResult<Boolean> deleteInfo(@RequestParam("id") Long id) {
        infoService.deleteInfo(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除Agent 定义表")
                @PreAuthorize("@ss.hasPermission('agent:info:delete')")
    public CommonResult<Boolean> deleteInfoList(@RequestParam("ids") List<Long> ids) {
        infoService.deleteInfoListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得Agent 定义表")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('agent:info:query')")
    public CommonResult<AgentInfoRespVO> getInfo(@RequestParam("id") Long id) {
        AgentInfoDO info = infoService.getInfo(id);
        return success(BeanUtils.toBean(info, AgentInfoRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得Agent 定义表分页")
    @PreAuthorize("@ss.hasPermission('agent:info:query')")
    public CommonResult<PageResult<AgentInfoRespVO>> getInfoPage(@Valid AgentInfoPageReqVO pageReqVO) {
        PageResult<AgentInfoDO> pageResult = infoService.getInfoPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AgentInfoRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出Agent 定义表 Excel")
    @PreAuthorize("@ss.hasPermission('agent:info:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInfoExcel(@Valid AgentInfoPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AgentInfoDO> list = infoService.getInfoPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "Agent 定义表.xls", "数据", AgentInfoRespVO.class,
                        BeanUtils.toBean(list, AgentInfoRespVO.class));
    }

}
