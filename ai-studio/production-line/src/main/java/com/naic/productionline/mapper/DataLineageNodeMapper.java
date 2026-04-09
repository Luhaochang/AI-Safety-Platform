package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.DataLineageNodePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataLineageNodeMapper extends BaseMapper<DataLineageNodePO> {
}
