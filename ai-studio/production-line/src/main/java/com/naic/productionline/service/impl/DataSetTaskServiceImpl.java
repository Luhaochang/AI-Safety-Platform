package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.api.api.DataPage;
import com.naic.api.api.ServiceException;
import com.naic.api.utils.Condition;
import com.naic.productionline.domain.cnv.DataSetTaskCnv;
import com.naic.productionline.domain.dto.DataSetTaskDTO;
import com.naic.productionline.domain.po.AutoLabelPO;
import com.naic.productionline.domain.po.DataSetPO;
import com.naic.productionline.domain.po.DataSetTaskPO;
import com.naic.productionline.domain.vo.DataSetTaskVO;
import com.naic.productionline.mapper.AutoLabelMapper;
import com.naic.productionline.mapper.DataSetMapper;
import com.naic.productionline.mapper.DataSetTaskMapper;
import com.naic.productionline.mapper.SceneRelationMapper;
import com.naic.productionline.service.AutoLabelService;
import com.naic.productionline.service.DataSetTaskService;
import com.naic.productionline.service.minio.ResourcesService;
import io.kubernetes.client.openapi.ApiException;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * 数据集标注任务管理 服务实现
 *
 * @author wangyunong
 */
@Service
@AllArgsConstructor
public class DataSetTaskServiceImpl extends ServiceImpl<DataSetTaskMapper, DataSetTaskPO> implements DataSetTaskService {

    private final DataSetTaskMapper dataSetTaskMapper;

    private final DataSetTaskCnv dataSetTaskCnv;

    private final DataSetMapper dataSetMapper;

    private final ResourcesService resourcesService;

    private final SceneRelationMapper sceneRelationMapper;

    private final AutoLabelMapper autoLabelMapper;

    private final AutoLabelService autoLabelService;

    @Override
    public DataPage<DataSetTaskVO> selectList(Map<String, Object> condition, Integer pageNo, Integer pageSize) {
        IPage<DataSetTaskVO> page = dataSetTaskMapper.selectPage(new Page<>(pageNo, pageSize), Condition.getQueryWrapper(condition, DataSetTaskPO.class)).convert(dataSetTaskCnv::poToVo);
        for (DataSetTaskVO task : page.getRecords()) {
            DataSetPO po = dataSetMapper.selectById(task.getDataSetId());
            task.setDataSetName(po.getDataSetName());
        }
        return DataPage.rest(page);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void insertDataSetTask(DataSetTaskDTO dto) {
        DataSetPO dataSet = dataSetMapper.selectById(dto.getDataSetId());
        //是否标注：0.未标注
        if (!dataSet.getIsMarked().equals(0)) {
            throw new ServiceException("数据集已标注或已有标注任务，无法再次标注");
        }
        dataSet.setIsChecked(1);
        //是否标注:1. 标注中
        dataSet.setIsMarked(1);
        dataSetMapper.updateById(dataSet);
        DataSetTaskPO po = dataSetTaskCnv.dtoToPo(dto);
        //状态：0.标注中
        po.setStatus(0);
        po.setFinishNumber(0L);
        dataSetTaskMapper.insert(po);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteDataSetTask(Long id) throws ApiException {
        DataSetTaskPO task = dataSetTaskMapper.selectById(id);

        List<AutoLabelPO> list = autoLabelMapper.selectList(new QueryWrapper<AutoLabelPO>().eq("data_set_task_id", id));
        if (!list.isEmpty()) {
            for (AutoLabelPO po : list) {
                autoLabelService.deleteAutoLabel(po.getId());
            }
        }
        //状态：0.标注中
        if (task.getStatus().equals(0)) {
            DataSetPO dataSet = dataSetMapper.selectById(task.getDataSetId());
            //是否标注：0.未标注
            dataSet.setIsMarked(0);
            dataSet.setIsChecked(0);
            dataSetMapper.updateById(dataSet);
        }
        dataSetTaskMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public String saveTask(MultipartFile file, Long id, Long finishNum) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        DataSetTaskPO task = dataSetTaskMapper.selectById(id);
        //状态：1.标注完成
        if (task.getStatus().equals(1)) {
            throw new ServiceException("任务已完成，无法修改");
        }
        DataSetPO dataSet = dataSetMapper.selectById(task.getDataSetId());
        String url = null;
        if (dataSet.getDataType() == 1) {
            //如果是图像，保存YOLO数据集标注格式
            url = dataSet.getFileUrl() + "/labels/" + file.getOriginalFilename();
        } else if (dataSet.getDataType() == 2) {
            //如果是文本，直接更新文件
            url = dataSet.getFileUrl() + "/" + file.getOriginalFilename();
        } else if (dataSet.getDataType() == 3) {
            //如果是表格，直接更新文件
            url = dataSet.getFileUrl() + "/" + file.getOriginalFilename();
        }
        String markUrl = resourcesService.updateFile(file, url);


        dataSetMapper.updateById(dataSet);
        updateFinishNum(id, finishNum);
        return markUrl;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public String submitTask(MultipartFile file, Long id, Long finishNum) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        DataSetTaskPO task = dataSetTaskMapper.selectById(id);
        //状态：1.标注完成
        if (task.getStatus().equals(1)) {
            throw new ServiceException("任务已完成，无法重复提交");
        }
        task.setStatus(1);
        dataSetTaskMapper.updateById(task);
        DataSetPO dataSet = dataSetMapper.selectById(task.getDataSetId());
        String markUrl = resourcesService.updateFile(file, file.getOriginalFilename());
        //2.已标注
        dataSet.setIsMarked(2);
        dataSetMapper.updateById(dataSet);
        updateFinishNum(id, finishNum);
        return markUrl;
    }

    private void updateFinishNum(Long taskId, Long finishNum) {
        DataSetTaskPO task = dataSetTaskMapper.selectById(taskId);
        task.setFinishNumber(finishNum);
        dataSetTaskMapper.updateById(task);
    }
}
