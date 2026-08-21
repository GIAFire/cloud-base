package com.cloud.module.AiFriend.dal.mysql.config;

import java.util.*;

import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.AiFriend.dal.dataobject.config.SysPromptDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统提示词表 Mapper
 *
 * @author ZR
 */
@Mapper
public interface SysPromptMapper extends BaseMapperX<SysPromptDO> {

    default SysPromptDO selectById(Long id) {
        return selectOne(SysPromptDO::getId, id);
    }

    default int deleteById(Long id) {
        return delete(SysPromptDO::getId, id);
    }

	default int deleteByIds(List<Long> ids) {
	    return deleteBatch(SysPromptDO::getId, ids);
	}

}