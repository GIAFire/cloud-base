package com.cloud.module.member.dal.mysql.auth;

import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloud.module.member.dal.dataobject.auth.MemberEmailCodeDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface MemberEmailCodeMapper extends BaseMapperX<MemberEmailCodeDO> {

    default MemberEmailCodeDO selectLast(String email, Integer scene) {
        return selectOne(new LambdaQueryWrapperX<MemberEmailCodeDO>()
                .eq(MemberEmailCodeDO::getEmail, email)
                .eq(MemberEmailCodeDO::getScene, scene)
                .orderByDesc(MemberEmailCodeDO::getId)
                .last("LIMIT 1"));
    }

    default MemberEmailCodeDO selectLastByEmail(String email) {
        return selectOne(new LambdaQueryWrapperX<MemberEmailCodeDO>()
                .eq(MemberEmailCodeDO::getEmail, email)
                .orderByDesc(MemberEmailCodeDO::getId)
                .last("LIMIT 1"));
    }

    default Long selectCountSince(String email, LocalDateTime since) {
        return selectCount(new LambdaQueryWrapperX<MemberEmailCodeDO>()
                .eq(MemberEmailCodeDO::getEmail, email)
                .ge(MemberEmailCodeDO::getCreateTime, since));
    }
}
