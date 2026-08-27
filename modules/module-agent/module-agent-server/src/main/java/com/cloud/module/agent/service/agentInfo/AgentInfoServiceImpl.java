package com.cloud.module.agent.service.agentInfo;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.cloud.module.agent.controller.admin.agentInfo.vo.*;
import com.cloud.module.agent.dal.dataobject.agentInfo.AgentInfoDO;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.util.object.BeanUtils;

import com.cloud.module.agent.dal.mysql.agentInfo.AgentInfoMapper;

import static com.cloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloud.framework.common.util.collection.CollectionUtils.convertList;
import static com.cloud.framework.common.util.collection.CollectionUtils.diffList;
import static com.cloud.module.agent.enums.ErrorCodeConstants.*;

/**
 * Agent 定义表 Service 实现类
 *
 * @author ZR
 */
@Service
@Validated
public class AgentInfoServiceImpl implements AgentInfoService {

    @Resource
    private AgentInfoMapper infoMapper;

    @Override
    public Long createInfo(AgentInfoSaveReqVO createReqVO) {
        // 插入
        AgentInfoDO info = BeanUtils.toBean(createReqVO, AgentInfoDO.class);
        infoMapper.insert(info);

        // 返回
        return info.getId();
    }

    @Override
    public void updateInfo(AgentInfoSaveReqVO updateReqVO) {
        // 校验存在
        validateInfoExists(updateReqVO.getId());
        // 更新
        AgentInfoDO updateObj = BeanUtils.toBean(updateReqVO, AgentInfoDO.class);
        infoMapper.updateById(updateObj);
    }

    @Override
    public void deleteInfo(Long id) {
        // 校验存在
        validateInfoExists(id);
        // 删除
        infoMapper.deleteById(id);
    }

    @Override
        public void deleteInfoListByIds(List<Long> ids) {
        // 删除
        infoMapper.deleteByIds(ids);
        }


    private void validateInfoExists(Long id) {
        if (infoMapper.selectById(id) == null) {
            throw exception(INFO_NOT_EXISTS);
        }
    }

    @Override
    public AgentInfoDO getInfo(Long id) {
        return infoMapper.selectById(id);
    }

    @Override
    public PageResult<AgentInfoDO> getInfoPage(AgentInfoPageReqVO pageReqVO) {
        return infoMapper.selectPage(pageReqVO);
    }

}