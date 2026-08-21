package com.cloud.module.AiFriend.service.config;

import java.util.*;
import jakarta.validation.*;
import com.cloud.module.AiFriend.controller.admin.config.vo.*;
import com.cloud.module.AiFriend.dal.dataobject.config.AgentInfoDO;
import com.cloud.module.AiFriend.dal.dataobject.config.ModelDO;
import com.cloud.module.AiFriend.dal.dataobject.config.SysPromptDO;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.PageParam;

/**
 * Agent 定义表 Service 接口
 *
 * @author ZR
 */
public interface AgentInfoService {

    /**
     * 创建Agent 定义表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAgentInfo(@Valid AgentInfoSaveReqVO createReqVO);

    /**
     * 更新Agent 定义表
     *
     * @param updateReqVO 更新信息
     */
    void updateAgentInfo(@Valid AgentInfoSaveReqVO updateReqVO);

    /**
     * 删除Agent 定义表
     *
     * @param id 编号
     */
    void deleteAgentInfo(Long id);

    /**
    * 批量删除Agent 定义表
    *
    * @param ids 编号
    */
    void deleteAgentInfoListByIds(List<Long> ids);

    /**
     * 获得Agent 定义表
     *
     * @param id 编号
     * @return Agent 定义表
     */
    AgentInfoDO getAgentInfo(Long id);

    /**
     * 获得Agent 定义表分页
     *
     * @param pageReqVO 分页查询
     * @return Agent 定义表分页
     */
    PageResult<AgentInfoDO> getAgentInfoPage(AgentInfoPageReqVO pageReqVO);

    // ==================== 子表（模型配置表） ====================

    /**
     * 获得模型配置表
     *
     * @param id 模型配置主键ID
     * @return 模型配置表
     */
    ModelDO getModelById(Long id);

    // ==================== 子表（系统提示词表） ====================

    /**
     * 获得系统提示词表
     *
     * @param id 提示词模板主键ID
     * @return 系统提示词表
     */
    SysPromptDO getSysPromptById(Long id);

}