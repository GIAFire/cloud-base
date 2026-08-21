package com.cloud.module.AiFriend.controller.admin.chat;

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

import com.cloud.module.AiFriend.controller.admin.chat.vo.*;
import com.cloud.module.AiFriend.dal.dataobject.chat.SessionDO;
import com.cloud.module.AiFriend.dal.dataobject.chat.MessageDO;
import com.cloud.module.AiFriend.service.chat.SessionService;

@Tag(name = "管理后台 - Agent会话表：保存用户和Agent之间的一段连续对话")
@RestController
@RequestMapping("/AiFriend/session")
@Validated
public class SessionController {

    @Resource
    private SessionService sessionService;

    @PostMapping("/create")
    @Operation(summary = "创建Agent会话表：保存用户和Agent之间的一段连续对话")
    @PreAuthorize("@ss.hasPermission('AiFriend:session:create')")
    public CommonResult<Long> createSession(@Valid @RequestBody SessionSaveReqVO createReqVO) {
        return success(sessionService.createSession(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新Agent会话表：保存用户和Agent之间的一段连续对话")
    @PreAuthorize("@ss.hasPermission('AiFriend:session:update')")
    public CommonResult<Boolean> updateSession(@Valid @RequestBody SessionSaveReqVO updateReqVO) {
        sessionService.updateSession(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除Agent会话表：保存用户和Agent之间的一段连续对话")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('AiFriend:session:delete')")
    public CommonResult<Boolean> deleteSession(@RequestParam("id") Long id) {
        sessionService.deleteSession(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除Agent会话表：保存用户和Agent之间的一段连续对话")
                @PreAuthorize("@ss.hasPermission('AiFriend:session:delete')")
    public CommonResult<Boolean> deleteSessionList(@RequestParam("ids") List<Long> ids) {
        sessionService.deleteSessionListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得Agent会话表：保存用户和Agent之间的一段连续对话")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('AiFriend:session:query')")
    public CommonResult<SessionRespVO> getSession(@RequestParam("id") Long id) {
        SessionDO session = sessionService.getSession(id);
        return success(BeanUtils.toBean(session, SessionRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得Agent会话表：保存用户和Agent之间的一段连续对话分页")
    @PreAuthorize("@ss.hasPermission('AiFriend:session:query')")
    public CommonResult<PageResult<SessionRespVO>> getSessionPage(@Valid SessionPageReqVO pageReqVO) {
        PageResult<SessionDO> pageResult = sessionService.getSessionPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SessionRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出Agent会话表：保存用户和Agent之间的一段连续对话 Excel")
    @PreAuthorize("@ss.hasPermission('AiFriend:session:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSessionExcel(@Valid SessionPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SessionDO> list = sessionService.getSessionPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "Agent会话表：保存用户和Agent之间的一段连续对话.xls", "数据", SessionRespVO.class,
                        BeanUtils.toBean(list, SessionRespVO.class));
    }

    // ==================== 子表（Agent消息表：保存输入、思考、正文等消息） ====================

    @GetMapping("/message/list-by-session-id")
    @Operation(summary = "获得Agent消息表：保存输入、思考、正文等消息列表")
    @Parameter(name = "sessionId", description = "会话ID，关联ai_agent_session.id")
    @PreAuthorize("@ss.hasPermission('AiFriend:session:query')")
    public CommonResult<List<MessageDO>> getMessageListBySessionId(@RequestParam("sessionId") Long sessionId) {
        return success(sessionService.getMessageListBySessionId(sessionId));
    }

}