package com.naic.productionline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.naic.api.api.DataPage;
import com.naic.productionline.domain.dto.ModelDTO;
import com.naic.productionline.domain.po.ModelPO;
import com.naic.productionline.domain.vo.ModelVO;

import java.util.List;
import java.util.Map;

/**
 * 模型管理 业务层
 *
 * @author xingdong
 */
public interface ModelService extends IService<ModelPO> {

    /**
     * 条件查询
     *
     * @param condition 条件
     * @return  PipelineTaskVO
     */
    DataPage<ModelVO> selectModel(Map<String,Object> condition, Integer pageNo, Integer pageSize);

    /**
     * 获取模型详情并增加浏览次数
     * @param id 模型ID
     * @return ModelVO
     */
    ModelVO getModelDetail(Long id);

    /**
     * 新增官方模型
     *
     * @param dto 模型信息
     */
    void insertModel(ModelDTO dto);

    /**
     * 新增自定义模型
     *
     * @param dto 模型信息
     */
    void insertSelfDefineModel(Long taskId,ModelDTO dto);

    /**
     * 修改模型
     *
     * @param dto 模型信息
     */
    void updateModel(ModelDTO dto);

    /**
     * 删除模型
     *
     * @param id 模型id
     */
    void deleteModel(Long id);

    /**
     * 设置模型是否为官方模型
     *
     * @param id 模型id
     * @param isOfficial 0.非官方1.官方
     */
    void setModelOfficial(Long id, Integer isOfficial);

    /**
     * 获取模型相关的配置选项（应用场景、任务类型、框架、提供方）
     *
     * @return 选项Map
     */
    Map<String, List<Map<String, Object>>> getModelOptions();

}