package com.naic.productionline.utils;

import com.naic.api.api.ServiceException;
import com.naic.productionline.config.AirflowConfig;
import com.naic.productionline.config.MlflowConfig;
import com.naic.productionline.domain.dto.AirFlowDTO;
import com.naic.productionline.domain.dto.MlflowRunDTO;
import com.naic.productionline.domain.po.PipelineTaskPO;
import com.naic.productionline.domain.vo.TaskFlowVO;
import com.naic.productionline.domain.vo.mlflow.*;
import com.naic.productionline.mapper.PipelineTaskMapper;
import com.naic.productionline.service.feign.AirflowFeign;
import com.naic.productionline.service.feign.MlflowFeign;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * MLflow工具
 *
 * @author wangyunong
 * @date 2024/10/15
 */
@Slf4j
@Component
@AllArgsConstructor
public class MLflowUtil {

    private final MlflowConfig mlflowConfig;

    private final AirflowConfig airflowConfig;

    private final PipelineTaskMapper mapper;

    private final AirflowFeign airflowFeign;

    private final MlflowFeign mlflowFeign;


    /**
     * 获取实验id
     */
    public String getExperimentId(String experimentName) {
        List<ExperimentVO> experimentList = mlflowFeign.searchExperiments(mlflowConfig.getRunMaxResults()).getExperiments();
        for (ExperimentVO experiment : experimentList) {
            if (experiment.getName().equals(experimentName)) {
                return experiment.getExperimentId();
            }
        }
        return null;
    }

    public Map<String, String> timeTransform(Long startTime, Long endTime) {
        Map<String, String> map = new HashMap<>();
        if (endTime == null) {
            Instant startInstant = Instant.ofEpochMilli(startTime);
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime startDateTime = startInstant.atZone(zoneId).toLocalDateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            map.put("startTime", startDateTime.format(formatter));
            return map;
        }

        // 将时间戳转换为Instant对象
        Instant startInstant = Instant.ofEpochMilli(startTime);
        Instant endInstant = Instant.ofEpochMilli(endTime);

        // 选择时区，将Instant转换为LocalDateTime
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime startDateTime = startInstant.atZone(zoneId).toLocalDateTime();
        LocalDateTime endDateTime = endInstant.atZone(zoneId).toLocalDateTime();

        // 打印转换后的日期时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        map.put("startTime", startDateTime.format(formatter));
        map.put("endTime", endDateTime.format(formatter));

        // 计算时间差
        Duration duration = Duration.between(startDateTime, endDateTime);
        String formattedDuration = String.format("%02d:%02d:%02d",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.getSeconds() % 60);
        map.put("diff", formattedDuration);

        return map;
    }

    /**
     * 获取Metric
     */
    public List<MetricVO> getMetric(String metricName, String runUuId) {
        List<MetricVO> metricList = mlflowFeign.getMetric(runUuId, metricName, mlflowConfig.getMetricMaxResults()).getMetrics();
        return metricList;
    }

    /**
     * 通过Metric获取epoch当前值
     */
    public List<MetricVO> getEpoch(String experimentName) {
        String experimentId = getExperimentId(experimentName);
        if (experimentId == null) {
            // 修改：返回空列表而不是抛出异常，让调用方决定如何处理，或者抛出异常由上层捕获
            // 这里为了保持原有逻辑风格抛出异常，但在Service层已做捕获
            throw new ServiceException("任务不存在");
        }
        TaskFlowVO taskFlow = new TaskFlowVO();
        List<String> experimentIds = List.of(experimentId);
        List<String> orderBy = List.of("attributes.start_time DESC");
        long maxResults = mlflowConfig.getRunMaxResults();
        String runViewType = "ACTIVE_ONLY";
        List<RunVO> run = mlflowFeign.getRun(new MlflowRunDTO()
                .setExperimentIds(experimentIds)
                .setMaxResults(maxResults).setOrderBy(orderBy)
                .setRunViewType(runViewType)).getRuns();
        // 修改：增加 !run.isEmpty() 判断
        if (run != null && !run.isEmpty()) {
            return getMetric("epoch", run.get(0).getInfo().getRunUuid());
        }
        return new ArrayList<>();
    }

    /**
     * 获取实验的第一个run_uuid
     */
    public String getFirstRunUuId(String experimentId) {
        List<String> experimentIds = List.of(experimentId);
        List<String> orderBy = List.of("attributes.start_time DESC");
        long maxResults = mlflowConfig.getRunMaxResults();
        String runViewType = "ACTIVE_ONLY";
        List<RunVO> run = mlflowFeign.getRun(new MlflowRunDTO()
                .setExperimentIds(experimentIds)
                .setMaxResults(maxResults).setOrderBy(orderBy)
                .setRunViewType(runViewType)).getRuns();
        // 增加非空判断
        if (run != null && !run.isEmpty()) {
            return run.get(0).getInfo().getRunUuid();
        }
        return null;
    }

    /**
     * 获取实验的第一个run的基本信息
     */
    public TaskFlowVO getFirstRun(String experimentName) {
        String experimentId = getExperimentId(experimentName);
        if (experimentId == null) {
            throw new ServiceException("任务不存在");
        }
        TaskFlowVO taskFlow = new TaskFlowVO();
        List<String> experimentIds = List.of(experimentId);
        List<String> orderBy = List.of("attributes.start_time DESC");
        long maxResults = mlflowConfig.getRunMaxResults();
        String runViewType = "ACTIVE_ONLY";
        List<RunVO> run = mlflowFeign.getRun(new MlflowRunDTO()
                .setExperimentIds(experimentIds)
                .setMaxResults(maxResults).setOrderBy(orderBy)
                .setRunViewType(runViewType)).getRuns();

        // 增加非空判断
        if (run != null && !run.isEmpty()) {
            taskFlow.setRunId(run.get(0).getInfo().getRunId());
            taskFlow.setRunUuId(run.get(0).getInfo().getRunUuid());
            taskFlow.setStatus(run.get(0).getInfo().getStatus());
            taskFlow.setLifecycleStage(run.get(0).getInfo().getLifecycleStage());
            Map<String, String> map = new HashMap<>();
            if (run.get(0).getInfo().getEndTime() == null) {
                map = timeTransform(run.get(0).getInfo().getStartTime(), run.get(0).getInfo().getEndTime());
                taskFlow.setStartTime(map.get("startTime"));
            } else {
                map = timeTransform(run.get(0).getInfo().getStartTime(), run.get(0).getInfo().getEndTime());
                taskFlow.setStartTime(map.get("startTime"));
                taskFlow.setEndTime(map.get("endTime"));
                taskFlow.setTimeDiff(map.get("diff"));
            }
            List<ParametersVO> parametersList = run.get(0).getData().getParams();
            taskFlow.setParamsList(parametersList);
            Map<String, List<MetricVO>> metricMap = new HashMap<>();
            if (run.get(0).getData().getMetrics() != null) {
                List<String> metricNameList = new ArrayList<>();
                for (MetricVO metricVO : run.get(0).getData().getMetrics()) {
                    metricNameList.add(metricVO.getKey());
                    List<MetricVO> metricList = getMetric(metricVO.getKey(), run.get(0).getInfo().getRunUuid());
                    metricMap.put(metricVO.getKey(), metricList);
                }
            }
            taskFlow.setMetricList(metricMap);
        }
        return taskFlow;
    }

    /**
     * 获取实验的第一个run的完成时间信息
     */
    public String getClassDict(String experimentName) {
        String experimentId = getExperimentId(experimentName);
        if (experimentId == null) {
            throw new ServiceException("任务不存在");
        }
        List<String> experimentIds = List.of(experimentId);
        List<String> orderBy = List.of("attributes.start_time DESC");
        long maxResults = mlflowConfig.getRunMaxResults();
        String runViewType = "ACTIVE_ONLY";
        List<RunVO> run = mlflowFeign.getRun(new MlflowRunDTO()
                .setExperimentIds(experimentIds)
                .setMaxResults(maxResults).setOrderBy(orderBy)
                .setRunViewType(runViewType)).getRuns();

        // 增加非空判断
        if (run != null && !run.isEmpty()) {
            List<ParametersVO> parametersList = run.get(0).getData().getParams();
            for (ParametersVO vo : parametersList) {
                if (vo.getKey().equals("class_dict")) {
                    return vo.getValue();
                }
            }
        }
        return "";
    }

    /**
     * 获取实验的第一个run的完成时间信息
     */
    public Long getRunTime(String experimentName) {
        String experimentId = getExperimentId(experimentName);
        if (experimentId == null) {
            throw new ServiceException("任务不存在");
        }
        List<String> experimentIds = List.of(experimentId);
        List<String> orderBy = List.of("attributes.start_time DESC");
        long maxResults = mlflowConfig.getRunMaxResults();
        String runViewType = "ACTIVE_ONLY";
        List<RunVO> run = mlflowFeign.getRun(new MlflowRunDTO()
                .setExperimentIds(experimentIds)
                .setMaxResults(maxResults).setOrderBy(orderBy)
                .setRunViewType(runViewType)).getRuns();
        // 增加非空判断
        if (run != null && !run.isEmpty() && run.get(0).getInfo().getEndTime() != null) {
            return run.get(0).getInfo().getEndTime() - run.get(0).getInfo().getStartTime();
        }
        return 0L;
    }

    /**
     * 获取模型文件目录
     */
    public List<ArtifactPathVO> getArtifactsPath(String experimentName, String path) {
        String experimentId = getExperimentId(experimentName);
        if (experimentId == null) {
            throw new ServiceException("任务不存在");
        }
        String runUuId = getFirstRunUuId(experimentId);
        if (runUuId == null) {
            throw new ServiceException("任务不存在");
        }
        ArtifactVO artifact = mlflowFeign.getArtifacts(runUuId, path);
        for (ArtifactPathVO artifactPath : artifact.getFiles()) {
            if (!artifactPath.getIsDir()) {
                artifactPath.setBasePath(mlflowConfig.getDownloadPath() + experimentId + "/" + runUuId + "/" + "artifacts" + "/" + artifactPath.getPath());
            }
        }
        return artifact.getFiles();
    }

    /**
     * 获取模型文件url
     */
    public String getTaskModelUrl(Long taskId) {
        PipelineTaskPO task = mapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException("产线不存在");
        }
        String experimentId = getExperimentId(task.getExperimentName());
        if (experimentId == null) {
            throw new ServiceException("任务不存在");
        }
        String runUuId = getFirstRunUuId(experimentId);
        if (runUuId == null) {
            throw new ServiceException("任务不存在");
        }

        RunInfo artifact = mlflowFeign.getRunInfo(runUuId);
        if (artifact.getRun().getOutputs().getModelOutputsVOS().get(0)==null){
            throw new ServiceException("模型不存在");
        }
        String modelId=artifact.getRun().getOutputs().getModelOutputsVOS().get(0).getModelId();
        return mlflowFeign.getModelArtifacts(modelId).getModel().getInfo().getArtifact_uri();
    }

    /**
     * 导出镜像文件
     */
    public void getImage(Long taskId) {
        PipelineTaskPO task = mapper.selectById(taskId);
        task.setImageUrl(mlflowConfig.getImageHeader() + task.getExperimentName() + "-image:1.0.0");

        task.setIsPackage(1);
        mapper.updateById(task);
        Map<String, Object> map = new HashMap<>();
        map.put("image_url", task.getImageUrl());
        map.put("task_id", task.getId());
        map.put("model_url", getTaskModelUrl(taskId));
        AirFlowDTO dto = new AirFlowDTO()
                .setDagRunId(UUID.randomUUID().toString())
                .setConf(map);
        airflowFeign.dagRun(dto);
    }

}