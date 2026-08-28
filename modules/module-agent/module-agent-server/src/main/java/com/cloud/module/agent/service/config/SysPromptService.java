package com.cloud.module.agent.service.config;

import java.util.*;
import jakarta.validation.*;
import com.cloud.module.agent.controller.admin.config.vo.*;
import com.cloud.module.agent.dal.dataobject.config.SysPromptDO;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.PageParam;

/**
 * 系统提示词表 Service 接口
 *
 * @author ZR
 */
public interface SysPromptService {

    /**
     * 创建系统提示词表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSysPrompt(@Valid SysPromptSaveReqVO createReqVO);

    /**
     * 更新系统提示词表
     *
     * @param updateReqVO 更新信息
     */
    void updateSysPrompt(@Valid SysPromptSaveReqVO updateReqVO);

    /**
     * 删除系统提示词表
     *
     * @param id 编号
     */
    void deleteSysPrompt(Long id);

    /**
    * 批量删除系统提示词表
    *
    * @param ids 编号
    */
    void deleteSysPromptListByIds(List<Long> ids);

    /**
     * 获得系统提示词表
     *
     * @param id 编号
     * @return 系统提示词表
     */
    SysPromptDO getSysPrompt(Long id);

    /**
     * 获得系统提示词精简列表
     *
     * @return 系统提示词列表
     */
    List<SysPromptDO> getSysPromptList();

    /**
     * 获得系统提示词表分页
     *
     * @param pageReqVO 分页查询
     * @return 系统提示词表分页
     */
    PageResult<SysPromptDO> getSysPromptPage(SysPromptPageReqVO pageReqVO);

}
