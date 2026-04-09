package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.DataSetPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据集管理 数据层
 *
 * @author wangyunong
 */
@Mapper
public interface DataSetMapper extends BaseMapper<DataSetPO> {

}