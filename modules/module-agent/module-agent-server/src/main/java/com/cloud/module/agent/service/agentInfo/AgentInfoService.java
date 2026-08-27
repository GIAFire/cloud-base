package com.cloud.module.agent.service.agentInfo;

import java.util.*;
import jakarta.validation.*;
import com.cloud.module.agent.controller.admin.agentInfo.vo.*;
import com.cloud.module.agent.dal.dataobject.agentInfo.AgentInfoDO;
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
    Long createInfo(@Valid AgentInfoSaveReqVO createReqVO);

    /**
     * 更新Agent 定义表
     *
     * @param updateReqVO 更新信息
     */
    void updateInfo(@Valid AgentInfoSaveReqVO updateReqVO);

    /**
     * 删除Agent 定义表
     *
     * @param id 编号
     */
    void deleteInfo(Long id);

    /**
    * 批量删除Agent 定义表
    *
    * @param ids 编号
    */
    void deleteInfoListByIds(List<Long> ids);

    /**
     * 获得Agent 定义表
     *
     * @param id 编号
     * @return Agent 定义表
     */
    AgentInfoDO getInfo(Long id);

    /**
     * 获得Agent 定义表分页
     *
     * @param pageReqVO 分页查询
     * @return Agent 定义表分页
     */
    PageResult<AgentInfoDO> getInfoPage(AgentInfoPageReqVO pageReqVO);

}