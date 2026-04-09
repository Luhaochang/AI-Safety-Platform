package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.naic.api.api.DataPage;
import com.naic.productionline.domain.po.*;
import com.naic.productionline.domain.vo.homePage.*;
import com.naic.productionline.domain.vo.mlflow.MetricVO;
import com.naic.productionline.mapper.*;
import com.naic.productionline.service.HomePageService;
import com.naic.productionline.service.feign.NodeExporterFeign;
import com.naic.productionline.utils.MLflowUtil;
import com.naic.productionline.utils.TrainUtil;
import io.kubernetes.client.openapi.ApiException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author wyn
 */
@Service
@Slf4j
@AllArgsConstructor
public class HomePageServiceImpl implements HomePageService {

    private final DataSetMapper dataSetMapper;

    private final PipelineTaskMapper pipelineTaskMapper;

    private final DataSetTaskMapper dataSetTaskMapper;

    private final ModelMapper modelMapper;

    private final ModelServiceMapper modelServiceMapper;

    private final TaskTypeMapper taskTypeMapper;

    private final MLflowUtil mLflowUtil;

    private final TrainUtil trainUtil;

    private final NodeExporterFeign nodeExporterFeign;

    private final static Pattern METRIC_PATTERN = Pattern.compile(
            "node_cpu_seconds_total\\{cpu=\"(\\d+)\",mode=\"([^\"]+)\"\\} (\\d+\\.\\d+)"
    );

    @Override
    public List<DataSetStatistics> getDataSetStatistics() {
        List<DataSetStatistics> list = new ArrayList<>();
        // 获取所有任务类型，不再使用固定的0-4循环
        List<TaskTypePO> taskTypeList = taskTypeMapper.selectList(new QueryWrapper<TaskTypePO>().orderByAsc("id"));

        if (taskTypeList != null && !taskTypeList.isEmpty()) {
            for (TaskTypePO taskType : taskTypeList) {
                DataSetStatistics dataSetStatistics = new DataSetStatistics();
                // 设置场景ID (对应 taskTypeId)
                dataSetStatistics.setTaskTypeId((long) taskType.getId().intValue());

                // 查询条件修改为 task_type_id，并使用 count 提高性能
                Integer count = Math.toIntExact(dataSetMapper.selectCount(new QueryWrapper<DataSetPO>().eq("task_type_id", taskType.getId())));
                dataSetStatistics.setDataSetNum(count);

                list.add(dataSetStatistics);
            }
        }
        return list;
    }

    @Override
    public DataPage<DataSetTaskStatistics> getDataSetTaskStatistics(Integer pageNo, Integer pageSize) {
        List<DataSetTaskStatistics> list = new ArrayList<>();
        List<DataSetTaskPO> taskList = dataSetTaskMapper.selectList(new QueryWrapper<DataSetTaskPO>().eq("status", 0));
        for (DataSetTaskPO task : taskList) {
            DataSetTaskStatistics pipelineTaskStatistics = new DataSetTaskStatistics()
                    .setDataSetTaskName(task.getTaskName())
                    .setCompletionProgress(getScore(task.getFinishNumber(), task.getSetNumber()));
            list.add(pipelineTaskStatistics);
        }
        return DataPage.rest(list, pageNo, pageSize);
    }

    @Override
    public List<FinishedPipelineTaskStatistics> getFinishedPipelineTaskStatistics() {
        List<FinishedPipelineTaskStatistics> list = new ArrayList<>();
        //时间分布：0. <1h 1. 1-24h 2.1-7day 3. 7-30day 4. >30day
        for (int i = 0; i < 5; i++) {
            FinishedPipelineTaskStatistics finishedPipelineTaskStatistics = new FinishedPipelineTaskStatistics().setType(i);
            if (i == 0) {
                List<PipelineTaskPO> taskList = pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>()
                        .lt("train_time", 3600000L));
                finishedPipelineTaskStatistics.setModelNum(taskList != null ? taskList.size() : 0);
            } else if (i == 1) {
                List<PipelineTaskPO> taskList = pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>()
                        .gt("train_time", 3600000L)
                        .lt("train_time", 86400000L));
                finishedPipelineTaskStatistics.setModelNum(taskList != null ? taskList.size() : 0);
            } else if (i == 2) {
                List<PipelineTaskPO> taskList = pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>()
                        .gt("train_time", 86400000L)
                        .lt("train_time", 604800000L));
                finishedPipelineTaskStatistics.setModelNum(taskList != null ? taskList.size() : 0);
            } else if (i == 3) {
                List<PipelineTaskPO> taskList = pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>()
                        .gt("train_time", 604800000L)
                        .lt("train_time", 2590000000L));
                finishedPipelineTaskStatistics.setModelNum(taskList != null ? taskList.size() : 0);
            } else {
                List<PipelineTaskPO> taskList = pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>()
                        .gt("train_time", 2590000000L));
                finishedPipelineTaskStatistics.setModelNum(taskList != null ? taskList.size() : 0);
            }
            list.add(finishedPipelineTaskStatistics);
        }
        return list;
    }

    @Override
    public List<ModelStatistics> getModelStatistics() {
        List<ModelStatistics> list = new ArrayList<>();
        // 获取所有任务类型，不再使用固定的0-4循环
        List<TaskTypePO> taskTypeList = taskTypeMapper.selectList(new QueryWrapper<TaskTypePO>().orderByAsc("id"));

        if (taskTypeList != null && !taskTypeList.isEmpty()) {
            for (TaskTypePO taskType : taskTypeList) {
                ModelStatistics modelStatistics = new ModelStatistics();
                // 设置场景ID (对应 task_type_id)
                modelStatistics.setTaskTypeId((long) taskType.getId().intValue());

                // 查询条件修改为 task_type_id，并使用 count 提高性能
                Integer count = Math.toIntExact(modelMapper.selectCount(new QueryWrapper<ModelPO>().eq("task_type_id", taskType.getId())));
                modelStatistics.setModelNum(count);

                list.add(modelStatistics);
            }
        }
        return list;
    }

    @Override
    public List<PipelineTaskStatistics> getPipelineTaskStatistics() {
        List<PipelineTaskStatistics> list = new ArrayList<>();
        // 查询正在运行中的任务 (status = 2)
        List<PipelineTaskPO> taskList = pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>().eq("status", 2));

        if (taskList == null || taskList.isEmpty()) {
            return list;
        }

        for (PipelineTaskPO task : taskList) {
            PipelineTaskStatistics pipelineTaskStatistics = new PipelineTaskStatistics().setPipelineTaskName(task.getLineName());
            try {
                // 增加安全性检查：确保 epochs 有效且大于 0，防止除零错误
                if (task.getEpochs() == null || task.getEpochs() <= 0) {
                    pipelineTaskStatistics.setCompletionProgress(0.0);
                } else {
                    // 使用 try-catch 包裹 MLflow 调用，防止因单个任务异常（如实验不存在）导致整个接口崩溃
                    try {
                        List<MetricVO> metricList = mLflowUtil.getEpoch(task.getExperimentName());
                        if (metricList != null && !metricList.isEmpty()) {
                            MetricVO lastMetric = metricList.get(metricList.size() - 1);
                            if (lastMetric != null && lastMetric.getStep() != null) {
                                pipelineTaskStatistics.setCompletionProgress(getScore(lastMetric.getStep(), task.getEpochs()));
                            } else {
                                pipelineTaskStatistics.setCompletionProgress(0.0);
                            }
                        } else {
                            pipelineTaskStatistics.setCompletionProgress(0.0);
                        }
                    } catch (Exception e) {
                        log.warn("获取任务 {} 的 MLflow 数据失败: {}", task.getLineName(), e.getMessage());
                        pipelineTaskStatistics.setCompletionProgress(0.0);
                    }
                }
            } catch (Exception e) {
                log.error("计算任务 {} 进度时发生异常", task.getLineName(), e);
                pipelineTaskStatistics.setCompletionProgress(0.0);
            }
            list.add(pipelineTaskStatistics);
        }
        return list;
    }

    @Override
    public SummaryStatistics getSummaryStatistics() {

        List<DataSetPO> dataSetList = dataSetMapper.selectList(null);
        List<DataSetPO> markedDataSetList = dataSetMapper.selectList(new QueryWrapper<DataSetPO>().eq("is_marked", 2));
        List<ModelPO> modelList = modelMapper.selectList(null);
        List<ModelPO> unofficialModelList = modelMapper.selectList(new QueryWrapper<ModelPO>().eq("is_official", 0));
        List<PipelineTaskPO> pipelineTaskList = pipelineTaskMapper.selectList(null);
        List<PipelineTaskPO> finishedPipelineTaskList = pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>().eq("status", 4));
        List<ModelServicePO> modelServiceList = modelServiceMapper.selectList(null);
        List<ModelServicePO> aliveModelServiceList = modelServiceMapper.selectList(new QueryWrapper<ModelServicePO>().eq("status", 2));
        SummaryStatistics summaryStatistics = new SummaryStatistics()
                .setDataSetNum(dataSetList != null ? dataSetList.size() : 0)
                .setMarkedDataSetNum(markedDataSetList != null ? markedDataSetList.size() : 0)
                .setModelNum(modelList != null ? modelList.size() : 0)
                .setUnofficialModelNum(unofficialModelList != null ? unofficialModelList.size() : 0)
                .setPipelineTaskNum(pipelineTaskList != null ? pipelineTaskList.size() : 0)
                .setFinishedPipelineTaskNum(finishedPipelineTaskList != null ? finishedPipelineTaskList.size() : 0)
                .setServiceNum(modelServiceList != null ? modelServiceList.size() : 0)
                .setAliveServiceNum(aliveModelServiceList != null ? aliveModelServiceList.size() : 0);
        return summaryStatistics;
    }

    @Override
    public DataPage<NodeStatistics> getNodeStatistics(Integer pageNo, Integer pageSize) throws ApiException {
        List<NodeStatistics> list = trainUtil.getNodeStatistics();
        List<NodeExporterStatistics> gpuUtilizationList = getGpuUtilization();
        List<NodeExporterStatistics> cpuUtilizationList = getCpuUtilization();
        List<NodeExporterStatistics> memoryUtilizationList = getMemoryUtilization();
        List<NodeExporterStatistics> fileSystemUtilizationList = getFileSystemUtilization();
        for (NodeStatistics node : list) {
            node.setGpuUtilization(getNodeExporter(node.getNodeAddress(), gpuUtilizationList));
            node.setCpuUtilization(getNodeExporter(node.getNodeAddress(), cpuUtilizationList));
            node.setMemoryUtilization(getNodeExporter(node.getNodeAddress(), memoryUtilizationList));
            node.setFileSystemUtilization(getNodeExporter(node.getNodeAddress(), fileSystemUtilizationList));
        }
        return DataPage.rest(list, pageNo, pageSize);
    }

    public Double getScore(Long finishedNum, Long setNumber) {
        // 增加判空和除零保护
        if (finishedNum == null || setNumber == null || setNumber == 0) {
            return 0.0;
        }
        double result = (double) finishedNum / setNumber;
        return Math.round(result * 100.0) / 100.0;
    }

    /**
     * 获取节点对应的NodeExporterStatistics信息
     */
    public String getNodeExporter(String nodeInstance, List<NodeExporterStatistics> list) {
        for (NodeExporterStatistics node : list) {
            String nodeUrl = node.getMetric().getInstance();
            if (nodeUrl.substring(0, nodeUrl.indexOf(':')).equals(nodeInstance)) {
                return node.getValue().get(1);
            }
        }
        return null;
    }

    /**
     * GPU使用率
     */
    public List<NodeExporterStatistics> getGpuUtilization() {
        return nodeExporterFeign.getMetrics("node_gpu_utilization").getData().getResult();
    }

    /**
     * CPU使用率
     */
    public List<NodeExporterStatistics> getCpuUtilization() {
        return nodeExporterFeign.getMetrics("100 - (avg(irate(node_cpu_seconds_total{mode=\"idle\",}[5m])) by (instance,service_id,ecs_cname) * 100)").getData().getResult();
    }

    /**
     * 内存使用率
     */
    public List<NodeExporterStatistics> getMemoryUtilization() {
        return nodeExporterFeign.getMetrics("100 - (node_memory_MemAvailable_bytes{} / node_memory_MemTotal_bytes{} * 100)").getData().getResult();
    }

    /**
     * 文件系统使用率
     */
    public List<NodeExporterStatistics> getFileSystemUtilization() {
        return nodeExporterFeign.getMetrics("max((node_filesystem_size_bytes{fstype=~\"ext.?|xfs\",}-node_filesystem_free_bytes{fstype=~\"ext.?|xfs\",}) *100/(node_filesystem_avail_bytes {fstype=~\"ext.?|xfs\",}+(node_filesystem_size_bytes{fstype=~\"ext.?|xfs\",}-node_filesystem_free_bytes{fstype=~\"ext.?|xfs\",})))by(ecs_cname,instance,service_id)").getData().getResult();
    }
}