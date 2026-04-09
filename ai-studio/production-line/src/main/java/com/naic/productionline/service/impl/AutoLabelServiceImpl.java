package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.api.api.DataPage;
import com.naic.api.api.ServiceException;
import com.naic.api.utils.Condition;
import com.naic.api.utils.SpringUtils;
import com.naic.productionline.config.KubernetesConfig;
import com.naic.productionline.config.MlflowConfig;
import com.naic.productionline.domain.cnv.AutoLabelCnv;
import com.naic.productionline.domain.cnv.PipelineTaskCnv;
import com.naic.productionline.domain.dto.AutoLabelDTO;
import com.naic.productionline.domain.dto.PipelineTaskDTO;
import com.naic.productionline.domain.po.*;
import com.naic.productionline.domain.vo.AutoLabelVO;
import com.naic.productionline.domain.vo.PipelineTaskVO;
import com.naic.productionline.domain.vo.TaskFlowVO;
import com.naic.productionline.domain.vo.mlflow.ArtifactPathVO;
import com.naic.productionline.domain.vo.taskLogTools.TaskLog;
import com.naic.productionline.mapper.*;
import com.naic.productionline.service.AutoLabelService;
import com.naic.productionline.service.ModelServiceService;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自动化标注管理 服务实现
 *
 * @author wangyunong
 */
@Service
@EnableAsync
@AllArgsConstructor
public class AutoLabelServiceImpl extends ServiceImpl<AutoLabelMapper, AutoLabelPO> implements AutoLabelService {

    private final AutoLabelMapper autoLabelMapper;
    private final ModelServiceMapper modelServiceMapper;
    private final DataSetMapper dataSetMapper;
    private final DataSetTaskMapper dataSetTaskMapper;
    private final AutoLabelCnv autoLabelCnv;
    private final ModelMapper modelMapper;
    private final TrainUtil trainUtil;
    private final KubernetesConfig config;

    @Override
    public DataPage<AutoLabelVO> selectList(Long dataSetTaskId, Integer pageNo, Integer pageSize) {
        List<AutoLabelPO> list = autoLabelMapper.selectList(new QueryWrapper<AutoLabelPO>().eq("data_set_task_id", dataSetTaskId));
        List<AutoLabelVO> voList = new ArrayList<>();
        for (AutoLabelPO po : list) {
            ModelServicePO service = modelServiceMapper.selectById(po.getServiceId());
            voList.add(autoLabelCnv.poToVo(po).setServiceName((service == null || !service.getStatus().equals(2)) ? "服务已下线" : service.getName()));
        }
        return DataPage.rest(voList, pageNo, pageSize);
    }

    @Override
    public Long insertAutoLabel(AutoLabelDTO dto) {
        if (!isMatch(dto.getDataSetTaskId(), dto.getServiceId())) {
            throw new ServiceException("所选服务不满足自动标注条件");
        }
        AutoLabelPO autoLabelPO = autoLabelCnv.dtoToPo(dto);
        //标注状态：0.未标注
        autoLabelPO.setStatus(0);
        autoLabelMapper.insert(autoLabelPO);
        return autoLabelPO.getId();
    }

    @Override
    public void updateAutoLabel(AutoLabelDTO dto) {
        AutoLabelPO autoLabel = autoLabelMapper.selectById(dto.getId());
        if (!autoLabel.getStatus().equals(0)) {
            throw new ServiceException("不处于未标注状态的任务无法修改");
        }
        if (!isMatch(dto.getDataSetTaskId(), dto.getServiceId())) {
            throw new ServiceException("所选服务不满足自动标注条件");
        }
        AutoLabelPO po = autoLabelCnv.dtoToPo(dto);
        autoLabelMapper.updateById(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAutoLabel(Long id) throws ApiException {
        AutoLabelPO autoLabel = autoLabelMapper.selectById(id);
        //标注状态：0.未标注
        if (!autoLabel.getStatus().equals(0)) {
            trainUtil.deleteJob(autoLabel.getJobName());
            trainUtil.deleteConfigMap(autoLabel.getMapName());
        }
        autoLabelMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    public void queue(Long id) throws IOException, ApiException {
        AutoLabelPO autoLabel = autoLabelMapper.selectById(id);
        ModelServicePO modelService = modelServiceMapper.selectById(autoLabel.getServiceId());
        ModelPO modelPO = modelMapper.selectById(modelService.getModelId());
        DataSetTaskPO dataSetTask = dataSetTaskMapper.selectById(autoLabel.getDataSetTaskId());
        DataSetPO dataSetPO = dataSetMapper.selectById(dataSetTask.getDataSetId());
        Map<String, String> map = new HashMap<>();
        map.put("file_url", dataSetPO.getFileUrl());
        map.put("service_url", modelService.getServiceUrl());
        map.put("class_dict", modelPO.getClassDict());
        map.put("task_id", autoLabel.getDataSetTaskId().toString());
        autoLabel.setStatus(1);
        String randomName = trainUtil.generateRandomName();
        String jobName = "auto-label-job-" + autoLabel.getId() + "-" + randomName;
        String containerName = "auto-label-container-" + autoLabel.getId() + "-" + randomName;
        String experimentName = "auto-label-experiments-" + autoLabel.getId() + "-" + randomName;
        String mapName = "auto-label-map-" + autoLabel.getId() + "-" + randomName;
        autoLabel.setJobName(jobName);
        autoLabel.setNameSpace(config.getNameSpace());
        autoLabel.setMapName(mapName);
        autoLabelMapper.updateById(autoLabel);
        trainUtil.train(map, config.getNameSpace(), jobName, containerName,
                config.getAutoLabelImageUrl(), mapName, experimentName);
    }

    public Boolean isMatch(Long dataSetTaskId, Long serviceId) {
        DataSetTaskPO task = dataSetTaskMapper.selectById(dataSetTaskId);
        ModelServicePO service = modelServiceMapper.selectById(serviceId);
        if (!service.getStatus().equals(2)) {
            return false;
        }
//        ModelPO model = modelMapper.selectById(service.getModelId());
//        if (task.getDataType().equals(0L)) {
//            if (task.getScene().equals(model.getScene())) {
//                return true;
//            }
//        } else if (task.getSecondaryScene().equals(model.getSecondaryScene())) {
//            return true;
//        }
        return false;
    }
}
