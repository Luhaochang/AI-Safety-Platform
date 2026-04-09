package com.naic.productionline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.naic.api.api.DataPage;
import com.naic.productionline.domain.dto.ModelDTO;
import com.naic.productionline.domain.po.ModelTagPO;
import com.naic.productionline.domain.vo.ModelTagVO;
import com.naic.productionline.domain.vo.ModelVO;

import java.util.List;
import java.util.Map;

/**
 * 模型标签管理 业务层
 *
 * @author wangyunong
 */
public interface ModelTagService extends IService<ModelTagPO> {

    /**
     * 按照类别查询
     *
     * @param category 类别
     * @return  ModelTagVO
     */
    List<ModelTagVO> getModelTagByCategory(Integer category);

    /**
     * 子标签查询
     *
     * @param superId 一级标签id
     * @return  ModelTagVO
     */
    List<ModelTagVO> getModelTagBySuper(Long superId);

    /**
     * 条件查询
     *
     * @param condition 条件
     * @return  ModelTagVO
     */
    DataPage<ModelTagVO> selectModel(Map<String,Object> condition, Integer pageNo, Integer pageSize);

}
