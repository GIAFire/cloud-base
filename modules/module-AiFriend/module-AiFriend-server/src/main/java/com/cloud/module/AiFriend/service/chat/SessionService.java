package com.cloud.module.AiFriend.service.chat;

import java.util.*;
import jakarta.validation.*;
import com.cloud.module.AiFriend.controller.admin.chat.vo.*;
import com.cloud.module.AiFriend.dal.dataobject.chat.SessionDO;
import com.cloud.module.AiFriend.dal.dataobject.chat.MessageDO;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.PageParam;

/**
 * Agent会话表：保存用户和Agent之间的一段连续对话 Service 接口
 *
 * @author ZR
 */
public interface SessionService {

    /**
     * 创建Agent会话表：保存用户和Agent之间的一段连续对话
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSession(@Valid SessionSaveReqVO createReqVO);

    /**
     * 更新Agent会话表：保存用户和Agent之间的一段连续对话
     *
     * @param updateReqVO 更新信息
     */
    void updateSession(@Valid SessionSaveReqVO updateReqVO);

    /**
     * 删除Agent会话表：保存用户和Agent之间的一段连续对话
     *
     * @param id 编号
     */
    void deleteSession(Long id);

    /**
    * 批量删除Agent会话表：保存用户和Agent之间的一段连续对话
    *
    * @param ids 编号
    */
    void deleteSessionListByIds(List<Long> ids);

    /**
     * 获得Agent会话表：保存用户和Agent之间的一段连续对话
     *
     * @param id 编号
     * @return Agent会话表：保存用户和Agent之间的一段连续对话
     */
    SessionDO getSession(Long id);

    /**
     * 获得Agent会话表：保存用户和Agent之间的一段连续对话分页
     *
     * @param pageReqVO 分页查询
     * @return Agent会话表：保存用户和Agent之间的一段连续对话分页
     */
    PageResult<SessionDO> getSessionPage(SessionPageReqVO pageReqVO);

    // ==================== 子表（Agent消息表：保存输入、思考、正文等消息） ====================

    /**
     * 获得Agent消息表：保存输入、思考、正文等消息列表
     *
     * @param sessionId 会话ID，关联ai_agent_session.id
     * @return Agent消息表：保存输入、思考、正文等消息列表
     */
    List<MessageDO> getMessageListBySessionId(Long sessionId);

}