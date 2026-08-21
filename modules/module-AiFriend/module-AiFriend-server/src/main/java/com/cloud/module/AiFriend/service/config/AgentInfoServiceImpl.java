package com.cloud.module.AiFriend.service.config;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.cloud.module.AiFriend.controller.admin.config.vo.*;
import com.cloud.module.AiFriend.dal.dataobject.config.AgentInfoDO;
import com.cloud.module.AiFriend.dal.dataobject.config.ModelDO;
import com.cloud.module.AiFriend.dal.dataobject.config.SysPromptDO;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.util.object.BeanUtils;

import com.cloud.module.AiFriend.dal.mysql.config.AgentInfoMapper;
import com.cloud.module.AiFriend.dal.mysql.config.ModelMapper;
import com.cloud.module.AiFriend.dal.mysql.config.SysPromptMapper;

import static com.cloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloud.framework.common.util.collection.CollectionUtils.convertList;
import static com.cloud.framework.common.util.collection.CollectionUtils.diffList;
import static com.cloud.module.AiFriend.enums.ErrorCodeConstants.*;

/**
 * Agent 定义表 Service 实现类
 *
 * @author ZR
 */
@Service
@Validated
public class AgentInfoServiceImpl implements AgentInfoService {

    @Resource
    private AgentInfoMapper agentInfoMapper;
    @Resource
    private ModelMapper modelMapper;
    @Resource
    private SysPromptMapper sysPromptMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAgentInfo(AgentInfoSaveReqVO createReqVO) {
        // 插入
        AgentInfoDO agentInfo = BeanUtils.toBean(createReqVO, AgentInfoDO.class);
        agentInfoMapper.insert(agentInfo);


        // 插入子表
        createModel(agentInfo.getId(), createReqVO.getModel());
        createSysPrompt(agentInfo.getId(), createReqVO.getSysPrompt());
        // 返回
        return agentInfo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAgentInfo(AgentInfoSaveReqVO updateReqVO) {
        // 校验存在
        validateAgentInfoExists(updateReqVO.getId());
        // 更新
        AgentInfoDO updateObj = BeanUtils.toBean(updateReqVO, AgentInfoDO.class);
        agentInfoMapper.updateById(updateObj);

        // 更新子表
        updateModel(updateReqVO.getId(), updateReqVO.getModel());
        updateSysPrompt(updateReqVO.getId(), updateReqVO.getSysPrompt());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAgentInfo(Long id) {
        // 校验存在
        validateAgentInfoExists(id);
        // 删除
        agentInfoMapper.deleteById(id);

        // 删除子表
        deleteModelById(id);
        deleteSysPromptById(id);
    }

    @Override
        @Transactional(rollbackFor = Exception.class)
    public void deleteAgentInfoListByIds(List<Long> ids) {
        // 删除
        agentInfoMapper.deleteByIds(ids);
    
    // 删除子表
            deleteModelByIds(ids);
            deleteSysPromptByIds(ids);
    }


    private void validateAgentInfoExists(Long id) {
        if (agentInfoMapper.selectById(id) == null) {
            throw exception(AGENT_INFO_NOT_EXISTS);
        }
    }

    @Override
    public AgentInfoDO getAgentInfo(Long id) {
        return agentInfoMapper.selectById(id);
    }

    @Override
    public PageResult<AgentInfoDO> getAgentInfoPage(AgentInfoPageReqVO pageReqVO) {
        return agentInfoMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（模型配置表） ====================

    @Override
    public ModelDO getModelById(Long id) {
        return modelMapper.selectById(id);
    }

    private void createModel(Long id, ModelDO model) {
        if (model == null) {
            return;
        }
        model.setId(id);
        modelMapper.insert(model);
    }

    private void updateModel(Long id, ModelDO model) {
        if (model == null) {
			return;
        }
        model.setId(id).clean();// 解决更新情况下：updateTime 不更新
        modelMapper.insertOrUpdate(model);
    }

    private void deleteModelById(Long id) {
        modelMapper.deleteById(id);
    }

	private void deleteModelByIds(List<Long> ids) {
        modelMapper.deleteByIds(ids);
	}

    // ==================== 子表（系统提示词表） ====================

    @Override
    public SysPromptDO getSysPromptById(Long id) {
        return sysPromptMapper.selectById(id);
    }

    private void createSysPrompt(Long id, SysPromptDO sysPrompt) {
        if (sysPrompt == null) {
            return;
        }
        sysPrompt.setId(id);
        sysPromptMapper.insert(sysPrompt);
    }

    private void updateSysPrompt(Long id, SysPromptDO sysPrompt) {
        if (sysPrompt == null) {
			return;
        }
        sysPrompt.setId(id).clean();// 解决更新情况下：updateTime 不更新
        sysPromptMapper.insertOrUpdate(sysPrompt);
    }

    private void deleteSysPromptById(Long id) {
        sysPromptMapper.deleteById(id);
    }

	private void deleteSysPromptByIds(List<Long> ids) {
        sysPromptMapper.deleteByIds(ids);
	}

}