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
import com.cloud.module.agent.dal.dataobject.config.ModelDO;
import com.cloud.module.agent.service.config.ModelService;

@Tag(name = "管理后台 - 模型配置表")
@RestController
@RequestMapping("/agent/model")
@Validated
public class ModelController {

    @Resource
    private ModelService modelService;

    @PostMapping("/create")
    @Operation(summary = "创建模型配置表")
    @PreAuthorize("@ss.hasPermission('agent:model:create')")
    public CommonResult<Long> createModel(@Valid @RequestBody ModelSaveReqVO createReqVO) {
        return success(modelService.createModel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新模型配置表")
    @PreAuthorize("@ss.hasPermission('agent:model:update')")
    public CommonResult<Boolean> updateModel(@Valid @RequestBody ModelSaveReqVO updateReqVO) {
        modelService.updateModel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除模型配置表")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('agent:model:delete')")
    public CommonResult<Boolean> deleteModel(@RequestParam("id") Long id) {
        modelService.deleteModel(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除模型配置表")
                @PreAuthorize("@ss.hasPermission('agent:model:delete')")
    public CommonResult<Boolean> deleteModelList(@RequestParam("ids") List<Long> ids) {
        modelService.deleteModelListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得模型配置表")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('agent:model:query')")
    public CommonResult<ModelRespVO> getModel(@RequestParam("id") Long id) {
        ModelDO model = modelService.getModel(id);
        return success(BeanUtils.toBean(model, ModelRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得模型配置表分页")
    @PreAuthorize("@ss.hasPermission('agent:model:query')")
    public CommonResult<PageResult<ModelRespVO>> getModelPage(@Valid ModelPageReqVO pageReqVO) {
        PageResult<ModelDO> pageResult = modelService.getModelPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ModelRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出模型配置表 Excel")
    @PreAuthorize("@ss.hasPermission('agent:model:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportModelExcel(@Valid ModelPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ModelDO> list = modelService.getModelPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "模型配置表.xls", "数据", ModelRespVO.class,
                        BeanUtils.toBean(list, ModelRespVO.class));
    }

}