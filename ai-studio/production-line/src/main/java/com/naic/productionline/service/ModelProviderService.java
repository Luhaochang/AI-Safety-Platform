package com.naic.productionline.service;

import com.naic.productionline.domain.vo.ModelProviderVO;

import java.util.List;

/**
 * 模型提供方 业务层
 *
 * @author xingdong
 */
public interface ModelProviderService {

    /**
     * 查询所有
     *
     * @return ModelProviderVO
     */
    List<ModelProviderVO> getAll();
}
