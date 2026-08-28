package com.cloud.module.agent.service.config;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.cloud.module.agent.controller.admin.config.vo.*;
import com.cloud.module.agent.dal.dataobject.config.ModelDO;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.common.pojo.PageParam;
import com.cloud.framework.common.util.object.BeanUtils;

import com.cloud.module.agent.dal.mysql.config.ModelMapper;

import static com.cloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloud.framework.common.util.collection.CollectionUtils.convertList;
import static com.cloud.framework.common.util.collection.CollectionUtils.diffList;
import static com.cloud.module.agent.enums.ErrorCodeConstants.*;

/**
 * 模型配置表 Service 实现类
 *
 * @author ZR
 */
@Service
@Validated
public class ModelServiceImpl implements ModelService {

    @Resource
    private ModelMapper modelMapper;

    @Override
    public Long createModel(ModelSaveReqVO createReqVO) {
        // 插入
        ModelDO model = BeanUtils.toBean(createReqVO, ModelDO.class);
        modelMapper.insert(model);

        // 返回
        return model.getId();
    }

    @Override
    public void updateModel(ModelSaveReqVO updateReqVO) {
        // 校验存在
        validateModelExists(updateReqVO.getId());
        // 更新
        ModelDO updateObj = BeanUtils.toBean(updateReqVO, ModelDO.class);
        modelMapper.updateById(updateObj);
    }

    @Override
    public void deleteModel(Long id) {
        // 校验存在
        validateModelExists(id);
        // 删除
        modelMapper.deleteById(id);
    }

    @Override
        public void deleteModelListByIds(List<Long> ids) {
        // 删除
        modelMapper.deleteByIds(ids);
        }


    private void validateModelExists(Long id) {
        if (modelMapper.selectById(id) == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
    }

    @Override
    public ModelDO getModel(Long id) {
        return modelMapper.selectById(id);
    }

    @Override
    public List<ModelDO> getModelList() {
        return modelMapper.selectSimpleList();
    }

    @Override
    public PageResult<ModelDO> getModelPage(ModelPageReqVO pageReqVO) {
        return modelMapper.selectPage(pageReqVO);
    }

}
