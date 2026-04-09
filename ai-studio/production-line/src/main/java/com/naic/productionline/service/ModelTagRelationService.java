package com.naic.productionline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.naic.productionline.domain.dto.ModelTagRelationDTO;
import com.naic.productionline.domain.po.ModelPO;
import com.naic.productionline.domain.po.ModelTagPO;
import com.naic.productionline.domain.po.ModelTagRelationPO;
import com.naic.productionline.domain.vo.ModelTagVO;
import com.naic.productionline.domain.vo.ModelVO;

import java.util.List;

/**
 * 模型标签关系管理 业务层
 *
 * @author wangyunong
 */
public interface ModelTagRelationService extends IService<ModelTagRelationPO> {

    /**
     * 新增模型标签关系
     *
     * @param dto 关系
     */
    void insertRelation(ModelTagRelationDTO dto);

    /**
     * 根据标签获取模型列表
     *
     * @param tagList 标签列表
     * @return List<ModelVO>
     */
    List<ModelVO> getModelByTag(List<Long> tagList);

    /**
     * 获取模型的全部标签
     *
     * @param modelId 模型id
     * @return List<ModelTagVO>
     */
    List<ModelTagVO> getTagByModel(Long modelId);

    /**
     * 删除模型的全部标签
     *
     * @param modelId 模型id
     */
    void deleteAllTagByModel(Long modelId);

    /**
     * 标签是否被使用
     *
     * @param tagId 标签id
     * @return Boolean
     */
    Boolean isHasRelation(Long tagId);
}
