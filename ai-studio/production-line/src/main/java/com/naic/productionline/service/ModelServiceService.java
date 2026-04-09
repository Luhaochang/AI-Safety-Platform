package com.naic.productionline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.naic.api.api.DataPage;
import com.naic.productionline.domain.dto.ModelDTO;
import com.naic.productionline.domain.dto.ModelServiceDTO;
import com.naic.productionline.domain.po.ModelServicePO;
import com.naic.productionline.domain.vo.ModelServiceVO;
import com.naic.productionline.domain.vo.ModelVO;
import com.naic.productionline.domain.vo.taskLogTools.TaskLog;
import io.kubernetes.client.openapi.ApiException;

import java.util.List;
import java.util.Map;

/**
 * 服务管理 业务层
 *
 * @author wangyunong
 */
public interface ModelServiceService extends IService<ModelServicePO> {

    /**
     * 条件查询
     *
     * @param condition 条件
     * @return  ModelServiceVO
     */
    DataPage<ModelServiceVO> selectService(Map<String,Object> condition, Integer pageNo, Integer pageSize);

    /**
     * 部署模型
     *
     * @param dto 服务信息
     */
    void insertService(ModelServiceDTO dto) throws ApiException;

    /**
     * 删除服务
     *
     * @param id 服务id
     */
    void deleteService(Long id) throws ApiException;

    /**
     * 根据服务id获取服务
     *
     * @param id 服务id
     * @return ModelServiceVO
     */
    ModelServiceVO getById(Long id);

    /**
     * es查询部署任务日志
     * @param serviceId 服务id
     * @return 查询结果
     */
    List<TaskLog> queryDeploymentByEs(Long serviceId);

    /**
     * 暂停/继续服务
     *
     * @param id 服务id
     * @param status 0.暂停 1.继续
     */
    void quitOrContinueService(Long id,Integer status) throws ApiException;
}
