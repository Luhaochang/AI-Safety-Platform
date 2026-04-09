package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.api.api.DataPage;
import com.naic.api.api.ServiceException;
import com.naic.api.utils.Condition;
import com.naic.productionline.domain.cnv.ModelCnv;import com.naic.productionline.domain.cnv.ModelTagRelationCnv;
import com.naic.productionline.domain.dto.ModelDTO;
import com.naic.productionline.domain.dto.ModelTagRelationDTO;
import com.naic.productionline.domain.po.*;
import com.naic.productionline.domain.vo.ModelTagVO;
import com.naic.productionline.domain.vo.ModelVO;
import com.naic.productionline.mapper.*;
import com.naic.productionline.service.ModelService;
import com.naic.productionline.service.ModelTagRelationService;
import com.naic.productionline.utils.MLflowUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模型管理 服务实现
 *
 * @author xingdong
 */
@Service
@AllArgsConstructor
public class ModelServiceImpl extends ServiceImpl<ModelMapper, ModelPO> implements ModelService {

    private final ModelMapper modelMapper;
    private final ModelCnv modelCnv;
    private final PipelineTaskMapper pipelineTaskMapper;
    private final ModelTagRelationService modelTagRelationService;
    private final ModelServiceMapper modelServiceMapper;
    private final MLflowUtil mLflowUtil;
    private final JupyterServiceMapper jupyterServiceMapper;

    // 对应表：app_scene (应用场景)
    private final AppSceneMapper appSceneMapper;
    // 对应表：task_type (任务类型)
    private final TaskTypeMapper taskTypeMapper;
    // 对应表：model_frame (模型框架)
    private final ModelFrameMapper frameMapper;
    // 对应表：model_provider (模型提供方)
    private final ModelProviderMapper providerMapper;

    @Override
    public DataPage<ModelVO> selectModel(Map<String, Object> condition, Integer pageNo, Integer pageSize) {
        IPage<ModelVO> page=modelMapper.selectPage(new Page<>(pageNo,pageSize), Condition.getQueryWrapper(condition,ModelPO.class)).convert(modelCnv::poToVo);
        List<ModelVO> list=page.getRecords();
        for(ModelVO model:list){
            model.setTagList(modelTagRelationService.getTagByModel(model.getId()));
        }
        return DataPage.rest(page);
    }

    @Override
    public ModelVO getModelDetail(Long id) {
        modelMapper.incrementViews(id);
        ModelPO model = modelMapper.selectById(id);
        if (model == null) {
            throw new ServiceException("模型不存在");
        }
        ModelVO vo = modelCnv.poToVo(model);
        vo.setTagList(modelTagRelationService.getTagByModel(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertModel(ModelDTO dto) {
        ModelPO model = modelCnv.dtoToPo(dto);
        model.setIsOfficial(1);
        if (model.getClassDict() == null) {
            model.setClassDict("");
        }
        model.setModelViews(0L);
        modelMapper.insert(model);

        if (dto.getTagIdList() != null) {
            for (Long id : dto.getTagIdList()) {
                modelTagRelationService.insertRelation(new ModelTagRelationDTO().setModelId(model.getId()).setTagId(id));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertSelfDefineModel(Long taskId, ModelDTO dto) {
        ModelPO model = modelCnv.dtoToPo(dto);
        model.setIsOfficial(0);
        model.setModelViews(0L);

        PipelineTaskPO task = pipelineTaskMapper.selectById(taskId);
        if (task == null || !task.getIsPackage().equals(2)) {
            throw new ServiceException("不符合新建自定义模型条件");
        }
        model.setClassDict(mLflowUtil.getClassDict(task.getExperimentName()));
        model.setFileUrl(task.getImageUrl());

        if (task.getAppSceneId() != null && task.getAppSceneId().length() != 0) {
            model.setAppSceneId(task.getAppSceneId());
        } else {
            if (model.getAppSceneId() == null) {
                throw new ServiceException("必须定义正确的应用场景");
            }
        }

        modelMapper.insert(model);
        if (dto.getTagIdList() != null) {
            for (Long id : dto.getTagIdList()) {
                modelTagRelationService.insertRelation(new ModelTagRelationDTO().setModelId(model.getId()).setTagId(id));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModel(ModelDTO dto) {
        int size = pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>().eq("model_id", dto.getId())).size();
        if (size > 0) {
            throw new ServiceException("模型已经被使用，无法修改");
        }
        int jupyterServiceSize = jupyterServiceMapper.selectList(new QueryWrapper<JupyterServicePO>().eq("model_id", dto.getId())).size();
        if (jupyterServiceSize > 0) {
            throw new ServiceException("模型已经开启了jupyter服务，请关闭服务后再删除");
        }
        ModelPO modelPO = modelCnv.dtoToPo(dto);
        modelMapper.updateById(modelPO);

        List<ModelTagVO> tagList = modelTagRelationService.getTagByModel(dto.getId());
        if (dto.getTagIdList() != null) {
            for (ModelTagVO tag : tagList) {
                boolean isHas = false;
                for (Long tagId : dto.getTagIdList()) {
                    if (tagId.equals(tag.getId())) {
                        isHas = true;
                        break;
                    }
                }
                if (!isHas) {
                    modelTagRelationService.removeById(tag.getId());
                }
            }
            for (Long tagId : dto.getTagIdList()) {
                boolean isHas = false;
                for (ModelTagVO tag : tagList) {
                    if (tagId.equals(tag.getId())) {
                        isHas = true;
                        break;
                    }
                }
                if (!isHas) {
                    modelTagRelationService.insertRelation(new ModelTagRelationDTO().setModelId(dto.getId()).setTagId(tagId));
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(Long id) {
        int size = pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>().eq("model_id", id)).size();
        if (size > 0) {
            throw new ServiceException("模型已经被产线使用，无法删除");
        }
        int serviceSize = modelServiceMapper.selectList(new QueryWrapper<ModelServicePO>().eq("model_id", id)).size();
        if (serviceSize > 0) {
            throw new ServiceException("模型已经被服务使用，无法删除");
        }
        int jupyterServiceSize = jupyterServiceMapper.selectList(new QueryWrapper<JupyterServicePO>().eq("model_id", id)).size();
        if (jupyterServiceSize > 0) {
            throw new ServiceException("模型已经开启了jupyter服务，请关闭服务后再删除");
        }
        modelMapper.deleteById(id);
        modelTagRelationService.deleteAllTagByModel(id);
    }

    @Override
    public void setModelOfficial(Long id, Integer isOfficial) {
        ModelPO model = modelMapper.selectById(id);
        model.setIsOfficial(isOfficial);
        modelMapper.updateById(model);
    }

    @Override
    public Map<String, List<Map<String, Object>>> getModelOptions() {
        Map<String, List<Map<String, Object>>> options = new HashMap<>();

        // 1. 获取应用场景 (App Scenes)
        List<AppScenePO> appScenes = appSceneMapper.selectList(null);
        options.put("appScenes", appScenes.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("value", item.getId());
            map.put("label", item.getAppName());
            return map;
        }).collect(Collectors.toList()));

        // 2. 获取任务类型 (Task Types)
        List<TaskTypePO> taskTypes = taskTypeMapper.selectList(null);
        options.put("taskTypes", taskTypes.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("value", item.getId());
            map.put("label", item.getTaskName());
            return map;
        }).collect(Collectors.toList()));

        // 3. 获取模型框架 (Frameworks)
        List<ModelFramePO> frameworks = frameMapper.selectList(null);
        options.put("frameworks", frameworks.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("value", item.getId());
            map.put("label", item.getFrameName());
            return map;
        }).collect(Collectors.toList()));

        // 4. 获取提供方 (Providers)
        List<ModelProviderPO> providers = providerMapper.selectList(null);
        options.put("providers", providers.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("value", item.getId());
            map.put("label", item.getProviderName());
            return map;
        }).collect(Collectors.toList()));

        return options;
    }
}