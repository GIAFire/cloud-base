package com.cloud.module.agent.dal.mysql.config;

import java.util.*;

import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agent.dal.dataobject.config.SysPromptDO;
import org.apache.ibatis.annotations.Mapper;
import com.cloud.module.agent.controller.admin.config.vo.*;

/**
 * 系统提示词表 Mapper
 *
 * @author ZR
 */
@Mapper
public interface SysPromptMapper extends BaseMapperX<SysPromptDO> {

    default List<SysPromptDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<SysPromptDO>()
                .select(SysPromptDO::getId, SysPromptDO::getPromptName)
                .orderByAsc(SysPromptDO::getPromptName));
    }

    default PageResult<SysPromptDO> selectPage(SysPromptPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SysPromptDO>()
                .likeIfPresent(SysPromptDO::getPromptName, reqVO.getPromptName())
                .eqIfPresent(SysPromptDO::getDescription, reqVO.getDescription())
                .eqIfPresent(SysPromptDO::getSysPrompt, reqVO.getSysPrompt())
                .betweenIfPresent(SysPromptDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SysPromptDO::getId));
    }

}
