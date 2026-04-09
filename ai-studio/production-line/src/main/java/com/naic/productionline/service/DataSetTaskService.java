package com.naic.productionline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.naic.api.api.DataPage;
import com.naic.productionline.domain.dto.DataSetTaskDTO;
import com.naic.productionline.domain.po.DataSetTaskPO;
import com.naic.productionline.domain.vo.DataSetTaskVO;
import io.kubernetes.client.openapi.ApiException;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * 数据集标注任务 业务层
 *
 * @author wangyunong
 */
public interface DataSetTaskService extends IService<DataSetTaskPO> {

    /**
     * 条件查询
     *
     * @param condition 条件
     * @return  DataSetTaskVO
     */
    DataPage<DataSetTaskVO> selectList(Map<String,Object> condition, Integer pageNo, Integer pageSize);

    /**
     * 新建标注任务
     *
     * @param dto 数据集标注信息
     */
    void insertDataSetTask(DataSetTaskDTO dto);

    /**
     * 删除标注任务
     *
     * @param id 数据集标注任务id
     */
    void deleteDataSetTask(Long id) throws ApiException;

    /**
     * 保存标注任务进度文件
     *
     * @param file  标注任务进度文件
     * @param id 数据集标注任务id
     */
    String saveTask(MultipartFile file,Long id,Long finishNum) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    /**
     * 提交标注任务进度文件
     *
     * @param file  标注任务进度文件
     * @param id 数据集标注任务id
     */
    String submitTask(MultipartFile file,Long id,Long finishNum) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

}
