package com.cloud.module.agent.service.config;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.cloud.module.agent.controller.admin.config.vo.*;
import com.cloud.module.agent.dal.dataobject.config.SysPromptDO;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.util.object.BeanUtils;

import com.cloud.module.agent.dal.mysql.config.SysPromptMapper;

import static com.cloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloud.framework.common.util.collection.CollectionUtils.convertList;
import static com.cloud.framework.common.util.collection.CollectionUtils.diffList;
import static com.cloud.module.agent.enums.ErrorCodeConstants.*;

/**
 * 系统提示词表 Service 实现类
 *
 * @author ZR
 */
@Service
@Validated
public class SysPromptServiceImpl implements SysPromptService {

    @Resource
    private SysPromptMapper sysPromptMapper;

    @Override
    public Long createSysPrompt(SysPromptSaveReqVO createReqVO) {
        // 插入
        SysPromptDO sysPrompt = BeanUtils.toBean(createReqVO, SysPromptDO.class);
        sysPromptMapper.insert(sysPrompt);

        // 返回
        return sysPrompt.getId();
    }

    @Override
    public void updateSysPrompt(SysPromptSaveReqVO updateReqVO) {
        // 校验存在
        validateSysPromptExists(updateReqVO.getId());
        // 更新
        SysPromptDO updateObj = BeanUtils.toBean(updateReqVO, SysPromptDO.class);
        sysPromptMapper.updateById(updateObj);
    }

    @Override
    public void deleteSysPrompt(Long id) {
        // 校验存在
        validateSysPromptExists(id);
        // 删除
        sysPromptMapper.deleteById(id);
    }

    @Override
        public void deleteSysPromptListByIds(List<Long> ids) {
        // 删除
        sysPromptMapper.deleteByIds(ids);
        }


    private void validateSysPromptExists(Long id) {
        if (sysPromptMapper.selectById(id) == null) {
            throw exception(SYS_PROMPT_NOT_EXISTS);
        }
    }

    @Override
    public SysPromptDO getSysPrompt(Long id) {
        return sysPromptMapper.selectById(id);
    }

    @Override
    public List<SysPromptDO> getSysPromptList() {
        return sysPromptMapper.selectSimpleList();
    }

    @Override
    public PageResult<SysPromptDO> getSysPromptPage(SysPromptPageReqVO pageReqVO) {
        return sysPromptMapper.selectPage(pageReqVO);
    }

}
