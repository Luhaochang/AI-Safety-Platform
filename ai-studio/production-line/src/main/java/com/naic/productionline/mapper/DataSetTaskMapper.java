package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.DataSetTaskPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据标注管理 数据层
 * 
 * @author wangyunong
 */
@Mapper
public interface DataSetTaskMapper extends BaseMapper<DataSetTaskPO> {

}
