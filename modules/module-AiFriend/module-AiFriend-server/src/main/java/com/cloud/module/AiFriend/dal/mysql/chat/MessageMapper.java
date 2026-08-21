package com.cloud.module.AiFriend.dal.mysql.chat;

import java.util.*;

import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.cloud.framework.mybatis.core.mapper.BaseMapperX;
import com.cloud.module.AiFriend.dal.dataobject.chat.MessageDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Agent消息表：保存输入、思考、正文等消息 Mapper
 *
 * @author ZR
 */
@Mapper
public interface MessageMapper extends BaseMapperX<MessageDO> {

    default List<MessageDO> selectListBySessionId(Long sessionId) {
        return selectList(MessageDO::getSessionId, sessionId);
    }

    default int deleteBySessionId(Long sessionId) {
        return delete(MessageDO::getSessionId, sessionId);
    }

	default int deleteBySessionIds(List<Long> sessionIds) {
	    return deleteBatch(MessageDO::getSessionId, sessionIds);
	}

}