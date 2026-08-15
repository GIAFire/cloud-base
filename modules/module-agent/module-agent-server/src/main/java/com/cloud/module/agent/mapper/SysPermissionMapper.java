package com.cloud.module.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.module.agent.entity.SysPermissionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 权限表（菜单/接口） Mapper 接口
 * </p>
 *
 * @author zhiRan
 * @since 2026-06-28
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermissionEntity> {

}
