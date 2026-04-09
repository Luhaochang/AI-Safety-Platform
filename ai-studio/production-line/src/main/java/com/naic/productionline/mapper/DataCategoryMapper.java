package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.DataCategoryPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataCategoryMapper extends BaseMapper<DataCategoryPO> {
}
