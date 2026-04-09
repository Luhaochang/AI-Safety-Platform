package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.api.api.ServiceException;
import com.naic.productionline.domain.cnv.ModelCnv;
import com.naic.productionline.domain.cnv.ModelTagCnv;
import com.naic.productionline.domain.cnv.ModelTagRelationCnv;
import com.naic.productionline.domain.dto.ModelTagRelationDTO;
import com.naic.productionline.domain.po.ModelTagPO;
import com.naic.productionline.domain.po.ModelTagRelationPO;
import com.naic.productionline.domain.vo.ModelTagVO;
import com.naic.productionline.domain.vo.ModelVO;
import com.naic.productionline.mapper.ModelMapper;
import com.naic.productionline.mapper.ModelTagMapper;
import com.naic.productionline.mapper.ModelTagRelationMapper;
import com.naic.productionline.service.ModelTagRelationService;
import com.naic.productionline.service.ModelTagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型标签关系管理 服务实现
 *
 * @author wangyunong
 */
@Service
@AllArgsConstructor
public class ModelTagRelationServiceImpl extends ServiceImpl<ModelTagRelationMapper, ModelTagRelationPO> implements ModelTagRelationService {

    private final ModelTagRelationMapper modelTagRelationMapper;

    private final ModelTagMapper modelTagMapper;

    private final ModelTagRelationCnv modelTagRelationCnv;

    private final ModelMapper modelMapper;

    private final ModelCnv modelCnv;

    private final ModelTagCnv modelTagCnv;

    @Override
    public void insertRelation(ModelTagRelationDTO dto) {
        ModelTagPO tag=modelTagMapper.selectById(dto.getTagId());
        //是否是一级标签:1.一级标签
        if(tag==null||(tag.getIsSuper().equals(1))){
            throw new ServiceException("标签不存在或为一级标签");
        }
        if(modelTagRelationMapper.selectList(new QueryWrapper<ModelTagRelationPO>().eq("model_id",dto.getModelId()).eq("tag_id",dto.getTagId())).size()>0){
            throw new ServiceException("模型已存在该标签");
        }
        modelTagRelationMapper.insert(modelTagRelationCnv.dtoToPo(dto));
    }

    @Override
    public List<ModelVO> getModelByTag(List<Long> tagList) {
        List<ModelTagRelationPO> list=modelTagRelationMapper.selectList(new QueryWrapper<ModelTagRelationPO>().in("tag_id",tagList));
        List<ModelVO> voList=new ArrayList<>();
        for(ModelTagRelationPO relation:list){
            ModelVO vo= modelCnv.poToVo(modelMapper.selectById(relation.getModelId()));
            vo.setTagList(getTagByModel(vo.getId()));
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public List<ModelTagVO> getTagByModel(Long modelId) {
        List<ModelTagRelationPO> relationList=modelTagRelationMapper.selectList(new QueryWrapper<ModelTagRelationPO>().eq("model_id",modelId));
        List<ModelTagVO> voList=new ArrayList<>();
        for (ModelTagRelationPO relation:relationList){
            ModelTagVO vo=modelTagCnv.poToVo(modelTagMapper.selectById(relation.getTagId()));
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public void deleteAllTagByModel(Long modelId) {
        List<ModelTagRelationPO> relationList=modelTagRelationMapper.selectList(new QueryWrapper<ModelTagRelationPO>().eq("model_id",modelId));
        if (relationList==null||relationList.isEmpty()){
            return;
        }
        modelTagRelationMapper.deleteBatchIds(relationList);
    }

    @Override
    public Boolean isHasRelation(Long tagId) {
        List<ModelTagRelationPO> list=modelTagRelationMapper.selectList(new QueryWrapper<ModelTagRelationPO>().eq("tag_id",tagId));
        return list != null && !list.isEmpty();
    }
}
