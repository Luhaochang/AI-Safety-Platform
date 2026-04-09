package com.naic.productionline.service;

import com.naic.productionline.domain.vo.ModelFrameVO;

import java.util.List;

/**
 * 模型框架 业务层
 *
 * @author xingdong
 */
public interface ModelFrameService {

    /**
     * 查询所有
     *
     * @return ModelFrameVO
     */
    List<ModelFrameVO> getAll();
}
