package com.naic.productionline.service;

import com.naic.api.api.DataPage;
import com.naic.productionline.domain.dto.JupyterServiceDTO;
import com.naic.productionline.domain.dto.ModelServiceDTO;
import com.naic.productionline.domain.po.JupyterImage;
import com.naic.productionline.domain.po.SceneRelation;
import com.naic.productionline.domain.vo.JupyterServiceVO;
import io.kubernetes.client.openapi.ApiException;

import java.util.List;
import java.util.Map;

/**
 * jupyter_image 业务层
 *
 * @author wangyunong
 */
public interface JupyterImageService {

    /**
     * JupyterImage列表
     *
     * @return  List<JupyterImage>
     */
    List<JupyterImage> getJupyterImage();

    /**
     * 根据模型id查询JupyterService服务
     *
     * @return  List<JupyterServiceVO>
     */
    List<JupyterServiceVO> getJupyterServiceByModelId(Long modelId);


    /**
     * JupyterImage列表
     *
     * @return  List<JupyterImage>
     */
    DataPage<JupyterServiceVO> getJupyterService(Map<String,Object> condition, Integer pageNo, Integer pageSize);

    /**
     * 部署镜像
     *
     * @param dto jupyter 服务
     */
    void insertService(JupyterServiceDTO dto) throws ApiException;

    /**
     * 删除服务
     *
     * @param id 服务id
     */
    void deleteService(Long id) throws ApiException;
}
