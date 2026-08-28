package com.cloud.module.agent.dal.mysql.config;

import java.util.*;

import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.agent.dal.dataobject.config.ModelDO;
import org.apache.ibatis.annotations.Mapper;
import com.cloud.module.agent.controller.admin.config.vo.*;

/**
 * 模型配置表 Mapper
 *
 * @author ZR
 */
@Mapper
public interface ModelMapper extends BaseMapperX<ModelDO> {

    default List<ModelDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<ModelDO>()
                .select(ModelDO::getId, ModelDO::getModelName)
                .orderByAsc(ModelDO::getModelName));
    }

    default PageResult<ModelDO> selectPage(ModelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ModelDO>()
                .eqIfPresent(ModelDO::getProtocol, reqVO.getProtocol())
                .eqIfPresent(ModelDO::getBaseUrl, reqVO.getBaseUrl())
                .eqIfPresent(ModelDO::getApiKey, reqVO.getApiKey())
                .eqIfPresent(ModelDO::getDescription, reqVO.getDescription())
                .likeIfPresent(ModelDO::getModelName, reqVO.getModelName())
                .eqIfPresent(ModelDO::getStreaming, reqVO.getStreaming())
                .eqIfPresent(ModelDO::getThinking, reqVO.getThinking())
                .eqIfPresent(ModelDO::getTemperature, reqVO.getTemperature())
                .eqIfPresent(ModelDO::getTopP, reqVO.getTopP())
                .eqIfPresent(ModelDO::getMaxTokens, reqVO.getMaxTokens())
                .eqIfPresent(ModelDO::getThinkingBudget, reqVO.getThinkingBudget())
                .betweenIfPresent(ModelDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ModelDO::getId));
    }

}
