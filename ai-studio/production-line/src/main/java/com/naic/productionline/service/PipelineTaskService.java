package com.naic.productionline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.naic.api.api.DataPage;
import com.naic.productionline.domain.dto.PipelineTaskDTO;
import com.naic.productionline.domain.po.PipelineTaskPO;
import com.naic.productionline.domain.vo.*;
import com.naic.productionline.domain.vo.mlflow.ArtifactPathVO;
import com.naic.productionline.domain.vo.taskLogTools.TaskLog;
import io.kubernetes.client.openapi.ApiException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 模型产线管理 业务层
 *
 * @author wangyunong
 */
public interface PipelineTaskService extends IService<PipelineTaskPO> {

    /**
     * 条件查询
     *
     * @param condition 条件
     * @return  PipelineTaskVO
     */
    DataPage<PipelineTaskVO> selectList(Map<String,Object> condition, Integer pageNo, Integer pageSize);

    /**
     * 新增模型产线
     *
     * @param dto 模型产线信息
     */
    Long insertPipelineTask(PipelineTaskDTO dto);

    /**
     * 修改模型产线
     *
     * @param dto 模型产线信息
     */
    void updatePipelineTask(PipelineTaskDTO dto);

    /**
     * 删除模型产线
     *
     * @param id 模型产线id
     */
    void deletePipelineTask(Long id) throws ApiException;

    /**
     * 提交训练，排队
     *
     * @param id 模型产线id
     */
    void queue(Long id,Map<String,String> map) throws IOException, ApiException;

    /**
     * 中断训练
     *
     * @param id 模型产线id
     */
    void quit(Long id) throws ApiException;

    /**
     * 获取实验基本信息
     *
     * @param id 模型产线id
     * @return  TaskFlowVO
     */
    TaskFlowVO getMlflowMessage(Long id) throws IOException, InterruptedException;

    /**
     * 获取实验的模型文件
     *
     * @param id 模型产线id
     * @return  List<Service.Param>
     */
    List<ArtifactPathVO> getArtifacts(Long id, String path) throws IOException, InterruptedException;

    /**
     *导出实验镜像
     *
     * @param id 模型产线id
     */
    void getImageUrl(Long id) throws IOException, InterruptedException;

    /**
     * es查询训练任务日志
     * @param taskId 任务id
     * @return 查询结果
     */
    List<TaskLog> queryByEs(Long taskId);

    /**
     * 设置产线状态，airflow接口
     *
     * @param taskId 模型产线id
     * @param success 模型产线导出是否成功
     */
    void setPackageStatus(Long taskId,Boolean success);

}