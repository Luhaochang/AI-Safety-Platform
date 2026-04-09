package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.api.api.DataPage;
import com.naic.api.api.ServiceException;
import com.naic.api.utils.Condition;
import com.naic.productionline.domain.cnv.DataSetCnv;
import com.naic.productionline.domain.dto.DataSetDTO;
import com.naic.productionline.domain.po.DataSetPO;
import com.naic.productionline.domain.po.DataSetTaskPO;
import com.naic.productionline.domain.po.JupyterServicePO;
import com.naic.productionline.domain.po.PipelineTaskPO;
import com.naic.productionline.domain.vo.DataSetVO;
import com.naic.productionline.mapper.DataSetMapper;
import com.naic.productionline.mapper.DataSetTaskMapper;
import com.naic.productionline.mapper.JupyterServiceMapper;
import com.naic.productionline.mapper.PipelineTaskMapper;
import com.naic.productionline.service.DataSetService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * 数据集管理 服务实现
 *
 * @author wangyunong
 */
@Service
@AllArgsConstructor
public class DataSetServiceImpl extends ServiceImpl<DataSetMapper, DataSetPO> implements DataSetService {

    private final DataSetMapper dataSetMapper;

    private final DataSetCnv dataSetCnv;

    private final DataSetTaskMapper dataSetTaskMapper;

    // 移除 SceneRelationMapper 依赖
    // private final SceneRelationMapper sceneRelationMapper;

    private final PipelineTaskMapper pipelineTaskMapper;

    private final JupyterServiceMapper jupyterServiceMapper;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void insertDataSet(DataSetDTO dto) {
        DataSetPO po = dataSetCnv.dtoToPo(dto);

        // 移除旧的 sceneRelationMapper 查询逻辑
        // 直接使用 DTO 中的 taskTypeId 和 appSceneId

        //未校验
        po.setIsChecked(0);
        //非官方数据集
        po.setIsOfficial(0);
        //未打包
        po.setIsPackage(0);
        dataSetMapper.insert(po);
        String fileUrl = po.getDataSetName() + "-id-" + po.getId() + "-" + UUID.randomUUID().toString();
        po.setFileUrl(fileUrl);
        dataSetMapper.updateById(po);
    }

    @Override
    public void updateDataSet(DataSetDTO dto) {
        Integer size = dataSetTaskMapper.selectList(new QueryWrapper<DataSetTaskPO>().eq("data_set_id", dto.getId())).size();
        if (!size.equals(0)) {
            throw new ServiceException("该数据集有标注任务，无法修改");
        }
        Integer pipelineSize = pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>().eq("data_set_id", dto.getId())).size();
        if (!pipelineSize.equals(0)) {
            throw new ServiceException("该数据集已被用于训练任务，无法修改");
        }
        Integer jupyterSize = jupyterServiceMapper.selectList(new QueryWrapper<JupyterServicePO>().eq("dataset_id", dto.getId())).size();
        if (!jupyterSize.equals(0)) {
            throw new ServiceException("该数据集已被用于jupyter服务，无法修改");
        }

        DataSetPO po = dataSetCnv.dtoToPo(dto);
        // 移除旧的 scene 设置逻辑
        dataSetMapper.updateById(po);
    }

    @Override
    public void deleteDataSet(Long id) {
        Integer size = dataSetTaskMapper.selectList(new QueryWrapper<DataSetTaskPO>().eq("data_set_id", id)).size();
        if (!size.equals(0)) {
            throw new ServiceException("该数据集有标注任务，无法删除");
        }
        Integer pipelineSize = pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>().eq("data_set_id", id)).size();
        if (!pipelineSize.equals(0)) {
            throw new ServiceException("该数据集已被用于训练任务，无法修改");
        }
        Integer jupyterSize = jupyterServiceMapper.selectList(new QueryWrapper<JupyterServicePO>().eq("dataset_id", id)).size();
        if (!jupyterSize.equals(0)) {
            throw new ServiceException("该数据集已被用于jupyter服务，无法修改");
        }
        dataSetMapper.deleteById(id);
    }

    @Override
    public DataPage<DataSetVO> selectList(Map<String, Object> condition, Integer pageNo, Integer pageSize) {
        IPage<DataSetVO> page = dataSetMapper.selectPage(new Page<>(pageNo, pageSize), Condition.getQueryWrapper(condition, DataSetPO.class)).convert(dataSetCnv::poToVo);
        // 移除循环查询应用场景名称的逻辑，因为字段已变更且表关联已移除
        // 如果前端需要展示场景名称，建议前端通过字典匹配 appSceneId
        return DataPage.rest(page);
    }

    @Override
    public void setOfficial(Long id, Integer isOfficial) {
        DataSetPO po = dataSetMapper.selectById(id);
        po.setIsOfficial(isOfficial);
        dataSetMapper.updateById(po);
    }

    @Override
    public void setChecked(Long id) {
        DataSetPO po = dataSetMapper.selectById(id);
        if (!po.getIsMarked().equals(2)) {
            throw new ServiceException("未标注完成的数据集无法校验");
        }
        po.setIsChecked(1);
        dataSetMapper.updateById(po);
    }

}