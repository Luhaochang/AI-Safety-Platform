package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naic.api.api.DataPage;
import com.naic.api.utils.Condition;
import com.naic.productionline.domain.cnv.JupyterServiceCnv;
import com.naic.productionline.domain.dto.JupyterServiceDTO;
import com.naic.productionline.domain.po.JupyterImage;
import com.naic.productionline.domain.po.JupyterServicePO;
import com.naic.productionline.domain.po.ModelServicePO;
import com.naic.productionline.domain.po.SceneRelation;
import com.naic.productionline.domain.vo.JupyterServiceVO;
import com.naic.productionline.domain.vo.ModelServiceVO;
import com.naic.productionline.mapper.JupyterImageMapper;
import com.naic.productionline.mapper.JupyterServiceMapper;
import com.naic.productionline.mapper.SceneRelationMapper;
import com.naic.productionline.service.JupyterImageService;
import com.naic.productionline.service.SceneRelationService;
import com.naic.productionline.utils.JupyterUtil;
import io.kubernetes.client.openapi.ApiException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模型管理 服务实现
 *
 * @author wangyunong
 */
@Service
@AllArgsConstructor
public class JupyterImageServiceImpl implements JupyterImageService {

    private final JupyterImageMapper jupyterImageMapper;

    private final JupyterServiceMapper jupyterServiceMapper;

    private final JupyterServiceCnv jupyterServiceCnv;

    private JupyterUtil jupyterUtil;

    @Override
    public List<JupyterImage> getJupyterImage() {
        return jupyterImageMapper.selectList(null);
    }

    @Override
    public List<JupyterServiceVO> getJupyterServiceByModelId(Long modelId) {
        List<JupyterServicePO> list= jupyterServiceMapper.selectList(new QueryWrapper<JupyterServicePO>().eq("model_id",modelId));
        List<JupyterServiceVO> voList=new ArrayList<>();
        for (JupyterServicePO po:list){
            JupyterServiceVO vo=jupyterServiceCnv.poToVo(po);
            JupyterImage image=jupyterImageMapper.selectById(vo.getJupyterId());
            vo.setJupyterImage(image);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public DataPage<JupyterServiceVO> getJupyterService(Map<String, Object> condition, Integer pageNo, Integer pageSize) {
        IPage<JupyterServiceVO> page=jupyterServiceMapper.selectPage(new Page<>(pageNo,pageSize), Condition.getQueryWrapper(condition, JupyterServicePO.class)).convert(jupyterServiceCnv::poToVo);
        List<JupyterServiceVO> list=page.getRecords();
        for(JupyterServiceVO vo:list){
            JupyterImage image=jupyterImageMapper.selectById(vo.getJupyterId());
            vo.setJupyterImage(image);
        }
        return DataPage.rest(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertService(JupyterServiceDTO dto) throws ApiException {
        JupyterServicePO po=jupyterServiceCnv.dtoToPo(dto);
        //部署状态：1.部署中
        po.setStatus(1);
        jupyterServiceMapper.insert(po);
        jupyterUtil.trainDeployment(po.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteService(Long id) throws ApiException {
        JupyterServicePO service=jupyterServiceMapper.selectById(id);
        String serviceName=service.getServiceName();
        jupyterServiceMapper.deleteById(id);
        jupyterUtil.deleteService(serviceName);
        jupyterUtil.deletePersistentVolume(service.getPersistentVolumeName(),service.getPersistentVolumeClaimName());
    }

}
