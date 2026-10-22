package com.cloud.module.agent.dal.mysql.tag;

import com.cloud.framework.common.enums.CommonStatusEnum;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.mybatis.core.dataobject.BaseDO;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloud.module.agent.controller.admin.tag.vo.AgentTagPageReqVO;
import com.cloud.module.agent.dal.dataobject.tag.AgentTagDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentTagMapper extends BaseMapperX<AgentTagDO> {

    default PageResult<AgentTagDO> selectPage(AgentTagPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AgentTagDO>()
                .likeIfPresent(AgentTagDO::getName, reqVO.getName())
                .eqIfPresent(AgentTagDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(BaseDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(AgentTagDO::getSort)
                .orderByAsc(AgentTagDO::getId));
    }

    default AgentTagDO selectByName(String name) {
        return selectOne(AgentTagDO::getName, name);
    }

    default List<AgentTagDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<AgentTagDO>()
                .eq(AgentTagDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(AgentTagDO::getSort)
                .orderByAsc(AgentTagDO::getId));
    }

}
