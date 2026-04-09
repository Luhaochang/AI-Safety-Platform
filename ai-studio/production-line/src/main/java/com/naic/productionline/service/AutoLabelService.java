package com.naic.productionline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.naic.api.api.DataPage;
import com.naic.productionline.domain.dto.AutoLabelDTO;
import com.naic.productionline.domain.dto.PipelineTaskDTO;
import com.naic.productionline.domain.po.AutoLabelPO;
import com.naic.productionline.domain.po.PipelineTaskPO;
import com.naic.productionline.domain.vo.AutoLabelVO;
import com.naic.productionline.domain.vo.PipelineTaskVO;
import com.naic.productionline.domain.vo.TaskFlowVO;
import com.naic.productionline.domain.vo.mlflow.ArtifactPathVO;
import com.naic.productionline.domain.vo.taskLogTools.TaskLog;
import io.kubernetes.client.openapi.ApiException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 自动化标注管理 业务层
 *
 * @author wangyunong
 */
public interface AutoLabelService extends IService<AutoLabelPO> {

    /**
     * 条件查询
     *
     * @param pageNo No
     * @param pageSize Size
     * @return  DataPage<AutoLabelVO>
     */
    DataPage<AutoLabelVO> selectList(Long dataSetTaskId,Integer pageNo, Integer pageSize);

    /**
     * 新增自动标注任务
     *
     * @param dto 自动标注信息
     */
    Long insertAutoLabel(AutoLabelDTO dto);

    /**
     * 修改自动标注任务
     *
     * @param dto 自动标注信息
     */
    void updateAutoLabel(AutoLabelDTO dto);

    /**
     * 删除自动标注任务
     *
     * @param id 模型产线id
     */
    void deleteAutoLabel(Long id) throws ApiException;

    /**
     * 提交自动标注任务，排队
     *
     * @param id 模型产线id
     */
    void queue(Long id) throws IOException, ApiException;

}
