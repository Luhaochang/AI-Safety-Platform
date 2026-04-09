package com.naic.productionline.service;

import com.naic.productionline.domain.vo.AppSceneVO;

import java.util.List;

/**
 * 应用场景 业务层
 *
 * @author xingdong
 */
public interface AppSceneService {

    /**
     * 查询所有
     *
     * @return AppSceneVO
     */
    List<AppSceneVO> getAll();
}
