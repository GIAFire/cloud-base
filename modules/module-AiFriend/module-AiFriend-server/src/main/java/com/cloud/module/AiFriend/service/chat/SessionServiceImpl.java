package com.cloud.module.AiFriend.service.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.cloud.module.AiFriend.controller.admin.chat.vo.*;
import com.cloud.module.AiFriend.dal.dataobject.chat.SessionDO;
import com.cloud.module.AiFriend.dal.dataobject.chat.MessageDO;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.util.object.BeanUtils;

import com.cloud.module.AiFriend.dal.mysql.chat.SessionMapper;
import com.cloud.module.AiFriend.dal.mysql.chat.MessageMapper;

import static com.cloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloud.framework.common.util.collection.CollectionUtils.convertList;
import static com.cloud.framework.common.util.collection.CollectionUtils.diffList;
import static com.cloud.module.AiFriend.enums.ErrorCodeConstants.*;

/**
 * Agent会话表：保存用户和Agent之间的一段连续对话 Service 实现类
 *
 * @author ZR
 */
@Service
@Validated
public class SessionServiceImpl implements SessionService {

    @Resource
    private SessionMapper sessionMapper;
    @Resource
    private MessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSession(SessionSaveReqVO createReqVO) {
        // 插入
        SessionDO session = BeanUtils.toBean(createReqVO, SessionDO.class);
        sessionMapper.insert(session);


        // 插入子表
        createMessageList(session.getId(), createReqVO.getMessages());
        // 返回
        return session.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSession(SessionSaveReqVO updateReqVO) {
        // 校验存在
        validateSessionExists(updateReqVO.getId());
        // 更新
        SessionDO updateObj = BeanUtils.toBean(updateReqVO, SessionDO.class);
        sessionMapper.updateById(updateObj);

        // 更新子表
        updateMessageList(updateReqVO.getId(), updateReqVO.getMessages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long id) {
        // 校验存在
        validateSessionExists(id);
        // 删除
        sessionMapper.deleteById(id);

        // 删除子表
        deleteMessageBySessionId(id);
    }

    @Override
        @Transactional(rollbackFor = Exception.class)
    public void deleteSessionListByIds(List<Long> ids) {
        // 删除
        sessionMapper.deleteByIds(ids);
    
    // 删除子表
            deleteMessageBySessionIds(ids);
    }


    private void validateSessionExists(Long id) {
        if (sessionMapper.selectById(id) == null) {
            throw exception(SESSION_NOT_EXISTS);
        }
    }

    @Override
    public SessionDO getSession(Long id) {
        return sessionMapper.selectById(id);
    }

    @Override
    public PageResult<SessionDO> getSessionPage(SessionPageReqVO pageReqVO) {
        return sessionMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（Agent消息表：保存输入、思考、正文等消息） ====================

    @Override
    public List<MessageDO> getMessageListBySessionId(Long sessionId) {
        return messageMapper.selectListBySessionId(sessionId);
    }

    private void createMessageList(Long sessionId, List<MessageDO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        list.forEach(o -> o.setSessionId(sessionId).clean());
        messageMapper.insertBatch(list);
    }

    private void updateMessageList(Long sessionId, List<MessageDO> list) {
	    list.forEach(o -> o.setSessionId(sessionId).clean());
	    List<MessageDO> oldList = messageMapper.selectListBySessionId(sessionId);
	    List<List<MessageDO>> diffList = diffList(oldList, list, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getId(), newVal.getId());
            if (same) {
                newVal.setId(oldVal.getId()).clean(); // 解决更新情况下：updateTime 不更新
            }
            return same;
	    });

	    // 第二步，批量添加、修改、删除
	    if (CollUtil.isNotEmpty(diffList.get(0))) {
	        messageMapper.insertBatch(diffList.get(0));
	    }
	    if (CollUtil.isNotEmpty(diffList.get(1))) {
	        messageMapper.updateBatch(diffList.get(1));
	    }
	    if (CollUtil.isNotEmpty(diffList.get(2))) {
	        messageMapper.deleteByIds(convertList(diffList.get(2), MessageDO::getId));
	    }
    }

    private void deleteMessageBySessionId(Long sessionId) {
        messageMapper.deleteBySessionId(sessionId);
    }

	private void deleteMessageBySessionIds(List<Long> sessionIds) {
        messageMapper.deleteBySessionIds(sessionIds);
	}

}