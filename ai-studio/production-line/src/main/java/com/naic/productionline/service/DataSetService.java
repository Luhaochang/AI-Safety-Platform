package com.naic.productionline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.naic.api.api.DataPage;
import com.naic.productionline.domain.dto.DataSetDTO;
import com.naic.productionline.domain.po.DataSetPO;
import com.naic.productionline.domain.vo.DataSetVO;

import java.util.Map;

/**
 * 数据集 业务层
 *
 * @author wangyunong
 */
public interface DataSetService extends IService<DataSetPO> {

    /**
     * 新增数据集
     *
     * @param dto 数据集信息
     */
    void insertDataSet(DataSetDTO dto);

    /**
     * 更新数据集
     *
     * @param dto 数据集信息
     */
    void updateDataSet(DataSetDTO dto);

    /**
     * 删除数据集
     *
     * @param id 数据集id
     */
    void deleteDataSet(Long id);

    /**
     * 条件查询
     *
     * @param condition 条件
     * @return DataSetVO
     */
    DataPage<DataSetVO> selectList(Map<String, Object> condition, Integer pageNo, Integer pageSize);

    /**
     * 设置是否官方数据集
     *
     * @param id         数据集id
     * @param isOfficial 是否官方数据集：0.否 1.是
     */
    void setOfficial(Long id, Integer isOfficial);

    /**
     * 设置数据集是否已校验
     *
     * @param id 数据集id
     */
    void setChecked(Long id);
}