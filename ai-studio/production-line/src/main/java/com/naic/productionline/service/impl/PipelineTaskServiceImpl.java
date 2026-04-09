package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.api.api.DataPage;
import com.naic.api.api.ServiceException;
import com.naic.api.utils.Condition;
import com.naic.api.utils.SpringUtils;
import com.naic.productionline.config.KubernetesConfig;
import com.naic.productionline.config.MlflowConfig;
import com.naic.productionline.domain.cnv.PipelineTaskCnv;
import com.naic.productionline.domain.dto.PipelineTaskDTO;
import com.naic.productionline.domain.po.DataSetPO;
import com.naic.productionline.domain.po.ModelPO;
import com.naic.productionline.domain.po.PipelineTaskPO;
import com.naic.productionline.domain.vo.*;
import com.naic.productionline.domain.vo.mlflow.ArtifactPathVO;
import com.naic.productionline.domain.vo.taskLogTools.TaskLog;
import com.naic.productionline.mapper.DataSetMapper;
import com.naic.productionline.mapper.ModelMapper;
import com.naic.productionline.mapper.PipelineTaskMapper;
import com.naic.productionline.mapper.SceneRelationMapper;
import com.naic.productionline.service.PipelineTaskService;
import com.naic.productionline.utils.MLflowUtil;
import com.naic.productionline.utils.TrainUtil;
import io.kubernetes.client.openapi.ApiException;
import lombok.AllArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 模型产线管理 服务实现
 *
 * @author wangyunong
 */
@Service
@EnableAsync
@AllArgsConstructor
public class PipelineTaskServiceImpl extends ServiceImpl<PipelineTaskMapper, PipelineTaskPO> implements PipelineTaskService {

    private final PipelineTaskMapper pipelineTaskMapper;

    private final PipelineTaskCnv pipelineTaskCnv;

    private final DataSetMapper dataSetMapper;

    private final ModelMapper modelMapper;

    private final TrainUtil trainUtil;

    private final MLflowUtil mLflowUtil;

    private final KubernetesConfig config;

    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    private final MlflowConfig mlflowConfig;

    @Override
    public DataPage<PipelineTaskVO> selectList(Map<String, Object> condition, Integer pageNo, Integer pageSize) {
        IPage<PipelineTaskVO> page = pipelineTaskMapper.selectPage(new Page<>(pageNo, pageSize), Condition.getQueryWrapper(condition, PipelineTaskPO.class)).convert(pipelineTaskCnv::poToVo);
        return DataPage.rest(page);
    }

    @Override
    public Long insertPipelineTask(PipelineTaskDTO dto) {
        PipelineTaskPO po = pipelineTaskCnv.dtoToPo(dto);
        if (po.getAppSceneId() == null && po.getTaskTypeId() == null) {
            throw new ServiceException("至少选择应用场景或任务场景中的一个");
        }
        //产线状态：0.配置中
        po.setStatus(0);
        //镜像导出状态0.未导出
        po.setIsPackage(0);
//        po.setEpochs(0L);
        po.setTrainTime(0L);
//        po.setLineId("yunchen-"+po.getId());
        pipelineTaskMapper.insert(po);
        return po.getId();
    }

    @Override
    public void updatePipelineTask(PipelineTaskDTO dto) {
        PipelineTaskPO task = pipelineTaskMapper.selectById(dto.getId());
        //产线状态：0.配置中
        if (!task.getStatus().equals(0)) {
            throw new ServiceException("产线不处于‘配置中’状态，无法修改配置");
        }

        PipelineTaskPO po = pipelineTaskCnv.dtoToPo(dto);
        po.setLineId("yunchen-" + po.getId());
        pipelineTaskMapper.updateById(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePipelineTask(Long id) throws ApiException {
        PipelineTaskPO task = pipelineTaskMapper.selectById(id);
        //产线状态：0.配置中 3.已停止
        if ((!task.getStatus().equals(0)) && (!task.getStatus().equals(3))) {
            trainUtil.deleteJob(task.getJobName());
            trainUtil.deleteConfigMap(task.getMapName());
        }
        pipelineTaskMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    public void queue(Long id, Map<String, String> map) throws ApiException {
        PipelineTaskPO task = pipelineTaskMapper.selectById(id);
        //产线状态：1.排队中
        task.setStatus(1);
        String randomName = trainUtil.generateRandomName();
        String jobName = "model-job-" + task.getId() + "-" + randomName;
        String containerName = "model-container-" + task.getId() + "-" + randomName;
        String experimentName = "model-experiments-" + task.getId() + "-" + randomName;
        String mapName = "map-job-" + task.getId() + "-" + randomName;
        task.setJobName(jobName);
        task.setExperimentName(experimentName);
        task.setNameSpace(config.getNameSpace());
        task.setMapName(mapName);
        pipelineTaskMapper.updateById(task);
        ModelPO model = modelMapper.selectById(task.getModelId());
        DataSetPO dataSet= dataSetMapper.selectById(task.getDataSetId());
        //加入数据集地址
//        map.put("data", dataSet.getFileUrl());
        map.put("mlflow-experiment", experimentName);
        map.put("mlflow-tracking-uri", mlflowConfig.getPath());
        trainUtil.train(map, config.getNameSpace(), jobName, containerName,
                model.getFileUrl(), mapName, experimentName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quit(Long id) throws ApiException {
        PipelineTaskPO task = pipelineTaskMapper.selectById(id);
        //产线状态：3.已停止
        task.setStatus(3);
        pipelineTaskMapper.updateById(task);
        trainUtil.deleteJob(task.getJobName());
        trainUtil.deleteConfigMap(task.getMapName());
    }

    @Override
    public TaskFlowVO getMlflowMessage(Long id) {
        PipelineTaskPO task = pipelineTaskMapper.selectById(id);
        return mLflowUtil.getFirstRun(task.getExperimentName());
    }

    @Override
    public List<ArtifactPathVO> getArtifacts(Long id, String path) {
        PipelineTaskPO task = pipelineTaskMapper.selectById(id);
        //4.运行完成
        if (!task.getStatus().equals(4)) {
            throw new ServiceException("产线未运行成功无法获取模型结果！");
        }
        return mLflowUtil.getArtifactsPath(task.getExperimentName(), path);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void getImageUrl(Long id) {
        mLflowUtil.getImage(id);
    }

    @Override
    public List<TaskLog> queryByEs(Long taskId) {
        PipelineTaskPO task = pipelineTaskMapper.selectById(taskId);
        //产线状态：0.配置中1.排队中
        if (task.getStatus().equals(0) || task.getStatus().equals(1)) {
            throw new ServiceException("任务未运行");
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String jobNameToSearch = task.getJobName();
        if (SpringUtils.isEmpty(jobNameToSearch)) {
            throw new ServiceException("产线不符合规范");
        }
        boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("kubernetes.job.name", jobNameToSearch));

        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("@timestamp").order(SortOrder.ASC));
        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        query.setTrackTotalHits(true);

        SearchHits<TaskLog> searchHits = elasticsearchRestTemplate.search(query, TaskLog.class);
        List<TaskLog> taskLogList = new ArrayList<>();
        for (SearchHit<TaskLog> searchHit : searchHits.getSearchHits()) {
            TaskLog po = searchHit.getContent();
            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE_TIME;
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ZonedDateTime dateTime = ZonedDateTime.parse(po.getTimestamp(), inputFormatter);
            String formattedDateTime = dateTime.format(outputFormatter);
            po.setTimestamp(formattedDateTime);
            taskLogList.add(po);
        }
        return taskLogList;
    }

    @Override
    public void setPackageStatus(Long taskId, Boolean success) {
        PipelineTaskPO task = pipelineTaskMapper.selectById(taskId);
        //镜像导出状态0.未导出1.导出中2.导出完成3.导出失败
        if (!task.getIsPackage().equals(1) && !task.getIsPackage().equals(3)) {
            throw new ServiceException("产线已导出成功或者未进行导出操作，无法修改导出状态");
        }
        if (success) {
            task.setIsPackage(2);
        } else {
            task.setIsPackage(3);
        }
        pipelineTaskMapper.updateById(task);
    }

}
