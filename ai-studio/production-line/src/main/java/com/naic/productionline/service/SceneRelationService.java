package com.naic.productionline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.naic.api.api.DataPage;
import com.naic.productionline.domain.po.ModelTagPO;
import com.naic.productionline.domain.po.SceneRelation;
import com.naic.productionline.domain.vo.ModelTagVO;

import java.util.List;
import java.util.Map;

/**
 * 应用场景 业务层
 *
 * @author wangyunong
 */
public interface SceneRelationService {

    /**
     * 按照应用场景查询
     *
     * @param applicationScene 应用场景
     * @return  ModelTagVO
     */
    List<SceneRelation> getByApplicationScene(Integer applicationScene);

    /**
     * 按照任务场景查询
     *
     * @param scene 任务场景
     * @return  ModelTagVO
     */
    List<SceneRelation> getByScene(Integer scene);

}
