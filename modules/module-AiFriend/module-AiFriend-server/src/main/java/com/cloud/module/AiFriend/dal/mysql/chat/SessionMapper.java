package com.cloud.module.AiFriend.dal.mysql.chat;

import java.util.*;

import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.AiFriend.dal.dataobject.chat.SessionDO;
import org.apache.ibatis.annotations.Mapper;
import com.cloud.module.AiFriend.controller.admin.chat.vo.*;

/**
 * Agent会话表：保存用户和Agent之间的一段连续对话 Mapper
 *
 * @author ZR
 */
@Mapper
public interface SessionMapper extends BaseMapperX<SessionDO> {

    default PageResult<SessionDO> selectPage(SessionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SessionDO>()
                .eqIfPresent(SessionDO::getAgentId, reqVO.getAgentId())
                .eqIfPresent(SessionDO::getUserId, reqVO.getUserId())
                .likeIfPresent(SessionDO::getTitle, reqVO.getTitle())
                .betweenIfPresent(SessionDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SessionDO::getId));
    }

}