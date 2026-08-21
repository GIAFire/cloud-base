package com.cloud.module.AiFriend.dal.mysql.config;

import java.util.*;

import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.AiFriend.dal.dataobject.config.ModelDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型配置表 Mapper
 *
 * @author ZR
 */
@Mapper
public interface ModelMapper extends BaseMapperX<ModelDO> {

    default ModelDO selectById(Long id) {
        return selectOne(ModelDO::getId, id);
    }

    default int deleteById(Long id) {
        return delete(ModelDO::getId, id);
    }

	default int deleteByIds(List<Long> ids) {
	    return deleteBatch(ModelDO::getId, ids);
	}

}