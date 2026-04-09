package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.api.api.DataPage;
import com.naic.api.api.ServiceException;
import com.naic.api.utils.Condition;
import com.naic.productionline.domain.cnv.ModelCnv;
import com.naic.productionline.domain.cnv.ModelTagRelationCnv;
import com.naic.productionline.domain.dto.ModelDTO;
import com.naic.productionline.domain.dto.ModelTagRelationDTO;
import com.naic.productionline.domain.po.ModelPO;
import com.naic.productionline.domain.po.ModelServicePO;
import com.naic.productionline.domain.po.PipelineTaskPO;
import com.naic.productionline.domain.po.SceneRelation;
import com.naic.productionline.domain.vo.ModelTagVO;
import com.naic.productionline.domain.vo.ModelVO;
import com.naic.productionline.mapper.ModelMapper;
import com.naic.productionline.mapper.ModelServiceMapper;
import com.naic.productionline.mapper.PipelineTaskMapper;
import com.naic.productionline.mapper.SceneRelationMapper;
import com.naic.productionline.service.ModelService;
import com.naic.productionline.service.ModelTagRelationService;
import com.naic.productionline.service.SceneRelationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 模型管理 服务实现
 *
 * @author wangyunong
 */
@Service
@AllArgsConstructor
public class SceneRelationServiceImpl implements SceneRelationService {

    private final SceneRelationMapper sceneRelationMapper;
    @Override
    public List<SceneRelation> getByApplicationScene(Integer applicationScene) {
        return sceneRelationMapper.selectList(new QueryWrapper<SceneRelation>().eq("application_scene",applicationScene));
    }

    @Override
    public List<SceneRelation> getByScene(Integer scene) {
        return sceneRelationMapper.selectList(new QueryWrapper<SceneRelation>().eq("scene_id",scene));
    }
}
