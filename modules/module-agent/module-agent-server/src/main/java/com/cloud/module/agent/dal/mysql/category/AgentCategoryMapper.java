package com.cloud.module.agent.dal.mysql.category;

import com.cloud.framework.common.enums.CommonStatusEnum;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.mybatis.core.dataobject.BaseDO;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloud.module.agent.controller.admin.category.vo.AgentCategoryPageReqVO;
import com.cloud.module.agent.dal.dataobject.category.AgentCategoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentCategoryMapper extends BaseMapperX<AgentCategoryDO> {

    default PageResult<AgentCategoryDO> selectPage(AgentCategoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AgentCategoryDO>()
                .likeIfPresent(AgentCategoryDO::getName, reqVO.getName())
                .eqIfPresent(AgentCategoryDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(BaseDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(AgentCategoryDO::getSort)
                .orderByAsc(AgentCategoryDO::getId));
    }

    default AgentCategoryDO selectByName(String name) {
        return selectOne(AgentCategoryDO::getName, name);
    }

    default List<AgentCategoryDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<AgentCategoryDO>()
                .eq(AgentCategoryDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(AgentCategoryDO::getSort)
                .orderByAsc(AgentCategoryDO::getId));
    }

}
