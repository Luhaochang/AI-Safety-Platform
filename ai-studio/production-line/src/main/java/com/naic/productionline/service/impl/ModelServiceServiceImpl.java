package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.api.api.DataPage;
import com.naic.api.api.ServiceException;
import com.naic.api.utils.Condition;
import com.naic.api.utils.SpringUtils;
import com.naic.productionline.domain.cnv.ModelCnv;
import com.naic.productionline.domain.cnv.ModelServiceCnv;
import com.naic.productionline.domain.dto.ModelServiceDTO;
import com.naic.productionline.domain.po.ModelPO;
import com.naic.productionline.domain.po.ModelServicePO;
import com.naic.productionline.domain.po.PipelineTaskPO;
import com.naic.productionline.domain.vo.ModelServiceVO;
import com.naic.productionline.domain.vo.ModelVO;
import com.naic.productionline.domain.vo.taskLogTools.TaskLog;
import com.naic.productionline.mapper.ModelMapper;
import com.naic.productionline.mapper.ModelServiceMapper;
import com.naic.productionline.service.ModelServiceService;
import com.naic.productionline.utils.MLflowUtil;
import com.naic.productionline.utils.ModelServiceUtil;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 服务管理 服务实现
 *
 * @author wangyunong
 */
@Service
@AllArgsConstructor
public class ModelServiceServiceImpl extends ServiceImpl<ModelServiceMapper, ModelServicePO> implements ModelServiceService {

    private final ModelServiceMapper modelServiceMapper;

    private final ModelServiceCnv modelServiceCnv;

    private final ModelMapper modelMapper;

    private final ModelCnv modelCnv;

    private final ModelServiceUtil modelServiceUtil;

    private final ElasticsearchRestTemplate elasticsearchRestTemplate;



    @Override
    public DataPage<ModelServiceVO> selectService(Map<String, Object> condition, Integer pageNo, Integer pageSize) {
        IPage<ModelServiceVO> page=modelServiceMapper.selectPage(new Page<>(pageNo,pageSize), Condition.getQueryWrapper(condition, ModelServicePO.class)).convert(modelServiceCnv::poToVo);
        return DataPage.rest(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertService(ModelServiceDTO dto) throws ApiException {
        ModelServicePO po=modelServiceCnv.dtoToPo(dto);
        //部署状态：1.部署中
        po.setStatus(1);
        modelServiceMapper.insert(po);
        modelServiceUtil.trainDeployment(po.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteService(Long id){
        ModelServicePO service=modelServiceMapper.selectById(id);
        String serviceName=service.getServiceName();
        modelServiceMapper.deleteById(id);
        modelServiceUtil.deleteService(serviceName);
    }

    @Override
    public ModelServiceVO getById(Long id) {
        ModelServicePO service=modelServiceMapper.selectById(id);
        if(service==null){
            throw new ServiceException("不存在该服务");
        }
        ModelServiceVO vo= modelServiceCnv.poToVo(service);
        ModelPO model=modelMapper.selectById(service.getModelId());
        vo.setModel(modelCnv.poToVo(model));
        return vo;
    }

    @Override
    public List<TaskLog> queryDeploymentByEs(Long serviceId) {
        ModelServicePO service=modelServiceMapper.selectById(serviceId);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String labelsNameToSearch = service.getServiceName();
        if (SpringUtils.isEmpty(labelsNameToSearch)) {
            throw new ServiceException("服务不符合规范");
        }
        boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("kubernetes.labels.app", labelsNameToSearch));
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
    @Transactional(rollbackFor = Exception.class)
    public void quitOrContinueService(Long id,Integer status) {
        ModelServicePO service=modelServiceMapper.selectById(id);
        if(!service.getStatus().equals(2)&&!service.getStatus().equals(4)){
            throw new ServiceException("不符合暂停/继续条件");
        }
        String deploymentName=service.getServiceName();
        if(status.equals(0)) {
            //部署状态：4.暂停
            service.setStatus(4);
            modelServiceMapper.updateById(service);
            modelServiceUtil.quitOrContinueDeployment(deploymentName, status);
        }
        else if(status.equals(1)) {
            //部署状态：1.部署中
            service.setStatus(1);
            modelServiceMapper.updateById(service);
            modelServiceUtil.quitOrContinueDeployment(deploymentName, status);
        }
    }

}
