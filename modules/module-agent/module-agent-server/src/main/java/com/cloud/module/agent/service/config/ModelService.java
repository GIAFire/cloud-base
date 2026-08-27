package com.cloud.module.agent.service.config;

import java.util.*;
import jakarta.validation.*;
import com.cloud.module.agent.controller.admin.config.vo.*;
import com.cloud.module.agent.dal.dataobject.config.ModelDO;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.PageParam;

/**
 * 模型配置表 Service 接口
 *
 * @author ZR
 */
public interface ModelService {

    /**
     * 创建模型配置表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createModel(@Valid ModelSaveReqVO createReqVO);

    /**
     * 更新模型配置表
     *
     * @param updateReqVO 更新信息
     */
    void updateModel(@Valid ModelSaveReqVO updateReqVO);

    /**
     * 删除模型配置表
     *
     * @param id 编号
     */
    void deleteModel(Long id);

    /**
    * 批量删除模型配置表
    *
    * @param ids 编号
    */
    void deleteModelListByIds(List<Long> ids);

    /**
     * 获得模型配置表
     *
     * @param id 编号
     * @return 模型配置表
     */
    ModelDO getModel(Long id);

    /**
     * 获得模型配置表分页
     *
     * @param pageReqVO 分页查询
     * @return 模型配置表分页
     */
    PageResult<ModelDO> getModelPage(ModelPageReqVO pageReqVO);

}