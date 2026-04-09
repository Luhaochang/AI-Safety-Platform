package com.naic.productionline.service;

import com.naic.api.api.DataPage;
import com.naic.productionline.domain.vo.homePage.*;
import io.kubernetes.client.openapi.ApiException;

import java.util.List;
import java.util.Map;

/**
 * 主页 业务层
 * @author wyn
 */
public interface HomePageService {

    /**
     * 数据集分类统计
     *
     * @return  List<DataSetStatistics>
     */
    List<DataSetStatistics> getDataSetStatistics();

    /**
     * 数据集标注完成率
     *
     * @return  List<DataSetTaskStatistics>
     */
    DataPage<DataSetTaskStatistics> getDataSetTaskStatistics(Integer pageNo,Integer pageSize);

    /**
     * 已完成模型训练任务的时间情况统计
     *
     * @return  List<FinishedPipelineTaskStatistics>
     */
    List<FinishedPipelineTaskStatistics> getFinishedPipelineTaskStatistics();

    /**
     * 模型情况统计
     *
     * @return  List<ModelStatistics>
     */
    List<ModelStatistics> getModelStatistics();

    /**
     * 模型训练情况统计
     *
     * @return  List<PipelineTaskStatistics>
     */
    List<PipelineTaskStatistics> getPipelineTaskStatistics();

    /**
     * 总体情况统计
     *
     * @return  SummaryStatistics
     */
    SummaryStatistics getSummaryStatistics();

    /**
     * 总体情况统计
     *
     * @return  SummaryStatistics
     */
    DataPage<NodeStatistics> getNodeStatistics(Integer pageNo,Integer pageSize) throws ApiException;
}
